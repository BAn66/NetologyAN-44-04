package ru.netologia.nmedia.viewmodel

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
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

interface OnInteractionListener {
    fun like(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
    fun openPost(post: Post)
    fun openImage(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

//    override fun onBindViewHolder( //для исправления анимации мерцания RecylerView
//        holder: RecyclerView.ViewHolder,
//        position: Int,
//        payloads: MutableList<Any>
//    ) {
//
//        if (payloads.isEmpty()) {
//            onBindViewHolder(holder, position)
//        } else {
//            payloads.forEach {
//                (it as? PayLoad)?.let {
//                    when (val item = getItem(position)) {
//                        is Ad -> (holder as? AdViewHolder)?.bind(it)
//                        is Post -> (holder as? PostViewHolder)?.bind(it)
//                        else -> Unit
//                    }
//                }
//            }
//        }
//    }

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
                PostViewHolder(binding, onInteractionListener)
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

//    fun bind(payload: PayLoad) {//для исправления анимации мерцания RecylerView
//        payload.likedByMe?.let {
//        }
//        payload.content?.let {
//        }
//    }

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
//            published.text = SimpleDateFormat("yyyy.MM.dd HH:mm").format(Date(post.published))
            published.text = "#${post.id} of ${post.published}"
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
//            btnLike.setOnClickListener {
//                println("like clicked")
//                onIteractionLister.like(post)
//            }

            btnLike.setOnClickListener {//анимация лайка
                val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.25F, 1F)
                val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.25F, 1F)
                ObjectAnimator.ofPropertyValuesHolder(it, scaleX, scaleY).apply {
                    duration = 500
//                    repeatCount = 100
                    interpolator = BounceInterpolator()
                }.start()
                onInteractionListener.like(post)
            }

            content.setOnClickListener {
                println("content clicked")
                onInteractionListener.openPost(post)


            }
            postLayout.setOnClickListener { onInteractionListener.openPost(post) }
            avatar.setOnClickListener { onInteractionListener.openPost(post) }
            author.setOnClickListener { onInteractionListener.openPost(post) }
            published.setOnClickListener { onInteractionListener.openPost(post) }
            imageHolder.setOnClickListener { onInteractionListener.openImage(post) }

            menu.isVisible = post.ownedByMe  //Меню видно если пост наш
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.remove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.edit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

        }
    }

//    fun bind(payload: PayLoad) {//для исправления анимации мерцания RecylerView
//        payload.likedByMe?.let {
//            binding.btnLike.isChecked = it
////            if (it){
////                ObjectAnimator.ofPropertyValuesHolder(
////                    binding.btnLike,
////                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0F, 1.2F),
////                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0F, 1.2F)
////                )
////            } else {
////                ObjectAnimator.ofFloat(binding.btnLike,View.ROTATION, 0F, 360F)
////
////            }.start()
//        }
//        payload.content?.let {
//            binding.content.text = it
//        }
//    }

}

//data class PayLoad(
//    //класс для исправления анимации мерцания RecylerView
//    val likedByMe: Boolean? = null,
//    val content: String? = null,
//)

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class)
            return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

//    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any =
////метод для исправления анимации мерцания RecylerView
//        PayLoad(
//            likedByMe = (newItem as Post).likedByMe.takeIf { it != (oldItem as Post).likedByMe },
//            content = (newItem as Post).content.takeIf { it != (oldItem as Post).content },
//        )


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