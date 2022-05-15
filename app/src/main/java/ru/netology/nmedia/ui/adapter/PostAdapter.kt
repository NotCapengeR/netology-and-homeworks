package ru.netology.nmedia.ui.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.PostItemBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.clickWithDebounce
import ru.netology.nmedia.utils.toPostText


interface PostListener {

    fun onAdded(title: String, text: String): Long

    fun onRemoved(id: Long): Boolean

    fun onEdit(id: Long, newText: String): Boolean

    fun onLiked(id: Long): Boolean

    fun onShared(id: Long): Int
}

class PostAdapter(
    private val listener: PostListener
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffUtilCallback) {

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) = with(binding) {
            this.root.tag = post
            tvCommentsCount.text = post.comments.toPostText()
            tvLikesCount.text = post.likes.toPostText()
            tvShareCount.text = post.shared.toPostText()
            tvViewsCount.text = post.views.toPostText()
            tvPostText.text = post.text
            tvPostTitle.text = post.title
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", post.date)
            postAvatar.setImageResource(post.avatarId)
            menuButton.clickWithDebounce {
                listener.onRemoved(post.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    object DiffUtilCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id
    }
}

