package ru.netology.nmedia.repository.auth

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.network.post_api.dto.PushToken
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.network.results.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val service: Lazy<PostService>,
    private val scope: CoroutineScope,
    private val prefs: SharedPreferences
) {
    private val _authData = MutableStateFlow(AuthData.EMPTY)
    val authData = _authData.asStateFlow()

    companion object {
        const val ID_KEY: String = "auth_id_key"
        const val TOKEN_KEY: String = "auth_token_key"
    }

    init {
        val id = prefs.getLong(ID_KEY, 0L)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id != 0L && token != null) {
            _authData.value = AuthData(id, token)
        }
        sendPushToken()
    }

    fun clearAuth() {
        _authData.value = AuthData.EMPTY.also {
            prefs.edit {
                putLong(ID_KEY, 0L)
                putString(TOKEN_KEY, null)
            }
            sendPushToken()
        }
    }

    fun setAuth(id: Long, token: String) {
        _authData.value = AuthData(id, token).also {
            prefs.edit {
                putLong(ID_KEY, id)
                putString(TOKEN_KEY, token)
            }
            sendPushToken()
        }
    }

    fun sendPushToken(token: String? = null) {
        scope.launch(Dispatchers.Default) {
            safeApiCall(Dispatchers.Default) {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                service.get().savePushToken(pushToken)
            }
        }
    }


    fun getAuthId(): Long = prefs.getLong(ID_KEY, 0L)
}

@Parcelize
data class AuthData(
    val id: Long,
    val token: String?
) : Parcelable {
    companion object {
        val EMPTY: AuthData = AuthData(0L, null)
    }
}