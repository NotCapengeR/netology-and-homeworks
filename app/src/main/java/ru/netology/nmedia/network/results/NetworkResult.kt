package ru.netology.nmedia.network.results

import retrofit2.Response

sealed class NetworkResult<T>(
    open val data: T? = null,
    open val message: String? = null,
    val status: NetworkStatus
) {

    data class Success<T>(override val data: T) : NetworkResult<T>(status = NetworkStatus.SUCCESS)

    data class Error<T>(override val message: String) : NetworkResult<T>(status = NetworkStatus.ERROR)

    object Loading : NetworkResult<Any?>(status = NetworkStatus.LOADING)
}

enum class NetworkStatus {
    SUCCESS,
    ERROR,
    LOADING
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            }
        }
        NetworkResult.Error("${response.code()} ${response.message()}")
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: e.toString())
    }
}