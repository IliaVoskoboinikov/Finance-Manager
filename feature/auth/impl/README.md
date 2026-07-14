# feature:auth:impl

## Назначение
Реализация фичи авторизации: экран входа/регистрации, экран профиля и их ViewModel.
Реализует `AuthFeatureApi` из `:feature:auth:api` и настраивает навигацию для модуля `app`.

Поддерживаемые способы входа:
- Имя + пароль (вход / регистрация) через `AuthRepository` из `:core:domain`.
- Гостевой режим.
- **Вход через Яндекс (OAuth)** на базе Yandex ID SDK (`com.yandex.android:authsdk`).

## Схема входа через Яндекс
1. Экран запускает вход через `YandexAuthSdk.contract`
   (`rememberLauncherForActivityResult`). Пользователь авторизуется в приложении Яндекса
   или в браузере.
2. При `YandexAuthResult.Success` ViewModel передаёт `access_token` Яндекса в
   `AuthRepository.loginWithYandex(...)`, который отправляет его на `api/v1/auth/oauth/yandex`.
3. Бэкенд проверяет токен в Яндексе и возвращает собственную пару токенов приложения —
   дальше сессия ведёт себя как при обычном входе (слияние гостевых данных, первичная синхронизация).

### Конфигурация
Yandex ID SDK объявляет `meta-data` с `client_id` и OAuth deep-link через
manifest-плейсхолдеры. AGP подставляет плейсхолдеры в объединённые манифесты библиотек
на уровне **app-модуля**, поэтому они заданы в `:app` `build.gradle.kts` (а не здесь):
- `YANDEX_CLIENT_ID` — `client_id` приложения в Яндекс OAuth, читается через `getSecret()`
  только из переменной окружения (CI) или `local.properties`. В исходники не коммитится —
  как `API_TOKEN`. Без него сборка проходит, но вход через Яндекс не работает.
- `YANDEX_OAUTH_HOST` — `oauth.yandex.ru`.

`client_id` не является настоящим секретом (SDK кладёт его в `meta-data` манифеста, и он
виден в схеме редиректа `yx<client_id>`), но в репозитории его не держим, чтобы не
смешивать с конфигом сборки и не путать с секретами.

## Зависимости
- `:feature:auth:api`, `:core:uikit`, `:core:logging-error`, `:core:auth`, `:core:domain`, `:sync`
- Yandex ID SDK, Hilt, Compose.
