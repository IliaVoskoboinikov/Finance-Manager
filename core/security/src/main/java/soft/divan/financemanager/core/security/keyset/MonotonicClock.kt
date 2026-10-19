package soft.divan.financemanager.core.security.keyset

/**
 * Монотонные часы: миллисекунды с загрузки устройства, включая сон
 * (`SystemClock.elapsedRealtime`).
 *
 * В отличие от часов системы, их не переводит ни пользователь, ни синхронизация времени —
 * поэтому по ним отсчитываются паузы за неверный PIN. При перезагрузке они начинаются с нуля.
 */
fun interface MonotonicClock {

    /** Сколько миллисекунд прошло с загрузки устройства. */
    fun elapsedMillis(): Long
}
