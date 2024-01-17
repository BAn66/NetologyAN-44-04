package ru.netologia.nmedia.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import ru.netologia.nmedia.BuildConfig
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.CardAdBinding
import ru.netologia.nmedia.databinding.CardPostBinding
import ru.netologia.nmedia.dto.Ad
import ru.netologia.nmedia.dto.FeedItem
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.enumeration.AttachmentType
import java.text.SimpleDateFormat
import java.util.Date

interface OnIteractionLister {
    fun like(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun openPost(post: Post)
    fun openImage(post: Post)
}

class PostsAdapter(
    private val onIteractionLister: OnIteractionLister
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
//            null -> error("Unknown item type")
//            null -> R.layout.card_post
            else -> R.layout.card_post
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onIteractionLister)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            else -> error("Unknown item type: $viewType")
        }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
//            null -> Unit
            else -> Unit
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(ad: Ad) {
        val urlAd = "${BuildConfig.BASE_URL}/media/${ad.image}"
        Glide.with(binding.imageAd)
            .load(urlAd)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(binding.imageAd)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onIteractionLister: OnIteractionLister
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
//            published.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(Date(post.published))
            published.text = "#${post.id.toString()} of ${SimpleDateFormat("yyyy.MM.dd HH:mm").format(Date(post.published))}"
            content.text = post.content

            val urlAvatar = "${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}"
            Glide.with(avatar)
                .load(urlAvatar)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .apply(RequestOptions().circleCrop()) //делает круглыми аватарки
                .into(avatar)

            if (post.attachment?.type == AttachmentType.IMAGE) {
                imageHolder.visibility = ImageView.VISIBLE
                val urlImages = "${BuildConfig.BASE_URL}/media/${post.attachment.url}"
                imageHolder.contentDescription = post.attachment.url

                Glide.with(imageHolder)
                    .load(urlImages)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(imageHolder)
            } else if (post.attachment?.type == null) {
                imageHolder.visibility = ImageView.GONE
            }

            btnLike.text = eraseZero(post.likes.toLong())
            btnLike.isChecked = post.likedByMe

            btnLike.setOnClickListener {
                println("like clicked")
                onIteractionLister.like(post)
            }

            content.setOnClickListener {
                println("content clicked")
                onIteractionLister.openPost(post)


            }
            postLayout.setOnClickListener { onIteractionLister.openPost(post) }
            avatar.setOnClickListener { onIteractionLister.openPost(post) }
            author.setOnClickListener { onIteractionLister.openPost(post) }
            published.setOnClickListener { onIteractionLister.openPost(post) }
            imageHolder.setOnClickListener { onIteractionLister.openImage(post) }

            menu.isVisible = post.ownedByMe  //Меню видно если пост наш
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onIteractionLister.remove(post)
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