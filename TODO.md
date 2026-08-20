1# 📋 TODO — технический бэклог

Задачи на будущее. Выполненное отмечаем `- [x]`, новое добавляем строкой `- [ ] ...`
в подходящий раздел (или заводим новый раздел).

> Продуктовый роадмап — в [README.md → Roadmap](README.md#roadmap-планы-развития).

## 🏗 Архитектура и модули

- [ ] Улучшить Gradle-модули
- [ ] Проанализировать архитектуру и разобраться с `common`-модулем
- [ ] Рефакторинг модуля `pincode` — сделать обёртку, от которой наследуется
      реализация экранов/ViewModel'ей: она автоматически запрашивает PIN при
      бездействии, а для экранов, где PIN не нужен, его можно отключить
- [ ] Причесать ViewModel
- [ ] Вытащить интересные идеи из [Now in Android](https://github.com/android/nowinandroid)

## 🧭 Навигация

- [ ] Перейти на Navigation 3
- [ ] Подключить плагин графа навигации — [skydoves/compose-nav-graph](https://github.com/skydoves/compose-nav-graph)

## 🎨 UI / UX

- [ ] Перейти на новый UI
- [ ] Улучшить темизацию
- [ ] Додумать плашку отсутствия интернета

## ⚙️ CI/CD

> Как всё устроено сейчас и обоснование каждого пункта — в [docs/ci-cd.md](docs/ci-cd.md).

Критично:

- [ ] Убрать инъекцию шелла в `send-file-tg`: сообщение коммита подставляется прямо
      в `run:` — передавать через `env:` и `"$VAR"`
- [ ] Завязать релиз на гейт качества — сейчас `cd_release.yml` публикует в Play,
      не запуская ни тестов, ни линтеров
- [ ] Добавить минимальный блок `permissions:` во все workflow
      (`contents: read` + `security-events: write` для SARIF)
- [ ] Настроить `moduleGraphAssert { … }` — задача `:app:assertModuleGraph`
      сейчас пустая (`SKIPPED`), архитектуру реально проверяет `CheckConventionsPlugin`
- [ ] Собирать `assembleRelease` в CI — R8/shrink включены только для release,
      ошибки в `proguard-rules.pro` всплывают лишь в момент релиза

Важно:

- [ ] Добавить триггер на `pull_request` (сейчас прогонов для пул-реквестов нет)
- [ ] Добавить `cancel-in-progress`, чтобы отменялись устаревшие прогоны
- [ ] Починить передачу флагов Gradle: в `ci.yml` они попали в *название* шага,
      в `cd_release.yml` используется несуществующая `$GRADLE_FLAGS`
- [ ] Починить версию в Telegram-отчётах: `printVersionName` печатает
      `Const.VERSION_NAME` (`0.0.1`) и не знает про `-PversionName`
- [ ] Использовать `android-setup` во всех джобах (`run-detekt`, `check-module-graph`,
      `report-telegram`, `distribute-app-firebase` идут на JDK раннера по умолчанию)
- [ ] Добавить Gradle cache для ускорения сборок (релиз — однозначно без кеша,
      тестовые сборки — можно с кешем; пока приложение простое, проблем быть не
      должно, но на больших проектах может быть больно) и убрать дублирующий
      `cache: gradle` в `actions/setup-java`
- [ ] Объединить `run-tests` и `run-coverage` — тесты сейчас прогоняются дважды;
      заодно перевести `./gradlew test` на `testDebugUnitTest`
- [ ] Свести порог покрытия к одному числу: фактически `minBound(95)`,
      в KDoc — 98 %, в `docs/testing.md` — 99 % и 98 %

Улучшения:

- [ ] Добавить AI-ревьюера
- [ ] Настроить Dependabot/Renovate для автообновления зависимостей
- [ ] `timeout-minutes` на джобах и `retention-days` на артефактах
- [ ] Гейты (а не только отчёты) на размер приложения (Ruler) и время сборки
- [ ] Включить загрузку SARIF для Android Lint (сейчас закомментирована)
- [ ] Instrumented-тесты на эмуляторе — понадобятся для миграционных тестов Room
- [ ] Автоматизация релиза: тег + GitHub Release + changelog, release notes для Play
- [ ] Разобраться с `ANDROID_SDK_ROOT: /usr/lib/android-sdk` в CD-workflow —
      на раннерах GitHub SDK лежит по другому пути
- [ ] Завести `YANDEX_CLIENT_ID` как CI-секрет (сейчас в CI-сборках client_id пустой)
- [ ] `CODEOWNERS`, шаблон PR, `SECURITY.md`, бейджи сборки в `README.md`

Сделано:

- [x] Подключить `koverVerifyFull` в CI — гейт покрытия работает в джобе `run-coverage`
      через composite action `.github/actions/coverage`

## 🧪 Тестирование

- [ ] Compose UI-тесты (отложенный «трек 4» плана покрытия: экраны, компоненты
      `core:uikit`, navigation `*FeatureImpl`) + UI-тесты на новый UI
- [ ] Screenshot-тесты (Paparazzi / Roborazzi)

## 🔐 Безопасность

- [ ] `FLAG_SECURE` — запрет скриншотов/записи экрана, с переключателем
      в настройках (отложено из код-ревью, п. 5)
- [ ] Перейти на HTTPS перед выходом на прод-бэкенд — убрать cleartext HTTP
      (сейчас осознанно допустим для тестового стенда)

## 📦 Данные и функциональность

- [ ] Забить айдишники категорий
- [ ] Подумать над мультиязычностью категорий
- [ ] ВАЛЮТА ‼️
- [ ] Учитывать валюту в `GetSumTransactionsUseCase` — сейчас суммы складываются
      без учёта валюты операции (из код-ревью, п. H; часть задачи «валюта»)
- [ ] Guest-merge: двойной учёт баланса при слиянии гостевых данных с аккаунтом
      (из код-ревью, п. J; требует доработки бэкенда)
- [ ] Экспорт данных (CSV / Excel / JSON)
- [ ] Доделать post-commit-sync (готово на ветке `post-commit-sync` — влить в master)
- [ ] Добавить сбор статистики в приложение

## 🔔 Уведомления

> Как устроено сейчас и обоснование решений — в [docs/notifications.md](docs/notifications.md).

- [ ] Адресные пуши, бэкенд: таблица `device_tokens`, ручки `POST /api/v1/devices`
      и `DELETE /api/v1/devices/{token}`, отправка через FCM HTTP v1 с таргетом `token`
- [ ] Адресные пуши, бэкенд: удалять токен по ответам `UNREGISTERED` / `INVALID_ARGUMENT`,
      иначе база зарастает мёртвыми записями
- [ ] Адресные пуши, бэкенд: снимать привязку токена к прежнему пользователю при смене
      аккаунта на устройстве — иначе новый пользователь получит чужие пуши (утечка ПД)
- [ ] Адресные пуши, мобилка: регистрация токена из `onNewToken` через WorkManager
      (сети в этот момент может не быть), плюс при старте и после логина
- [ ] Адресные пуши, мобилка: снятие регистрации по `AuthEvent.OnLogout`
      (`DELETE /devices` + `FirebaseMessaging.deleteToken()`)
- [ ] Адресные пуши, мобилка: `DeviceApiService` + DTO в `core:data`,
      `RegisterPushTokenUseCase` в `core:domain`, кеш «последний отправленный токен + user»
- [ ] Вернуть `deepLink` в `NotificationMessage` и смапить на `NavKey`, когда пуши
      начнут вести на конкретный экран
- [ ] Отказаться от `DelegatingWorker`: отдать `:app` `Configuration.Provider`
      с `HiltWorkerFactory` и убрать дефолтный `WorkManagerInitializer` из `androidx.startup` —
      тогда `@HiltWorker` ставятся в очередь напрямую, а `:core:workmanager` удаляется
- [ ] Дать пользователю настройку напоминания о неактивности (порог / выключить)

## 🛠 Инструменты и качество

- [ ] Подключить LeakCanary
- [ ] Улучшить документацию
- [x] Доделать [help_comand.md](help_comand.md) — шпаргалка по командам: сборка, тесты,
      покрытие, линтеры, графы модулей и навигации, Ruler, ADB-рецепты, пути отчётов

## 🚀 Релиз

- [ ] Реальные Room-миграции + миграционные тесты (`MigrationTestHelper`) вместо
      `fallbackToDestructiveMigration` — обязательно до выхода к реальным пользователям
- [ ] Обфускация и сборка (R8/ProGuard)
- [ ] 🔴 Починить `assembleRelease` — `:app:minifyReleaseWithR8` падает на
      `Missing class kotlinx.parcelize.Parcelize` (тянется из `com.yandex.authsdk`).
      Правило генерирует сам R8: `app/build/outputs/mapping/release/missing_rules.txt`
      (`-dontwarn kotlinx.parcelize.Parcelize`) → перенести в `app/proguard-rules.pro`.
      Заодно ломается `:app:analyzeReleaseBundle`. Всплыло только сейчас, потому что
      CI не собирает release (см. раздел CI/CD выше)
- [ ] Релиз

## 💡 Идеи на подумать

- [ ] Бюджеты / лимиты по категориям с прогрессом за месяц
- [ ] Регулярные (повторяющиеся) операции — подписки, зарплата
- [ ] Поиск и фильтры по истории операций
- [ ] Импорт данных (CSV / JSON) — парный к экспорту
- [ ] Виджет на домашний экран (быстрое добавление расхода / баланс)
- [ ] Напоминание-нотификация «внеси расходы за день»
- [ ] Baseline Profiles / macrobenchmark для ускорения холодного старта
