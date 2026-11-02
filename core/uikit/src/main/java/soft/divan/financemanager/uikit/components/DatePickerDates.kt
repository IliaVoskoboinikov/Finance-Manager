package soft.divan.financemanager.uikit.components

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Переводит дату в миллисекунды Material3 `DatePicker`.
 *
 * Пикер хранит выбранный день как полночь UTC, а не как момент в поясе устройства, поэтому
 * перевод идёт через [ZoneOffset.UTC]: через пояс устройства день сдвигается на ±1.
 */
internal fun LocalDate.toDatePickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/**
 * Обратный к [toDatePickerMillis] перевод миллисекунд `DatePicker` в дату — тоже через UTC,
 * иначе в поясах UTC− полночь UTC попадает на предыдущий день.
 */
internal fun datePickerMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
