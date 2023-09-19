package ru.netologia.nmedia.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.netologia.nmedia.databinding.CardPostBinding
import ru.netologia.nmedia.dto.Post


//для еденичный функций норм, но если действий в адаптаре много, лучше интерфейс использовать
//typealias OnLikeListener = (post: Post) -> Unit //Тип для callback, просто синоним который будет вызван в адаптаре
//typealias OnShareListener = (post: Post) -> Unit
//typealias OnRemoveListener = (post: Post) -> Unit

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