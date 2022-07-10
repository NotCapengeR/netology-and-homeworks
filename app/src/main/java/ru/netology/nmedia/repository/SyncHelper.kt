package ru.netology.nmedia.repository

import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse

interface SyncHelper {

    suspend fun syncDB(
        serverData: Map<Long, PostResponse>,
        localData: Map<Long, PostEntity>
    )

    suspend fun calculateDiffAndUpdate(
        local: PostEntity?,
        remote: PostResponse?
    )
}