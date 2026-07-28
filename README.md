# Finance Manager 🚧 [Work in progress] 🚧

Android‑приложение для учёта личных финансов: доходы, расходы, счета, категории,
история операций, синхронизация и базовая аналитика. Проект задуман как pet‑project,
который показывает продуманную архитектуру, работу с данными и современные Android‑подходы.

### Documentation

Полная документация находится в папке `docs/`:

- [docs](./docs/README.md)

---

### Screenshots

| Expenses                        | My Accounts                     | Analytics                        | Category                        | Settings                        |
|---------------------------------|---------------------------------|----------------------------------|---------------------------------|---------------------------------|
| ![](docs/screens/expenses.jpeg) | ![](docs/screens/accounts.jpeg) | ![](docs/screens/analytics.jpeg) | ![](docs/screens/category.jpeg) | ![](docs/screens/settings.jpeg) |

---

### Features

- **Учёт транзакций**
    - Добавление доходов и расходов
    - Привязка к счетам и категориям
    - История операций

- **Счета и категории**
    - Управление счетами (баланс, валюта)
    - Предопределённые категории

- **Статистика и анализ**
    - Сводка по периодам
    - Анализ по категориям и счетам

- **Синхронизация**
    - Фоновая синхронизация через WorkManager (`:sync`)
    - Гибкая настройка интервала синка

- **Персонализация**
    - Тема оформления и дизайн‑настройки (`feature:design-app`)
    - Локаль / язык (`feature:languages`)
    - Вибрация и звуки (`feature:haptics`, `feature:sounds`)

- **Безопасность**
    - Модуль безопасности (`feature:security`) с PIN / биометрией
    - Работа через токен API

---

### Tech Stack

- **Язык**: Kotlin
- **UI**: Jetpack Compose (Material 3, кастомные компоненты в `core:uikit`)
- **Архитектура**:
    - Clean Architecture
    - MVVM (ViewModel + UiState)
    - Мультимодульная структура (core / feature)
- **DI**: Dagger Hilt
- **Асинхронность**: Coroutines + Flow
- **Локальное хранилище**:
    - Room (`FinanceManagerDatabase`)
    - DataStore
- **Сеть**:
    - Retrofit + OkHttp
    - Кастомные Interceptor’ы (Auth, Retry, NetworkConnection, Logging)
- **Фоновая работа**: WorkManager (синхронизация)
- **Качество кода**:
    - Detekt + свои правила
    - Android Lint + модуль `:lint` с кастомными чекерами
    - ktlint

### Roadmap (планы развития)

Продуктовые планы — ниже; полный технический бэклог — в [TODO.md](./TODO.md).

- [ ] Перейти на новый backend (https://github.com/chernykh-dev/finance-manager-backend)
- [x] Сделать регистрацию пользователей с JWT токеном
- [ ] Экспорт данных (CSV / Excel / JSON)
- [ ] Более детальная аналитика (диаграммы, breakdown по периодам/категориям)
- [ ] Сделать экран онбординга и обучения пользователя
- [ ] Добавить аналитику
- [ ] Добавить тесты
- [ ] Релиз в Play Market

