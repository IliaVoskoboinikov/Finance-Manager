package soft.divan.financemanager.feature.sounds.impl.data.sounds

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.feature.sounds.api.domain.SoundType

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SoundsPoolHolderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `holder loads sounds and plays known type without crashing`() {
        val holder = SoundsPoolHolder(context)

        holder.play(SoundType.COIN)
    }

    @Test
    fun `every sound type is handled without crashing`() {
        val holder = SoundsPoolHolder(context)

        SoundType.entries.forEach { type -> holder.play(type) }
    }
}
