package soft.divan.financemanager.core.network.util

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
// Revue me>>
