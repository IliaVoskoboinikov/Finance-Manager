package soft.divan.finansemanager.core.database.model

enum class SyncStatus {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE
}

/**
| SyncStatus     | serverId | Что делать                   |
| -------------- | -------- | ---------------------------- |
| PENDING_CREATE | null     | create → update local        |
| PENDING_UPDATE | not null | update                       |
| PENDING_DELETE | null     | delete local only            |
| PENDING_DELETE | not null | delete remote → delete local |
| SYNCED         | not null | nothing                      |
 */