package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post

object PostRepositoryImpl : PostRepository {

    private val post = Post(1, "Университет Нетология", "—", 0, 0)


    override fun likePost(post: Post): Boolean {
        if (!post.isLiked) {
            post.likesCount++
        } else {
            post.likesCount--
        }
        post.isLiked = !post.isLiked
        return post.isLiked
    }

    override fun sharePost(post: Post): Int {
        post.shareCount++
        return post.shareCount
    }

    override fun getPost(): Post = post

    override fun getPostLiveData(): LiveData<Post> = MutableLiveData(post)

}