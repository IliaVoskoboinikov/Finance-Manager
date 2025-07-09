package soft.divan.financemanager.category.domain

inline fun <T, R> Result<T>.resolve(
    onSuccess: (value: T) -> R,
    onError: (exception: Throwable) -> R,
): R {
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess(getOrThrow())
        else -> onError(exception)
    }
}