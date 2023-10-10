package ru.netologia.nmedia.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netologia.nmedia.databinding.CardPostBinding
import ru.netologia.nmedia.dto.Post

class PostsAdapter(
    private val onIteractionLister: OnIteractionLister
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostViewHolder(binding, onIteractionLister)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
//        val post = list[position]
        val post = getItem(position)
        holder.bind(post)
    }

}