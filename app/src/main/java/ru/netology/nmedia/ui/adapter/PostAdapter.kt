package ru.netology.nmedia.ui.adapter

import android.text.format.DateFormat
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostItemBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Post.Companion.POST_DATE_PATTERN
import ru.netology.nmedia.ui.base.BaseViewHolder
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import ru.netology.nmedia.utils.toPostText
import timber.log.Timber


interface PostListener {

    suspend fun onRemoved(id: Long): Boolean

    fun onEdit(id: Long, currentText: String, currentTitle: String): Boolean

    suspend fun onLiked(id: Long): Boolean

    suspend fun onShared(id: Long): Int

    suspend fun onCommented(id: Long): Int

    fun onLinkPressed(url: String)

    suspend fun onLinkRemoved(id: Long): Boolean

    fun onItemPressed(id: Long, currentText: String, currentTitle: String)
}

class PostAdapter(
    private val listener: PostListener
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffUtilCallback), View.OnClickListener {

    private val adapterScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onClick(view: View) {
        val post = view.tag as Post
        when (view.id) {
            R.id.menuButton -> showPopupMenu(view)

            R.id.ivLikes -> adapterScope.launch {
                listener.onLiked(post.id)
            }
            R.id.ivComments -> adapterScope.launch {
                listener.onCommented(post.id)
            }
            R.id.ivShare -> adapterScope.launch {
                listener.onShared(post.id)
            }

            R.id.post_item -> listener.onItemPressed(post.id, post.text, post.title)

            else -> {/* do nothing */}
        }
    }


    private fun showPopupMenu(view: View) {
        val context = view.context
        val popupMenu = PopupMenu(context, view)
        val post = view.tag as Post

        popupMenu.menu.add(0, EDIT_ID, Menu.NONE, context.getString(R.string.edit))
        popupMenu.menu.add(0, REMOVE_ID, Menu.NONE, context.getString(R.string.post_remove))

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                REMOVE_ID -> adapterScope.launch {
                    listener.onRemoved(post.id)
                }

                EDIT_ID -> listener.onEdit(post.id, post.text, post.title)

            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    inner class PostViewHolder(private val binding: PostItemBinding) :
        BaseViewHolder(binding.root) {

        fun bind(post: Post) = with(binding) {
            postItem.tag = post
            menuButton.tag = post
            ivLikes.tag = post
            ivComments.tag = post
            ivShare.tag = post
            ivComments.text = post.comments.toPostText()
            ivLikes.text = post.likes.toPostText()
            ivShare.text = post.shared.toPostText()
            tvViewsCount.text = post.views.toPostText()
            tvPostText.text = post.text
            tvPostTitle.text = post.title
            tvDateTime.text = DateFormat.format(POST_DATE_PATTERN, post.date)
            ivPostAvatar.setImageResource(post.avatarId)
            yTLayout.setVisibility(post.video != null)
            Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
            if (post.video != null) {
                Timber.d("YT video id: ${post.video.id}")
                ytVideoDuration.text = post.video.duration
                ytAuthor.text = post.video.author
                ytTitle.text = post.video.title
                Glide.with(root.context)
                    .load(post.video.thumbnailUrl)
                    .centerCrop()
                    .into(ytThumbnail)
                ytThumbnail.setDebouncedListener {
                    listener.onLinkPressed("$YOUTUBE_URL${post.video.id}")
                }
                ytCancel.setDebouncedListener {
                    scope.launch {
                        listener.onLinkRemoved(post.id)
                    }
                }
            }
            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            ivLikes.isChecked = post.isLiked
            postItem.setDebouncedListener(onClickListener = this@PostAdapter)
            ivLikes.setDebouncedListener(50L, this@PostAdapter)
            menuButton.setDebouncedListener(200L, this@PostAdapter)
            ivShare.setDebouncedListener(50L, this@PostAdapter)
            ivComments.setDebouncedListener(50L, this@PostAdapter)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {
        val binding =
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    object DiffUtilCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem
    }

    companion object {
        const val YOUTUBE_URL: String = "https://www.youtube.com/watch?v="
        private const val REMOVE_ID: Int = 1
        private const val EDIT_ID: Int = 2
    }
}

