package ru.netology.nmedia.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import ru.netology.nmedia.databinding.PostLoadStateDownBinding
import ru.netology.nmedia.ui.base.ItemViewHolder
import ru.netology.nmedia.utils.setDebouncedListener
import ru.netology.nmedia.utils.setVisibility

typealias LoadStateRetry = () -> Unit

class PagingLoadStateAdapter(
    private val listener: LoadStateRetry
): LoadStateAdapter<PagingLoadStateAdapter.PostPagingViewHolder>()  {

    inner class PostPagingViewHolder(
        private val binding: PostLoadStateDownBinding
    ): ItemViewHolder<LoadState>(binding.root) {

        override fun bind(item: LoadState) = with(binding) {
            progress.setVisibility(item is LoadState.Loading)
            retry.setVisibility(item is LoadState.Error)
            retry.setDebouncedListener(100L)  {
                listener.invoke()
            }
        }

    }

    override fun onBindViewHolder(holder: PostPagingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): PostPagingViewHolder {
        return PostPagingViewHolder(
            PostLoadStateDownBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false)
        )
    }
}