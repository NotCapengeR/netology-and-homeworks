package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {

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
}