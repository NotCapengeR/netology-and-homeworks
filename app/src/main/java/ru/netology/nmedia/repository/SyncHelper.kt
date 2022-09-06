package ru.netology.nmedia.repository

import ru.netology.nmedia.database.entities.PostEntity
import ru.netology.nmedia.network.post_api.dto.PostResponse
import ru.netology.nmedia.network.results.NetworkResult

interface SyncHelper {

    suspend fun syncDB()

    suspend fun calculateDiffAndUpdate(
        local: PostEntity?,
        remote: PostResponse?
    )

    //suspend fun pingServer(): NetworkResult<List<PostResponse>>
}