package soft.divan.financemanager.core.loggingerror

import java.util.Collections
import java.util.IdentityHashMap

/** Цепочка `cause` длиннее этого — патология; дальше звенья не копируются. */
internal const val MAX_CAUSE_DEPTH = 32

/**
 * Копия исключения без текста, пригодная для отправки третьей стороне (Crashlytics).
 *
 * [message] — имя типа исходного исключения: по нему видно, что упало, а текст исходника
 * отброшен. Стек переносится из исходника, поэтому в отчёте видно место сбоя. Тип самой
 * записи в Crashlytics у всех копий один — `RedactedException`.
 */
internal class RedactedException(
    originalType: String,
    cause: RedactedException?
) : Exception(originalType, cause) {

    // Свой стек не нужен: его сразу заменяет стек исходника
    override fun fillInStackTrace(): Throwable = this
}

/**
 * Возвращает копию цепочки исключений без текста: каждое звено `cause` — [RedactedException]
 * с типом и стеком исходного звена.
 *
 * Текст исключения не наш: библиотеки и платформа вставляют в него разбираемые значения —
 * `DateTimeParseException: Text '<значение>' could not be parsed`, Gson `JsonReader`:
 * `Expected a long but was <значение>`. Что окажется в тексте завтра, зависит от библиотеки, а не
 * от нас, поэтому наружу идут только тип и стек. Цикл в `cause` обрывается на повторном звене,
 * `suppressed` не копируются.
 */
internal fun Throwable.redacted(): RedactedException =
    redact(this, Collections.newSetFromMap(IdentityHashMap()), depth = 1)

private fun redact(original: Throwable, seen: MutableSet<Throwable>, depth: Int): RedactedException {
    seen += original
    val cause = original.cause?.takeIf { depth < MAX_CAUSE_DEPTH && it !in seen }
    // cause — через конструктор: initCause у исключения, созданного с cause, бросает
    return RedactedException(
        originalType = original.javaClass.name,
        cause = cause?.let { redact(it, seen, depth + 1) }
    ).apply { stackTrace = original.stackTrace }
}
