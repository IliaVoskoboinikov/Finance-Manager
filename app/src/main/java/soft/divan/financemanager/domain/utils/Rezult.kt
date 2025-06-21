package soft.divan.financemanager.domain.utils

sealed class Rezult<out T : Any?> {

    data class Success<out T : Any?>(val data: T) : Rezult<T>()

    data class Error(val exception: Exzeption) : Rezult<Nothing>() {
        constructor(reason: Reazon) : this(Exzeption(reason = reason))
        constructor(wrapped: Throwable) : this(Exzeption(wrapped = wrapped))
    }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}
