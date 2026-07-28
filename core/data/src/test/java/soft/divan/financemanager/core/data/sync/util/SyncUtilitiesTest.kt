package soft.divan.financemanager.core.data.sync.util

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SyncUtilitiesTest {

    @Test
    fun `sync delegates to syncWith passing the synchronizer`() = runTest {
        val syncable = mockk<Syncable>()
        coEvery { syncable.syncWith(any()) } returns true
        val synchronizer = object : Synchronizer {}

        val result = with(synchronizer) { syncable.sync() }

        assertThat(result).isTrue()
        coVerify(exactly = 1) { syncable.syncWith(synchronizer) }
    }

    @Test
    fun `sync propagates false result`() = runTest {
        val syncable = mockk<Syncable>()
        coEvery { syncable.syncWith(any()) } returns false
        val synchronizer = object : Synchronizer {}

        val result = with(synchronizer) { syncable.sync() }

        assertThat(result).isFalse()
    }
}
