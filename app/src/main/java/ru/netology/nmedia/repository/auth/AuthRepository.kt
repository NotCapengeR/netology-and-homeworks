package ru.netology.nmedia.repository.auth

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.safeApiCall
import ru.netology.nmedia.repository.dto.Photo
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {

    suspend fun login(login: String, password: String): NetworkResult<AuthData>

    suspend fun register(login: String, password: String, name: String): NetworkResult<AuthData>

    suspend fun registerWithAvatar(login: String, password: String, name: String, avatar: Photo?): NetworkResult<AuthData>

    fun getAuthId(): Long
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences,
    private val authManager: AuthManager,
    private val service: PostService,
    private val scope: CoroutineScope
) : AuthRepository {

    override suspend fun login(login: String, password: String): NetworkResult<AuthData> {
        val result = safeApiCall { service.login(login, password) }
        if (result is NetworkResult.Success) {
            val data = result.data
            authManager.setAuth(data.id, data.token!!)
        }
        return result
    }

    override suspend fun register(login: String, password: String, name: String): NetworkResult<AuthData> {
        val result = safeApiCall { service.register(login, password, name) }
        if (result is NetworkResult.Success) {
            val data = result.data
            authManager.setAuth(data.id, data.token!!)
        }
        return result
    }

    override suspend fun registerWithAvatar(login: String, password: String, name: String, avatar: Photo?): NetworkResult<AuthData> {
        if (avatar?.file == null) {
            return register(login, password, name)
        }
        val result = safeApiCall { service.registerWithPhoto(
            login.toRequestBody("text/plain".toMediaType()),
            password.toRequestBody("text/plain".toMediaType()),
            name.toRequestBody("text/plain".toMediaType()),
            MultipartBody.Part.createFormData(
            "file", avatar.file.name, avatar.file.asRequestBody()
        )) }
        if (result is NetworkResult.Success) {
            val data = result.data
            authManager.setAuth(data.id, data.token!!)
        }
        return result
    }


    override fun getAuthId(): Long = prefs.getLong(AuthManager.ID_KEY, 0L)
}