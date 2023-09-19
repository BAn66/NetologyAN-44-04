package ru.netologia.nmedia.viewmodel


import android.widget.PopupMenu
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.CardPostBinding
import ru.netologia.nmedia.dto.Post

class PostViewHolder(
    private val binding: CardPostBinding, private val onIteractionLister: OnIteractionLister

) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content

            btnLike.text = eraseZero(post.likes)
            btnLike.isChecked = post.likedByMe
//            btnLike.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
//            countOfLiked.text = eraseZero(post.likes)
            btnShare.text = eraseZero(post.shares)
            btnViews.text = eraseZero(post.views)

            if(post.video == ""){
              videoGroup.visibility = Group.GONE
            } else {
                videoGroup.visibility = Group.VISIBLE
            }


            play.setOnClickListener {
                println("videogroup clicked")
                onIteractionLister.playVideo(post)

            }
            videoHolder.setOnClickListener {
                println("videogroup clicked")
                onIteractionLister.playVideo(post)
            }
            videoContent.setOnClickListener {
                println("videogroup clicked")
                onIteractionLister.playVideo(post)
            }

            btnLike.setOnClickListener {
                println("like clicked")
                onIteractionLister.like(post)
            }

            btnShare.setOnClickListener {
                println("share clicked")
                onIteractionLister.share(post)
            }

            content.setOnClickListener {
                println("content clicked")
                onIteractionLister.openPost(post)

            }
            postLayout.setOnClickListener { onIteractionLister.openPost(post) }
            avatar.setOnClickListener { onIteractionLister.openPost(post) }
            author.setOnClickListener { onIteractionLister.openPost(post) }
            published.setOnClickListener { onIteractionLister.openPost(post) }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onIteractionLister.remove(post) //вызов колбэка
                                true
                            }

                            R.id.edit -> {
                                onIteractionLister.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

        }
    }
}

private fun eraseZero(number: Long): String {
    var s = ""

    when (number) {
        in 0..999 -> s = number.toString()
        in 1000..9999 -> s =
            (number.toDouble() / 1000).toString().substring(0, number.toString().length - 1)
                .replace(".0", "") + "K"

        in 10000..999999 -> s =
            number.toString().substring(0, number.toString().length - 3) + "K"

        in 1000000..Int.MAX_VALUE -> s =
            (number.toDouble() / 1000000).toString().substring(0, number.toString().length - 4)
                .replace(".0", "") + "M"
    }
    return s
}
