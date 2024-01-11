package ru.netologia.nmedia.viewmodel

import androidx.recyclerview.widget.DiffUtil
import ru.netologia.nmedia.dto.FeedItem
import ru.netologia.nmedia.dto.Post

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if(oldItem::class != newItem::class)
            return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}