package ru.netology.nmedia.network.results

import retrofit2.Response
import ru.netology.nmedia.network.exceptions.FailedHttpRequestException
import ru.netology.nmedia.utils.getErrorMessage
import timber.log.Timber

sealed class NetworkResult<T>(
    open val data: T? = null,
    open val code: Int? = null,
    open val error: Throwable? = null,
    val status: NetworkStatus
) {

    data class Success<T>(
        override val data: T,
        override val code: Int? = null
    ) : NetworkResult<T>(status = NetworkStatus.SUCCESS)

    data class Error<T>(
        override val error: Throwable,
        override val code: Int? = null,
    ) : NetworkResult<T>(status = NetworkStatus.ERROR)

    class Loading<T> : NetworkResult<T>(status = NetworkStatus.LOADING)
}

enum class NetworkStatus {
    SUCCESS,
    ERROR,
    LOADING
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    try {
        val response = apiCall()
        if (response.isSuccessful) {
            Timber.d("Response with code ${response.code()} has been received!")
            val body = response.body()
            if (body != null) {
                return NetworkResult.Success(body, response.code())
            }
        }
        return NetworkResult.Error(FailedHttpRequestException(response), response.code())
    } catch (t: Throwable) {
        return NetworkResult.Error(t)
    }
}

suspend fun saveCall(call: suspend () -> Unit): Boolean {
    return try {
        call()
        true
    } catch (t: Throwable) {
        Timber.e("Error occurred: ${t.getErrorMessage()}")
        false
    }
}