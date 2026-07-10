package soft.divan.financemanager.core.network.serialization

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.math.BigDecimal

/**
 * Сериализует денежные значения как [BigDecimal] без потери точности.
 *
 * По умолчанию Gson читает JSON-число в `Double` (binary64), что искажает десятичные
 * суммы. Адаптер обходит `Double`: при чтении строит [BigDecimal] прямо из текста
 * числового токена, при записи выводит «плоское» десятичное число (без экспоненты и
 * кавычек), которое серверный `decimal` принимает как обычное JSON-число.
 */
class BigDecimalTypeAdapter : TypeAdapter<BigDecimal>() {

    override fun write(out: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            out.nullValue()
            return
        }
        // jsonValue пишет verbatim-токен без кавычек — число, а не строка.
        out.jsonValue(value.toPlainString())
    }

    override fun read(reader: JsonReader): BigDecimal? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        // nextString на числовом токене отдаёт исходный текст числа → точный BigDecimal.
        return BigDecimal(reader.nextString())
    }
}
