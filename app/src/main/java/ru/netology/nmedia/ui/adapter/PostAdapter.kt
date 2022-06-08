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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostItemBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import ru.netology.nmedia.utils.toPostText
import timber.log.Timber


interface PostListener {

    fun onAdded(title: String, text: String): Long

    fun onRemoved(id: Long): Boolean

    fun onEdit(id: Long, currentText: String, currentTitle: String): Boolean

    fun onLiked(id: Long): Boolean

    fun onShared(id: Long): Int

    fun onCommented(id: Long): Int

    fun onPostMoved(id: Long, movedBy: Int): Boolean

    fun onLinkPressed(url: String)

    fun onLinkRemoved(id: Long): Boolean
}

class PostAdapter(
    private val listener: PostListener
) : ListAdapter<Post, PostAdapter.PostViewHolder>(DiffUtilCallback), View.OnClickListener {

    override fun onClick(view: View) {
        val post = view.tag as Post
        when (view.id) {
            R.id.menuButton -> showPopupMenu(view)

            R.id.ivLikes -> listener.onLiked(post.id)

            R.id.ivComments -> listener.onCommented(post.id)

            R.id.ivShare -> listener.onShared(post.id)

            else -> {/* do nothing */}
        }
    }

    private fun showPopupMenu(view: View) {
        val context = view.context
        val popupMenu = PopupMenu(context, view)
        val post = view.tag as Post
        val position = currentList.indexOf(post)
        Timber.d("Position: $position, id: ${post.id}")

        popupMenu.menu.add(0, EDIT_ID, Menu.NONE, context.getString(R.string.edit))
        popupMenu.menu.add(0, REMOVE_ID, Menu.NONE, context.getString(R.string.post_remove))
        popupMenu.menu
            .add(0, MOVE_UP_ID, Menu.NONE, context.getString(R.string.post_move_up)).apply {
                isEnabled = position > 0
            }
        popupMenu.menu
            .add(0, MOVE_DOWN_ID, Menu.NONE, context.getString(R.string.post_mode_down)).apply {
                isEnabled = position < this@PostAdapter.currentList.size - 1
            }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                REMOVE_ID -> {
                    Timber.d("Position: $position")
                    listener.onRemoved(post.id)
                }

                EDIT_ID -> listener.onEdit(post.id, post.text, post.title)

                MOVE_DOWN_ID -> listener.onPostMoved(post.id, -1)

                MOVE_UP_ID -> listener.onPostMoved(post.id, 1)
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    inner class PostViewHolder(private val binding: PostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) = with(binding) {
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
            tvDateTime.text = DateFormat.format("d MMMM yyyy, HH:mm", post.date)
            ivPostAvatar.setImageResource(post.avatarId)
            yTLayout.setVisibility(post.video != null)
            Linkify.addLinks(tvPostText, Linkify.WEB_URLS)
            if (post.video != null) {
                val video = post.video.items.first()
                val thumbnail = video.snippet.thumbnails.thumbnail
                ytVideoDuration.text =
                    video.contentDetails.duration
                        .replace("PT", "")
                        .replace('S', ' ')
                        .replace('H', ':')
                        .replace('M', ':')
                ytAuthor.text = video.snippet.channelTitle
                ytTitle.text = video.snippet.title
                Glide.with(root.context)
                    .load(thumbnail.url)
                    .centerCrop()
                    .into(ytThumbnail)
                ytThumbnail.setDebouncedListener {
                    listener.onLinkPressed("$YOUTUBE_URL${video.id}")
                }
                ytCancel.setDebouncedListener {
                    listener.onLinkRemoved(post.id)
                }
            }
            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            ivLikes.isChecked = post.isLiked
            ivLikes.setDebouncedListener(50L, this@PostAdapter)
            menuButton.setDebouncedListener(200L, this@PostAdapter)
            ivShare.setDebouncedListener(50L, this@PostAdapter)
            ivComments.setDebouncedListener(50L, this@PostAdapter)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostAdapter.PostViewHolder {
        val binding =
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Timber.tag("questions")
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
            oldItem.id == newItem.id
    }

    companion object {
        const val YOUTUBE_URL: String = "https://www.youtube.com/watch?v="
        private const val REMOVE_ID: Int = 1
        private const val MOVE_UP_ID: Int = 2
        private const val MOVE_DOWN_ID: Int = 3
        private const val EDIT_ID: Int = 4
    }
}

