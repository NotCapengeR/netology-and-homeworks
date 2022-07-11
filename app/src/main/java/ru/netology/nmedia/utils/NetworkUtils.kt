package ru.netology.nmedia.utils

import ru.netology.nmedia.network.results.NetworkResult

// Extensions that can modify NetworkResult data if it is iterable

fun <T, I : Iterable<T>> NetworkResult<I>.distinct(): NetworkResult<List<T>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.distinct(),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, K, I : Iterable<T>> NetworkResult<I>.distinctBy(
    selector: (T) -> K
): NetworkResult<List<T>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.distinctBy(selector),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, I : Iterable<T>> NetworkResult<I>.lastOrNull(predicate: (T) -> Boolean): T? =
    this.data?.lastOrNull(predicate)

fun <T, I : Iterable<T>> NetworkResult<I>.lastOrNull(): T? = this.data?.lastOrNull()

inline fun <T, I : Iterable<T>> NetworkResult<I>.firstOrNull(predicate: (T) -> Boolean): T? =
    this.data?.firstOrNull(predicate)

fun <T, I : Iterable<T>> NetworkResult<I>.firstOrNull(): T? = this.data?.firstOrNull()


fun <T, I : Iterable<T>> NetworkResult<I>.lastIndexOf(element: T): Int =
    this.data?.lastIndexOf(element) ?: -1

inline fun <T, I : Iterable<T>> NetworkResult<I>.indexOfLast(
    predicate: (T) -> Boolean
): Int = this.data?.indexOfLast(predicate) ?: -1

inline fun <T, I : Iterable<T>> NetworkResult<I>.indexOfFirst(
    predicate: (T) -> Boolean
): Int = this.data?.indexOfFirst(predicate) ?: -1

inline fun <T, I : Iterable<T>> NetworkResult<I>.find(
    predicate: (T) -> Boolean
): T? = this.data?.find(predicate)


inline fun <T, I : Iterable<T>> NetworkResult<I>.findLast(
    predicate: (T) -> Boolean
): T? = this.data?.findLast(predicate)


inline fun <T, I : Iterable<T>> NetworkResult<I>.filter(predicate: (T) -> Boolean): NetworkResult<List<T>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filter(predicate),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, C : MutableCollection<in T>, I : Iterable<T>> NetworkResult<I>.filterTo(
    destination: C,
    predicate: (T) -> Boolean
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterTo(destination, predicate),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, I : Iterable<T>> NetworkResult<I>.filterIndexed(
    predicate: (index: Int, T) -> Boolean
): NetworkResult<List<T>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterIndexed(predicate),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, C : MutableCollection<in T>, I : Iterable<T>> NetworkResult<I>.filterIndexedTo(
    destination: C,
    predicate: (index: Int, T) -> Boolean
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterIndexedTo(destination, predicate),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

fun <R, C : MutableCollection<in R>, I : Iterable<*>> NetworkResult<I>.filterIsInstanceTo(
    destination: C,
    klass: Class<R>
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterIsInstanceTo(destination, klass),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, I : Iterable<T>> NetworkResult<I>.filterNot(predicate: (T) -> Boolean): NetworkResult<List<T>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterNot(predicate),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, C : MutableCollection<in T>, I : Iterable<T>> NetworkResult<I>.filterNotTo(
    destination: C,
    predicate: (T) -> Boolean
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterNotTo(destination, predicate),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

fun <T : Any, I : Iterable<T?>> NetworkResult<I>.filterNotNull(): NetworkResult<List<T>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterNotNull(),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

fun <T : Any, C : MutableCollection<in T>, I : Iterable<T?>> NetworkResult<I>.filterNotNullTo(
    destination: C
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.filterNotNullTo(destination),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R, I : Iterable<T>> NetworkResult<I>.map(
    transform: (T) -> R
): NetworkResult<List<R>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.map(transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R, C : MutableCollection<in R>, I : Iterable<T>> NetworkResult<I>.mapTo(
    destination: C,
    transform: (T) -> R
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.mapTo(destination, transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R : Any, I : Iterable<T>> NetworkResult<I>.mapNotNull(
    transform: (T) -> R?
): NetworkResult<List<R>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.mapNotNull(transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R, I : Iterable<T>> NetworkResult<I>.mapIndexed(
    transform: (index: Int, T) -> R
): NetworkResult<List<R>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.mapIndexed(transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R : Any, C : MutableCollection<in R>, I : Iterable<T>> NetworkResult<I>.mapIndexedTo(
    destination: C,
    transform: (index: Int, T) -> R
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.mapIndexedTo(destination, transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R : Any, I : Iterable<T>> NetworkResult<I>.mapIndexedNotNull(
    transform: (index: Int, T) -> R?
): NetworkResult<List<R>> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.mapIndexedNotNull(transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

inline fun <T, R : Any, C : MutableCollection<in R>, I : Iterable<T>> NetworkResult<I>.mapIndexedNotNullTo(
    destination: C,
    transform: (index: Int, T) -> R?
): NetworkResult<C> {
    return when (this) {
        is NetworkResult.Success ->
            NetworkResult.Success(
                data = this.data.mapIndexedNotNullTo(destination, transform),
                code = this.code
            )
        is NetworkResult.Error -> NetworkResult.Error(this.error, this.code)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}
