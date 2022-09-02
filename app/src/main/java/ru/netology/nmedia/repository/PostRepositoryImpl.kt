package ru.netology.nmedia.repository

import android.content.SharedPreferences
import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.database.dao.DeletedPostDAO
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.database.entities.DeletedPostEntity
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult
import ru.netology.nmedia.network.results.NetworkResult.Companion.RESPONSE_CODE_OK
import ru.netology.nmedia.repository.auth.AuthManager.Companion.ID_KEY
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Photo
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.utils.Mapper
import ru.netology.nmedia.utils.getErrorMessage
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
    private val dao: PostDAO,
    private val deletedDAO: DeletedPostDAO,
    private val source: RemotePostSource,
) : PostRepository, SyncHelper {

    override val latestPosts: Flow<List<PostResponse>> = source.latestPosts.map { latest ->
        latest.filter { post ->
            dao.getPostById(post.id) == null
        }
    }.catch { Timber.e("Error occurred while parsing newer posts: ${it.getErrorMessage()}") }
        .flowOn(Dispatchers.IO)

    override suspend fun insertLatest(latest: List<PostResponse>): List<PostEntity> {
        return withContext(Dispatchers.IO) {
            latest.map { post -> PostEntity.parser(post) }
                .onEach { post ->
                    dao.insertPost(post)
                }
        }
    }

    override suspend fun calculateDiffAndUpdate(local: PostEntity?, remote: PostResponse?) {
        if (local == null || remote == null || local.id != remote.id) return
        if (local.likes != remote.likes) {
            if ((local.likes - remote.likes == 1 || local.likes - remote.likes == -1) &&
                local.isLiked != remote.isLiked
            ) {
                source.likeById(local.id)
            } else {
                dao.updateLikesCount(local.id, remote.likes)
            }
        }

        if (local.text != remote.text) {
            dao.updateText(local.id, remote.text)
        }
        if (local.title != remote.title) {
            dao.updateTitle(local.id, remote.title)
        }
        if (local.avatar != remote.avatar) {
            dao.updateAvatar(local.id, remote.avatar)
        }
        if (local.attachment != remote.attachment) {
            dao.updateAttachment(
                local.id,
                remote.attachment?.name,
                remote.attachment?.description,
                remote.attachment?.type
            )
        }
        if (Mapper.parseStringToEpoch(local.date) != remote.date) {
            dao.updateDate(local.id, Mapper.parseEpochToAbsolute(remote.date))
        }
    }

    override suspend fun syncDB(
        serverData: Map<Long, PostResponse>,
        localData: Map<Long, PostEntity>
    ) {
        deletedDAO.getAllIds().forEach { id ->
            source.deletePostById(id).also { isSuccess ->
                if (isSuccess) deletedDAO.removeFromDeleted(id)
            }
        }
        serverData.keys.forEach { id ->
            if (!localData.containsKey(id)) {
                dao.insertPost(PostEntity.parser(serverData[id]!!))
            }
        }
        localData.keys.forEach { id ->
            if (!serverData.containsKey(id)) {
                dao.deletePostById(id)
                deletedDAO.removeFromDeleted(id)
            } else {
                calculateDiffAndUpdate(localData[id], serverData[id])
            }
        }
    }

    override suspend fun getDeletedPostsIds(): List<Long> {
        return deletedDAO.getAllIds()
    }

    override fun getAllPosts(): Flow<NetworkResult<List<PostResponse>>> = flow {
        emit(NetworkResult.Loading())
        emit(pingServer())
    }.flowOn(Dispatchers.IO)

    override suspend fun pingServer(): NetworkResult<List<PostResponse>> {
        return source.getAll().also { result ->
            if (result is NetworkResult.Success && result.code == RESPONSE_CODE_OK) {
                syncDB(
                    serverData = result.data.associateBy { response -> response.id },
                    localData = dao.getAllAsList().associateBy { entity -> entity.id }
                )
            }
        }
    }

    override fun getPostsFromDB(): Flow<List<Post>> = dao.getAll()
        .map { Mapper.mapEntitiesToPosts(it) }
        .catch { Timber.e("Error occurred while getting posts from DB: ${it.getErrorMessage()}") }
        .flowOn(Dispatchers.IO)

    override suspend fun getPostById(id: Long): Post? =
        when (val response = source.getPostById(id)) {
            is NetworkResult.Success -> Post.parser(response.data)
            is NetworkResult.Loading -> getPostFromDBById(id)
            is NetworkResult.Error -> getPostFromDBById(id)
        }

    override suspend fun getPostFromDBById(id: Long): Post? {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            Post.parser(dao.getPostById(id))
        }
    }

    override suspend fun getPostsFromDBAsList(): List<Post> {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            Mapper.mapEntitiesToPosts(dao.getAllAsList())
        }
    }


    override suspend fun addPost(title: String, text: String, attachment: Attachment?): Long {
        source.addPost(title, text, attachment).data?.also {
            try {
                dao.addPost(it.id, title, text, avatar = it.avatar, authorId = getAuthId())
            } catch (ex: SQLiteConstraintException) {
                dao.insertPost(
                    PostEntity(
                        id = it.id,
                        title = title,
                        text = text,
                        avatar = it.avatar,
                        attachment = it.attachment,
                        authorId = getAuthId()
                    )
                )
            }
            return it.id
        }
        return 0L
    }

    override suspend fun addPostWithAttachment(title: String, text: String, photo: Photo?): Long {
        return withContext(scope.coroutineContext + Dispatchers.IO) {
            val media = source.uploadImage(photo)
            addPost(title, text, Attachment.attachmentFromMedia(media.data))
        }
    }


    override fun addVideo(url: String, postId: Long) {
    }

    override suspend fun removeLink(id: Long): Boolean {
        return false
        //return dao.removeVideo(id) > 0
    }

    override suspend fun removePost(id: Long): Boolean {
        val post = dao.getPostById(id) ?: return false
        return (dao.deletePostById(id) > 0).also {
            if (it) {
                deletedDAO.insert(DeletedPostEntity.parser(post))
                scope.launch(Dispatchers.IO) {
                    source.deletePostById(id).also { isSuccess ->
                        if (isSuccess) {
                            deletedDAO.removeFromDeleted(id)
                        }
                    }
                }
            }
        }
    }

    override suspend fun editPost(id: Long, newText: String): Boolean {
        return (source.editPost(id, newText) is NetworkResult.Success).also {
            if (it) {
                dao.editPost(id, newText)
            }
        }
    }

    override suspend fun likePost(id: Long): Boolean {
        val post = getPostFromDBById(id) ?: return false
        return if (post.isLiked) {
            dao.dislikePostById(id) > 0
        } else {
            dao.likePostById(id) > 0
        }.also {
            if (it) {
                scope.launch(Dispatchers.IO) {
                    source.likeById(id)
                }
            }
        }
    }

    override suspend fun sharePost(id: Long): Int {
        return 0
//        val post = getPostById(id) ?: return -1
//        val nextValue = post.shared + 1
//        dao.sharePostById(post.id, nextValue)
//        return nextValue
    }

    override suspend fun commentPost(id: Long): Int {
        return 0
//        val post = getPostById(id) ?: return -1
//        val nextValue = post.comments + 1
//        dao.commentPostById(post.id, nextValue)
//        return nextValue
    }

    override fun getAuthId(): Long {
        return prefs.getLong(ID_KEY, 0L)
    }

    private companion object {
        private const val URL_PATTERN: String =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        private val COMPILED_PATTERN: Pattern =
            Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE)
    }
}