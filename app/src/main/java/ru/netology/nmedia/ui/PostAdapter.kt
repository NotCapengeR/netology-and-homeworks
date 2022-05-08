package ru.netology.nmedia.ui

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.PostItemBinding
import ru.netology.nmedia.utils.toPostText
import java.time.format.DateTimeFormatter

class PostAdapter(
    private val posts: List<Post>
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffUtilCallback) {

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, H:mm")

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: Post) {
            with(binding) {
                tvCommentsCount.text = post.comments.toPostText()
                tvLikesCount.text = post.likes.toPostText()
                tvShareCount.text = post.shared.toPostText()
                tvViewsCount.text = post.views.toPostText()
                tvPostText.text = post.text
                tvPostTitle.text = post.title
                tvDateTime.text = dateFormatter.format(post.date)
                postAvatar.setImageResource(post.avatarId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemBinding =
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(itemBinding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    object DiffUtilCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id

    }
}