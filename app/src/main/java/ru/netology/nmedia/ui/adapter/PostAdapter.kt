package ru.netology.nmedia.ui.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostItemBinding
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.Post.Companion.ATTACHMENTS_BASE_URL
import ru.netology.nmedia.repository.dto.Post.Companion.AVATARS_BASE_URL
import ru.netology.nmedia.ui.base.BaseViewHolder
import ru.netology.nmedia.utils.Mapper
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import ru.netology.nmedia.utils.toPostText
import timber.log.Timber


interface PostListener {

    fun onRemoved(id: Long)

    fun onEdit(id: Long, currentText: String, currentTitle: String): Boolean

    fun onLiked(id: Long)

    fun onShared(id: Long)

    fun onCommented(id: Long)

    fun onLinkPressed(url: String)

    fun onLinkRemoved(id: Long)

    fun onItemPressed(id: Long, currentText: String, currentTitle: String)
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


            R.id.post_item -> listener.onItemPressed(post.id, post.text, post.title)

            else -> {/* do nothing */
            }
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
                REMOVE_ID -> listener.onRemoved(post.id)


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
            tvDateTime.text = Mapper.parseEpochSeconds(post.date)
            Timber.d("Post ${post.id}: ${tvDateTime.text}")
            if (post.avatar.isNotBlank() && post.avatar.isNotEmpty()) {
                Glide.with(root.context)
                    .load("$AVATARS_BASE_URL${post.avatar}")
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .error(R.drawable.alert_circle)
                    .circleCrop()
                    .timeout(10_000)
                    .into(ivPostAvatar)
            } else {
                Glide.with(root.context).clear(ivPostAvatar)
            }
            ivAttachment.setVisibility(post.attachment != null)
            if (post.attachment != null && post.attachment.type == Attachment.AttachmentType.IMAGE) {
                Glide.with(root.context)
                    .load("$ATTACHMENTS_BASE_URL${post.attachment.name}")
                    .placeholder(R.drawable.play)
                    .error(ColorDrawable(Color.RED))
                    .centerCrop()
                    .timeout(10_000)
                    .into(ivAttachment)
            }
            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            ivLikes.isChecked = post.isLiked
            postItem.setDebouncedListener(50L, this@PostAdapter)
            ivLikes.setDebouncedListener(50L, this@PostAdapter)
            menuButton.setDebouncedListener(200L, this@PostAdapter)
            ivShare.setDebouncedListener(50L, this@PostAdapter)
            ivComments.setDebouncedListener(50L, this@PostAdapter)
        }

        fun bindLikes(post: Post) = with(binding) {
            ivLikes.text = post.likes.toPostText()
            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            ivLikes.isChecked = post.isLiked
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

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any?>
    ) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }

        val payload = payloads.first() as List<*>
        if (payload.contains(LIKES)) {
            holder.bindLikes(getItem(position))
        }
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    private object DiffUtilCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem == newItem

        override fun getChangePayload(oldItem: Post, newItem: Post): List<Int> {
            val payloads: MutableList<Int> = mutableListOf()
            if (newItem.likes != oldItem.likes) {
                payloads.add(LIKES)
            }
            if (oldItem.date != newItem.date) {
                DATE
            }
            return payloads
        }
    }
    private companion object {
        private const val REMOVE_ID: Int = 1
        private const val EDIT_ID: Int = 2

        private const val LIKES: Int = 0
        private const val AVATAR: Int = 1
        private const val DATE: Int = 2
    }
}

