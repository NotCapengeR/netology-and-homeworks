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
import ru.netology.nmedia.ui.activity.MainActivity
import ru.netology.nmedia.utils.getAppComponent
import ru.netology.nmedia.utils.fromJsonOrNull
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    @Inject lateinit var gson: Gson

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
                NotificationChannel(NEW_POSTS_CHANNEL_ID, postName, importance).apply {
                    description = descriptionPosts
                }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannels(listOf(likeChannel, postChannel))
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("New message has been received: ${message.messageId}, ${message.data}")
        message.data[ACTION]?.let {

            when (ActionType.valueOfOrNull(it)) {

                ActionType.LIKE -> {
                    handleLike(gson.fromJsonOrNull(message.data[CONTENT], Like::class.java))
                }

                ActionType.NEW_POST -> {
                    handleNewPost(gson.fromJsonOrNull(message.data[CONTENT], NewPost::class.java))
                }

                null -> Timber.e("Received unknown action type from the server: $it")
            }
        }
    }

    override fun onNewToken(token: String) {
        Timber.d("New tokes has been received: $token")
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
            .setContentText("This is just a text...")
            .setLargeIcon(getDrawable(R.drawable.ic_launcher_foreground)?.toBitmap())
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
            .setStyle(NotificationCompat.BigTextStyle().bigText(post.text.drop(20)))
            .setLargeIcon(getDrawable(R.drawable.ic_launcher_foreground)?.toBitmap())
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

    private companion object {
        private const val LIKE_CHANNEL_ID: String = "like_channel_id"
        private const val NEW_POSTS_CHANNEL_ID = "new_posts_channel_id"
        private const val ACTION: String = "action"
        private const val CONTENT: String = "content"
    }
}