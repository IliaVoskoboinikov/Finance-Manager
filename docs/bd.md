# Local Database

## Overview

Локальная база данных используется для хранения финансовых данных пользователя
в offline-first режиме и служит источником данных для слоя `core:data`.

База данных хранит:

- счета пользователя
- категории доходов и расходов
- транзакции
- состояние синхронизации с сервером

Модель данных спроектирована с учетом:

- офлайн-работы
- последующей синхронизации
- разделения `localId` и `serverId`

---

## Entities & Relationships

```mermaid
erDiagram
    ACCOUNT {
        string localId PK
        int serverId
        string name
        string balance
        string currency
        string createdAt
        string updatedAt
        string syncStatus
    }

    CATEGORY {
        int id PK
        string name
        string emoji
        boolean isIncome
    }

    TRANSACTION {
        string localId PK
        int serverId
        string accountLocalId FK
        int categoryId FK
        string currencyCode
        string amount
        string transactionDate
        string comment
        string createdAt
        string updatedAt
        string syncStatus
    }

    ACCOUNT ||--o{ TRANSACTION: "localId → accountLocalId (FK)"
    CATEGORY ||--o{ TRANSACTION: "id → categoryId (FK)"
```// Revue me>>
