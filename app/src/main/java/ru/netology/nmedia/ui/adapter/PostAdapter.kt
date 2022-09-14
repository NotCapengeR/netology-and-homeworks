package ru.netology.nmedia.ui.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostItemBinding
import ru.netology.nmedia.databinding.PostSeparatorBinding
import ru.netology.nmedia.repository.dto.Attachment
import ru.netology.nmedia.repository.dto.Post
import ru.netology.nmedia.repository.dto.Post.Companion.ATTACHMENTS_BASE_URL
import ru.netology.nmedia.repository.dto.Post.Companion.AVATARS_BASE_URL
import ru.netology.nmedia.repository.dto.PostAdapterEntity
import ru.netology.nmedia.repository.dto.PostTimeSeparator
import ru.netology.nmedia.ui.base.ItemViewHolder
import ru.netology.nmedia.utils.Mapper
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility
import ru.netology.nmedia.utils.toPostText
import timber.log.Timber


interface PostListener {

    fun onRemoved(id: Long)

    fun onEdit(id: Long, currentText: String, currentTitle: String): Boolean

    fun onLiked(id: Long)

    fun onShared(text: String)

    fun onCommented(id: Long)

    fun onLinkPressed(url: String)

    fun onLinkRemoved(id: Long)

    fun onItemPressed(id: Long, currentText: String, currentTitle: String)

    fun onImageDetails(id: Long, uri: String)
}

class PostAdapter(
    private val listener: PostListener
) : PagingDataAdapter<PostAdapterEntity, RecyclerView.ViewHolder>(DiffUtilCallback),
    View.OnClickListener {

    override fun onClick(view: View) {
        val post = view.tag as Post
        when (view.id) {
            R.id.menuButton -> showPopupMenu(view)

            R.id.ivLikes -> listener.onLiked(post.id)

            R.id.ivComments -> listener.onCommented(post.id)

            R.id.ivShare -> listener.onShared(post.text)

            R.id.ivAttachment -> {
                if (post.attachment != null) {
                    listener.onImageDetails(
                        post.id,
                        "$ATTACHMENTS_BASE_URL${post.attachment.name}"
                    )
                }
            }


            R.id.post_item -> listener.onItemPressed(post.id, post.text, post.title)

            else -> {/* do nothing */
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostTimeSeparator -> SEPARATOR_TYPE
            is Post -> POST_TYPE
            null -> NULL_TYPE
        }
    }


    private fun showPopupMenu(view: View) {
        val context = view.context
        val popupMenu = PopupMenu(context, view)
        val post = view.tag as Post
        if (!post.isOwner) return

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
        ItemViewHolder<Post>(binding.root) {

        override fun bind(item: Post) = with(binding) {
            postItem.tag = item
            menuButton.tag = item
            ivLikes.tag = item
            ivComments.tag = item
            ivShare.tag = item
            ivAttachment.tag = item
            ivComments.text = item.comments.toPostText()
            ivLikes.text = item.likes.toPostText()
            ivShare.text = item.shared.toPostText()
            tvViewsCount.text = item.views.toPostText()
            tvPostText.text = item.text
            tvPostTitle.text = item.title
            tvDateTime.text = Mapper.parseEpochSeconds(item.date)
            Timber.d("Post ${item.id}: ${tvDateTime.text}")
            if (item.avatar.isNotBlank() && item.avatar.isNotEmpty()) {
                Glide.with(root.context)
                    .load("$AVATARS_BASE_URL${item.avatar}")
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .error(R.drawable.alert_circle)
                    .circleCrop()
                    .timeout(10_000)
                    .into(ivPostAvatar)
            } else {
                Glide.with(root.context).clear(ivPostAvatar)
            }
            ivAttachment.setVisibility(item.attachment != null)
            if (item.attachment != null && item.attachment.type == Attachment.AttachmentType.IMAGE) {
                Glide.with(root.context)
                    .load("$ATTACHMENTS_BASE_URL${item.attachment.name}")
                    .placeholder(R.drawable.push_nmedia)
                    .error(ColorDrawable(Color.RED))
                    .centerCrop()
                    .timeout(10_000)
                    .into(ivAttachment)
            }
            if (item.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            menuButton.setVisibility(item.isOwner).also {
                Timber.d("Item: ${item.isOwner}")
            }
            ivLikes.isChecked = item.isLiked
            postItem.setDebouncedListener(50L, this@PostAdapter)
            ivLikes.setDebouncedListener(50L, this@PostAdapter)
            menuButton.setDebouncedListener(200L, this@PostAdapter)
            ivShare.setDebouncedListener(50L, this@PostAdapter)
            ivComments.setDebouncedListener(50L, this@PostAdapter)
            ivAttachment.setDebouncedListener(50L, this@PostAdapter)
        }

        fun bindLikes(post: Post) = with(binding) {
            ivLikes.tag = post
            ivLikes.text = post.likes.toPostText()
            if (post.isLiked) {
                ivLikes.setIconResource(R.drawable.heart)
            } else {
                ivLikes.setIconResource(R.drawable.heart_outline)
            }
            ivLikes.isChecked = post.isLiked
        }

        fun bindText(post: Post) = with(binding) {
            tvPostText.text = post.text
        }

        fun bindMenu(item: Post) = with(binding) {
            menuButton.tag = item
            menuButton.setVisibility(item.isOwner)
            menuButton.setDebouncedListener(200L, this@PostAdapter)
        }

    }

    inner class SeparatorViewHolder(private val binding: PostSeparatorBinding) :
        ItemViewHolder<PostTimeSeparator>(binding.root) {
        override fun bind(item: PostTimeSeparator) = with(binding) {
            tvDateTime.text = item.time
            progress.setVisibility(false)
        }

        fun bindNull() = with(binding) {
            progress.setVisibility(true)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            POST_TYPE -> PostViewHolder(
                PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            SEPARATOR_TYPE, NULL_TYPE -> {
                SeparatorViewHolder(
                    PostSeparatorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                )
            }
            else -> throw IllegalArgumentException("Unknown view type given: $viewType")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        return when (val type = getItemViewType(position)) {
            POST_TYPE -> (holder as PostViewHolder).bind(item as Post)
            SEPARATOR_TYPE -> (holder as SeparatorViewHolder).bind(item as PostTimeSeparator)
            NULL_TYPE -> (holder as SeparatorViewHolder).bindNull()
            else -> {
                Timber.d("Unknown view type given: $type")
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any?>
    ) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }

        payloads.firstOrNull().let { payload ->
            if (payload !is List<*>) {
                return super.onBindViewHolder(holder, position, payloads)
            }
            if (payload.contains(LIKES) && holder is PostViewHolder) {
                holder.bindLikes(
                    getItem(position) as? Post ?: return super.onBindViewHolder(
                        holder,
                        position,
                        payloads
                    )
                )
            }
            if (payload.contains(TEXT) && holder is PostViewHolder) {
                holder.bindText(
                    getItem(position) as? Post ?: return super.onBindViewHolder(
                        holder,
                        position,
                        payloads
                    )
                )
            }
            if (payload.contains(SEPARATOR) && holder is SeparatorViewHolder) {
                holder.bind(
                    getItem(position) as? PostTimeSeparator ?: return super.onBindViewHolder(
                        holder,
                        position,
                        payloads
                    )
                )
            }
            if (payload.contains(IS_OWNER) && holder is PostViewHolder) {
                holder.bindMenu(
                    getItem(position) as? Post ?: return super.onBindViewHolder(
                        holder,
                        position,
                        payloads
                    )
                )
            }
        }
    }


    private object DiffUtilCallback : DiffUtil.ItemCallback<PostAdapterEntity>() {
        override fun areItemsTheSame(
            oldItem: PostAdapterEntity,
            newItem: PostAdapterEntity
        ): Boolean {
            val isSameSeparator = oldItem is PostTimeSeparator && newItem is PostTimeSeparator &&
                    oldItem.time == newItem.time
            val isSamePost = oldItem is Post && newItem is Post &&
                    oldItem.id == newItem.id
            return isSameSeparator || isSamePost
        }

        override fun areContentsTheSame(
            oldItem: PostAdapterEntity,
            newItem: PostAdapterEntity
        ): Boolean =
            oldItem == newItem

        override fun getChangePayload(
            oldItem: PostAdapterEntity,
            newItem: PostAdapterEntity
        ): List<Int> {
            val payloads: MutableList<Int> = mutableListOf()
            if (oldItem is PostTimeSeparator || newItem is PostTimeSeparator) {
                if (oldItem is PostTimeSeparator && newItem is PostTimeSeparator) {
                    payloads.add(SEPARATOR)
                    return payloads
                }
                return emptyList()
            } else {
                if ((newItem as Post).likes != (oldItem as Post).likes) {
                    payloads.add(LIKES)
                }
                if (newItem.text != oldItem.text) {
                    payloads.add(TEXT)
                }
                if (newItem.isOwner != oldItem.isOwner) {
                    payloads.add(IS_OWNER)
                    Timber.d("New: ${newItem.isOwner}, Old: ${oldItem.isOwner}")
                }
                return payloads
            }
        }
    }

    private companion object {
        private const val REMOVE_ID: Int = 1
        private const val EDIT_ID: Int = 2

        private const val LIKES: Int = 0
        private const val TEXT: Int = 1
        private const val SEPARATOR: Int = 2
        private const val IS_OWNER: Int = 3

        private const val SEPARATOR_TYPE: Int = 2
        private const val POST_TYPE: Int = 1
        private const val NULL_TYPE: Int = 0
    }
}

