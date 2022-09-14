package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.database.PostDB
import ru.netology.nmedia.database.dao.DeletedPostDAO
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.database.dao.PostRemoteKeyDao
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.database.entities.PostRemoteKeyEntity
import ru.netology.nmedia.network.exceptions.FailedHttpRequestException
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.utils.getErrorMessage
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.absoluteValue

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val service: PostService,
    private val db: PostDB,
    private val dao: PostDAO,
    private val source: RemotePostSource,
    private val deletedDao: DeletedPostDAO,
    private val remoteKeyDao: PostRemoteKeyDao
) : RemoteMediator<Int, PostEntity>(), SyncHelper {


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            Timber.d(loadType.name)
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    remoteKeyDao.getAfter()?.let { id ->
                        service.getAfter(id, state.config.initialLoadSize)
                    } ?: service.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    return MediatorResult.Success(endOfPaginationReached = true)
//                    val id = remoteKeyDao.getAfter() ?: return MediatorResult.Success(
//                        endOfPaginationReached = false
//                    )
//                    service.getAfter(id, state.config.pageSize)
                }
                LoadType.APPEND -> {
                    val id = remoteKeyDao.getBefore() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getBefore(id, state.config.pageSize)
                }
            }
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                throw FailedHttpRequestException(response)
            }
            val entities = getEntities(body)
            Timber.d(body.map { it.id }.toString())

            val isEnd = body.isEmpty()
            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        remoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.AFTER,
                                id = body.first().id,
                            ),
                            PostRemoteKeyEntity(
                                type = PostRemoteKeyEntity.KeyType.BEFORE,
                                id = body.last().id,
                            )
                        )
                        //dao.removeAll()
                    }
                    LoadType.PREPEND -> {
                        if (!isEnd) {
                            remoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id
                                )
                            )
                        }
                    }
                    LoadType.APPEND -> {
                        if (!isEnd) {
                            remoteKeyDao.insert(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    id = body.last().id
                                )
                            )
                        }
                    }
                }
                dao.insertAll(body
                    .map { PostEntity.parser(it) }
                    .filter { entity ->
                        deletedDao.getPostById(entity.id) == null
                    }
                )
                syncDB()
                if (entities.isNotEmpty()) {
                    body.forEach { response ->
                        calculateDiffAndUpdate(entities[response.id], response)
                    }
                }
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (t: Throwable) {
            Timber.d(t.getErrorMessage())
            return MediatorResult.Error(t)
        }
    }

    override suspend fun syncDB() {
        deletedDao.getAllIds().forEach { id ->
            source.deletePostById(id).also { isSuccess ->
                if (isSuccess) {
                    deletedDao.removeFromDeleted(id)
                }
            }
        }
    }

    private suspend fun getEntities(responses: List<PostResponse>): Map<Long, PostEntity> {
        val entities: HashMap<Long, PostEntity> = HashMap()
        responses.forEach { response ->
            dao.getPostById(response.id)?.let { entity ->
                entities[entity.id] = entity
            }
        }
        return entities
    }


    override suspend fun calculateDiffAndUpdate(local: PostEntity?, remote: PostResponse?) {
        if (local == null || remote == null || local.id != remote.id) return
        if (local.likes != remote.likes) {
            if (((local.likes - remote.likes).absoluteValue == 1 && local.isLiked != remote.isLiked)) {
                source.likeById(local.id).let { result ->
                    result.data?.let { response ->
                        dao.setLikes(response.id, response.likes, response.isLiked)
                    }
                }
            }
        }
    }
}