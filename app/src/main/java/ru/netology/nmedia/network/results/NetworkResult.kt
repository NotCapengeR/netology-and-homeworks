package ru.netology.nmedia.network.results

import retrofit2.Response
import timber.log.Timber

sealed class NetworkResult<T>(
    open val data: T? = null,
    open val code: Int? = null,
    open val message: String? = null,
    val status: NetworkStatus
) {

    data class Success<T>(
        override val data: T,
        override val code: Int
    ) : NetworkResult<T>(status = NetworkStatus.SUCCESS)

    data class Error<T>(
        override val message: String,
        override val code: Int? = null,
        override val data: T? = null
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
        return NetworkResult.Error(response.message(), response.code())
    } catch (e: Exception) {
        return NetworkResult.Error(e.message ?: e.toString())
    }
}

suspend fun saveCall(call: suspend () -> Unit): Boolean {
    return try {
        call()
        true
    } catch (e: Throwable) {
        Timber.e("Error occurred: ${e.message ?: e.toString()}")
        false
    }
}