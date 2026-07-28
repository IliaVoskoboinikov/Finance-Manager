package soft.divan.financemanager.sync.worker

import androidx.work.NetworkType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SyncConstraintsTest {

    @Test
    fun `sync constraints require connected network`() {
        assertThat(SyncConstraints.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
    }

    @Test
    fun `sync constraints are recreated on each access`() {
        // property с get() — каждый вызов строит новый объект, но с тем же содержимым
        assertThat(SyncConstraints).isEqualTo(SyncConstraints)
    }

    @Test
    fun `default synchronization period is 4 hours`() {
        assertThat(SYNCHRONIZATION_PERIOD_IN_HOURS).isEqualTo(4)
    }

    @Test
    fun `interval bounds form a valid range`() {
        assertThat(MIN_SYNC_INTERVAL_HOURS).isEqualTo(1)
        assertThat(MAX_SYNC_INTERVAL_HOURS).isEqualTo(24)
        assertThat(MIN_SYNC_INTERVAL_HOURS).isLessThan(MAX_SYNC_INTERVAL_HOURS)
        assertThat(SYNCHRONIZATION_PERIOD_IN_HOURS)
            .isBetween(MIN_SYNC_INTERVAL_HOURS, MAX_SYNC_INTERVAL_HOURS)
    }

    @Test
    fun `delegatedData stores worker class name for DelegatingWorker`() {
        val data = SyncWorker::class.delegatedData()

        assertThat(data.getString(WORKER_CLASS_NAME)).isEqualTo(SyncWorker::class.java.name)
    }
}
