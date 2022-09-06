package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import ru.netology.nmedia.database.PostDB
import ru.netology.nmedia.database.dao.PostDAO
import ru.netology.nmedia.database.dao.PostRemoteKeyDao
import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.database.entities.PostRemoteKeyEntity
import ru.netology.nmedia.network.exceptions.FailedHttpRequestException
import ru.netology.nmedia.network.post_api.service.PostService
import ru.netology.nmedia.utils.getErrorMessage
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator @Inject constructor(
    private val service: PostService,
    private val db: PostDB,
    private val dao: PostDAO,
    private val remoteKeyDao: PostRemoteKeyDao
) : RemoteMediator<Int, PostEntity>() {


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            Timber.d(loadType.name)
            val response = when (loadType) {
                LoadType.REFRESH -> service.getLatest(state.config.initialLoadSize)
                LoadType.PREPEND -> {
                    val id = remoteKeyDao.max() ?: return MediatorResult.Success(
                        endOfPaginationReached = false
                    )
                    service.getAfter(id, state.config.pageSize)
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
            Timber.d(body.size.toString())
            Timber.d(body.map { it.id }.toString())

            val isEnd = body.isEmpty()
            db.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        remoteKeyDao.removeAll()
                        remoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.AFTER,
                                    id = body.first().id,
                                ),
                                PostRemoteKeyEntity(
                                    type = PostRemoteKeyEntity.KeyType.BEFORE,
                                    id = body.last().id,
                                ),
                            )
                        )
                        dao.removeAll()
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
                dao.insertAll(body.map { PostEntity.parser(it) })
            }
            return MediatorResult.Success(endOfPaginationReached = body.isEmpty())
        } catch (t: Throwable) {
            Timber.d(t.getErrorMessage())
            return MediatorResult.Error(t)
        }
    }
}