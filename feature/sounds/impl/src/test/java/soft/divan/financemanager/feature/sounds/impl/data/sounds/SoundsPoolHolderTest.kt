package soft.divan.financemanager.feature.sounds.impl.data.sounds

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.test.core.app.ApplicationProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import soft.divan.financemanager.feature.sounds.api.domain.SoundType
import soft.divan.financemanager.feature.sounds.impl.R

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SoundsPoolHolderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val soundPool = SoundPool.Builder()
        .setAudioAttributes(soundEffectAttributes())
        .build()
    private val shadowPool = shadowOf(soundPool)

    private fun holder() = SoundsPoolHolder(context, soundPool)

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

    @Test
    fun `play before load completes is deferred until the sound is loaded`() {
        val holder = holder()

        holder.play(SoundType.COIN)
        assertThat(shadowPool.wasResourcePlayed(R.raw.coin)).isFalse()

        shadowPool.notifyResourceLoaded(R.raw.coin, true)

        assertThat(shadowPool.getResourcePlaybacks(R.raw.coin)).hasSize(1)
    }

    @Test
    fun `repeated plays during loading collapse into one`() {
        val holder = holder()

        repeat(3) { holder.play(SoundType.COIN) }
        shadowPool.notifyResourceLoaded(R.raw.coin, true)

        assertThat(shadowPool.getResourcePlaybacks(R.raw.coin)).hasSize(1)
    }

    @Test
    fun `play after load plays immediately every time`() {
        val holder = holder()
        shadowPool.notifyResourceLoaded(R.raw.coin, true)

        holder.play(SoundType.COIN)
        holder.play(SoundType.COIN)

        assertThat(shadowPool.getResourcePlaybacks(R.raw.coin)).hasSize(2)
    }

    @Test
    fun `load completion without a pending request plays nothing`() {
        holder()

        shadowPool.notifyResourceLoaded(R.raw.coin, true)

        assertThat(shadowPool.wasResourcePlayed(R.raw.coin)).isFalse()
    }

    @Test
    fun `failed load drops the pending request and keeps the sound silent`() {
        val holder = holder()

        holder.play(SoundType.COIN)
        shadowPool.notifyResourceLoaded(R.raw.coin, false)
        holder.play(SoundType.COIN)

        assertThat(shadowPool.wasResourcePlayed(R.raw.coin)).isFalse()
    }

    @Test
    fun `sound effects use sonification attributes muted by silent mode`() {
        val attributes = soundEffectAttributes()

        assertThat(attributes.usage).isEqualTo(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        assertThat(attributes.contentType).isEqualTo(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    }
}
