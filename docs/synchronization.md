# Synchronization

Этот документ описывает, как в **Finance Manager** устроена синхронизация данных:
какие модули задействованы, как планируются фоновые задачи и в каком порядке
синхронизируются сущности.

> Немедленные пуши, инициированные внутри локальной БД-транзакции, откладываются до её
> успешного commit — см. [Post-commit sync](./post-commit-sync.md).

## Goals

- Гарантировать **корректный порядок** синка:
    - сначала категории,
    - затем счета,
    - затем транзакции (которые зависят от счетов и категорий).
- Не блокировать UI — всё выполняется в фоне через WorkManager.
- Иметь централизованное место для управления:
    - интервалом периодической синхронизации;
    - триггером "одноразового" синка;
    - временем последней успешной синхронизации.

## High‑level overview

Синк реализован в отдельном модуле `:sync` и пересекается с:

- `core:data` — конкретные менеджеры синка для доменных сущностей;
- `core:network` — сетевые запросы;
- `core:database` — запись в Room;
- feature `synchronization:api/impl` — UI для управления синком.

```mermaid
flowchart TB
    subgraph UI["feature:synchronization:impl"]
      SyncScreen["SynchronizationScreen\n(SynchronizationViewModel)"]
    end

    subgraph SYNC_DOMAIN[":sync – domain"]
      ObserveInterval["ObserveSyncIntervalHoursUseCase"]
      SetInterval["SetSyncIntervalHoursUseCase"]
      ObserveLastTime["ObserveLastSyncTimeUseCase"]
      SetLastTime["SetLastSyncTimeUseCase"]
    end

    subgraph SCHEDULER[":sync – scheduler"]
      Scheduler["WorkManagerSyncScheduler"]
    end

    subgraph WORKER[":sync – worker"]
      SyncWorker["SyncWorker\n(CoroutineWorker + Synchronizer)"]
    end

    subgraph DATA["core:data – pull с сервера"]
      CatSync["CategorySyncManagerImpl"]
      AccSync["AccountSyncManagerImpl"]
      TxSync["TransactionSyncManagerImpl"]
    end

    subgraph OUTBOX["core:data – отправка на сервер"]
      Proc["OutboxProcessor"]
    end

    SyncScreen -->|"setInterval / syncNow"| SYNC_DOMAIN
    SYNC_DOMAIN --> Scheduler
    Scheduler -->|"OneTime / PeriodicWork"| SyncWorker
    SyncWorker --> CatSync
    SyncWorker --> AccSync
    SyncWorker --> TxSync
    SyncWorker --> Proc
    SyncWorker --> SetLastTime
```

### Порядок шагов в `SyncWorker`

Ключевая логика синка находится в `SyncWorker`:

1. Запускается `SyncWorker` по расписанию или по требованию (через `DelegatingWorker` из
   `:core:workmanager` — он общий с `:core:notifications`, подробности в
   [`notifications.md`](./notifications.md)).
2. Внутри `doWork()` вызывается `SyncCoordinator.syncAll()`, который тянет данные **с сервера**.
   Порядок важен (категории нужны транзакциям, счета тоже), но сбой шага **не отменяет
   следующие**: каждый работает с тем, что уже есть локально, и приносит частичный прогресс —
    - `CategorySyncManagerImpl.sync()` — категории не зависят ни от кого;
    - `AccountSyncManagerImpl.sync()` — счета опираются на категории;
    - `TransactionSyncManagerImpl.sync()` — транзакции зависят от счетов;
3. Затем разбирается очередь исходящих операций — `OutboxProcessor.process()`. Этот шаг
   выполняется **всегда**, даже если какой-то pull не удался: накопленные локальные изменения не
   должны ждать отправки из-за проблем с чтением (см. [outbox.md](./outbox.md));
4. Каждый шаг оборачивается в `runStep(name) { ... }`:
    - логируем старт и завершение;
    - исключение шага — это его сбой, но отмена корутины пробрасывается дальше: остановленная
      работа не должна продолжаться со следующего шага;
    - при ошибке `SyncWorker` возвращает `Result.retry()`, чтобы WorkManager повторил задачу;
5. При успешном выполнении всех шагов вызывается `SetLastSyncTimeUseCase` с текущим временем.

> Отправкой локальных изменений менеджеры больше не занимаются: `syncWith` у них выполняет только
> pull. За доставку на сервер отвечает очередь — с повторами, backoff и dead-letter.

**Что считается успехом шага.** `safeApiCall` исключений не бросает, поэтому `syncWith`
возвращает результат pull'а, а не «завершился без исключений» — иначе без сети шаг выглядел бы
удачным, время синхронизации обновлялось бы, а повтор не запрашивался. Сбой — ошибка сети или
сервера и ошибка чтения локальных записей (в этом случае pull прерывается: без них каждая
серверная запись выглядела бы новой и вставлялась бы дубликатом). Сеть, заблокированная
состоянием клиента — гость или нет сессии, — сбоем **не** считается: повтор работы этого не
изменит. Так же устроен шаг очереди: `process()` возвращает `false`, только если записи остались
неотправленными по временной причине.

Упрощённый псевдокод:

```kotlin
suspend fun syncAll(): Boolean {
    // Результаты в переменные: `&&` пропустил бы оставшиеся шаги при первом же сбое
    val categoriesPulled = runStep("CategorySync") { categorySyncManager.sync() }
    val accountsPulled = runStep("AccountSync") { accountSyncManager.sync() }
    val transactionsPulled = runStep("TransactionSync") { transactionSyncManager.sync() }
    val pulled = categoriesPulled && accountsPulled && transactionsPulled

    // Очередь разбирается независимо от исхода pull
    val pushed = runStep("OutboxSync") { outboxProcessor.process() }

    val success = pulled && pushed
    if (success) setLastSyncTimeUseCase(System.currentTimeMillis())
    return success
}
```

### Планирование синка: `WorkManagerSyncScheduler`

Интервал и триггеры синка завязаны на `WorkManagerSyncScheduler`:

- **Периодический синк**:

```kotlin
val request = PeriodicWorkRequestBuilder<DelegatingWorker>(
    hours.toLong(),
    TimeUnit.HOURS
)
    .setConstraints(SyncConstraints)
    .setInputData(SyncWorker::class.delegatedData())
    .build()

WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
        SYNC_PERIODIC_WORK,
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
```

`schedulePeriodicSync` вызывается и на каждом старте (`SyncInitializer`), и при смене интервала
в настройках (`SetSyncIntervalHoursUseCase`). Политика — `UPDATE`: она меняет период уже
поставленной задачи на месте и сохраняет время прошлого запуска, поэтому старт приложения не
сдвигает отсчёт, а идущий синк не прерывается. `KEEP` оставлял задачу со старым периодом, и
новый интервал не применялся; `REPLACE` сбрасывал бы отсчёт на каждом старте.

- **Одноразовый синк** (например, по кнопке "Синхронизировать сейчас"):

```kotlin
val request = OneTimeWorkRequestBuilder<DelegatingWorker>()
    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    .setConstraints(SyncConstraints)
    .setInputData(SyncWorker::class.delegatedData())
    .build()

WorkManager.getInstance(context)
    .enqueueUniqueWork(
        SYNC_ONE_TIME_WORK,
        ExistingWorkPolicy.REPLACE,
        request
    )
```

### Constraints и foreground

- `SyncConstraints` — набор требований к окружению:
    - например, только при наличии сети / на зарядке (см. реализацию в `SyncConstraints.kt`);
- `SyncWorker.getForegroundInfo()` — использует `syncForegroundInfo()` для отображения
  foreground‑уведомления во время синка (особенно важно для длительных задач).

## Sync domain & UI

Модуль `:sync` содержит доменный слой вокруг синхронизации:

- **Use case’ы**:
    - `ObserveLastSyncTimeUseCase`, `ObserveSyncIntervalHoursUseCase`;
    - `SetLastSyncTimeUseCase`, `SetSyncIntervalHoursUseCase`.
- **Repository**:
    - `SyncRepository` / `SyncRepositoryImpl` — единая точка доступа к данным синка
      (например, хранит интервал в DataStore, время последнего синка и т.д.).

Фича `feature:synchronization:impl` подписывается на эти use case’ы, строит `SynchronizationUiState`
и позволяет пользователю:

- изменять интервал фоновой синхронизации;
- запускать синк вручную;
- видеть время последней успешной синхронизации и возможные ошибки.

## Summary

- Синк изолирован в модуле `:sync`, что упрощает поддержку и развитие.
- Порядок синка категорий → счетов → транзакций жёстко зафиксирован в `SyncWorker`.
- WorkManager используется как надёжный механизм планирования с retry‑логикой.
- Фичевая часть (`feature:synchronization:*`) работает только с use case’ами/репозиторием
  и не знает деталей реализации сетевых запросов или Room.

