package ru.netology.nmedia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavDeepLinkBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.di.modules.FirebaseModule.Companion.FIREBASE_TAG
import ru.netology.nmedia.network.post_api.dto.PushMessage
import ru.netology.nmedia.repository.auth.AuthManager
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.fromJsonOrNull
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    @Inject lateinit var gson: Gson
    @Inject lateinit var manager: AuthManager

    override fun onCreate() {
        super.onCreate()
        getAppComponent().inject(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //like channel
            val nameLike = getString(R.string.channel_remote_name_like)
            val descriptionLike = getString(R.string.channel_remote_name_like_descriptions)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val likeChannel = NotificationChannel(LIKE_CHANNEL_ID, nameLike, importance).apply {
                description = descriptionLike
            }

            //new posts channel
            val postName = getString(R.string.channel_remote_name_new_posts)
            val descriptionPosts = getString(R.string.channel_remote_name_new_posts_descriptions)
            val postChannel =
                NotificationChannel(TEST_CHANNEL_ID, postName, importance).apply {
                    description = descriptionPosts
                }

            //test channel

            val testName = "Just for debug"
            val testDescription = "For developers"
            val testChannel =
                NotificationChannel(NEW_POSTS_CHANNEL_ID, testName, importance).apply {
                    description = testDescription
                }


            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(likeChannel, postChannel, testChannel))
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        firebaseLoggerDebug("New message (${message.messageId}) has been received: \n${message.data}")
        gson.fromJsonOrNull(message.data[CONTENT], PushMessage::class.java)?.let { push ->
            val authId = manager.getAuthId()
            when (val it = push.recipientId) {

                null, authId -> {
                    testPush(push.content)
                }

                0L -> {
                    if (authId != it) {
                        manager.sendPushToken()
                    }
                }
                else -> {
                    manager.sendPushToken()
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        firebaseLoggerDebug("New token has been received: $token")
        manager.sendPushToken(token)

    }

    private fun testPush(content: String?) {
        if (content == null) return
        val intent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.mainFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, LIKE_CHANNEL_ID)
            .setSmallIcon(R.drawable.push_nmedia)
            .setContentTitle("Push test")
            .setChannelId(TEST_CHANNEL_ID)
            .setContentText(content)
            .setLargeIcon(getDrawable(R.drawable.ic_netology)?.toBitmap())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }

    private fun handleLike(like: Like?) {
        if (like == null) return
        val intent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.mainFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, LIKE_CHANNEL_ID)
            .setSmallIcon(R.drawable.heart)
            .setContentTitle(
                getString(R.string.notification_user_liked, like.userName, like.postAuthor)
            )
            .setChannelId(LIKE_CHANNEL_ID)
            .setContentText("This is just a text...")
            .setLargeIcon(getDrawable(R.drawable.ic_netology)?.toBitmap())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)

    }

    private fun handleNewPost(post: NewPost?) {
        if (post == null) return
        val intent = NavDeepLinkBuilder(this)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.mainFragment)
            .createPendingIntent()

        val notification = NotificationCompat.Builder(this, NEW_POSTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.pencil)
            .setContentTitle(getString(R.string.notification_new_post, post.author))
            .setContentText(post.text.take(20))
            .setChannelId(NEW_POSTS_CHANNEL_ID)
            .setStyle(NotificationCompat.BigTextStyle().bigText(post.text.drop(20)))
            .setLargeIcon(getDrawable(R.drawable.ic_netology)?.toBitmap())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(intent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(100_000), notification)
    }

    private enum class ActionType {
        LIKE, NEW_POST;

        companion object {
            fun valueOfOrNull(value: String): ActionType? {
                return try {
                    valueOf(value)
                } catch (ex: IllegalArgumentException) {
                    null
                }
            }
        }
    }

    private fun firebaseLoggerDebug(message: String) = Timber.tag(FIREBASE_TAG).d(message)

    private fun firebaseLoggerError(message: String) = Timber.tag(FIREBASE_TAG).e(message)

    private companion object {
        private const val LIKE_CHANNEL_ID: String = "like_channel_id"
        private const val NEW_POSTS_CHANNEL_ID = "new_posts_channel_id"
        private const val TEST_CHANNEL_ID: String = "test_channel_id"
        private const val RECIPIENT_ID: String = "recipientId"
        private const val ACTION: String = "action"
        private const val CONTENT: String = "content"
    }
}