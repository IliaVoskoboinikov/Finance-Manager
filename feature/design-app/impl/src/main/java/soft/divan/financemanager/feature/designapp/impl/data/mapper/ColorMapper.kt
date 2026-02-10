package soft.divan.financemanager.feature.designapp.impl.data.mapper

import androidx.compose.ui.graphics.Color

private const val COLOR_CHANNEL_MAX = 255
private const val COLOR_CHANNEL_MIN = 0
private const val HEX_RADIX = 16
private const val HEX_CHANNEL_LENGTH = 2
private const val HEX_PADDING_CHAR = '0'

fun Color.toHexString(): String {
    val a = (alpha * COLOR_CHANNEL_MAX)
        .toInt()
        .coerceIn(COLOR_CHANNEL_MIN, COLOR_CHANNEL_MAX)
        .toString(HEX_RADIX)
        .padStart(HEX_CHANNEL_LENGTH, HEX_PADDING_CHAR)

    val r = (red * COLOR_CHANNEL_MAX)
        .toInt()
        .coerceIn(COLOR_CHANNEL_MIN, COLOR_CHANNEL_MAX)
        .toString(HEX_RADIX)
        .padStart(HEX_CHANNEL_LENGTH, HEX_PADDING_CHAR)

    val g = (green * COLOR_CHANNEL_MAX)
        .toInt()
        .coerceIn(COLOR_CHANNEL_MIN, COLOR_CHANNEL_MAX)
        .toString(HEX_RADIX)
        .padStart(HEX_CHANNEL_LENGTH, HEX_PADDING_CHAR)

    val b = (blue * COLOR_CHANNEL_MAX)
        .toInt()
        .coerceIn(COLOR_CHANNEL_MIN, COLOR_CHANNEL_MAX)
        .toString(HEX_RADIX)
        .padStart(HEX_CHANNEL_LENGTH, HEX_PADDING_CHAR)

    return "#$a$r$g$b".uppercase()
}
// Revue me>>
