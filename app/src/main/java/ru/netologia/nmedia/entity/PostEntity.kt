package ru.netologia.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netologia.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Long = 0L,
    val authorAvatar: String? = null
//    val shares: Long = 0L,
//    val views: Long = 0L,
//    val video: String = ""
){
    fun toDto() = Post(id, author, content, published, likedByMe, likes, authorAvatar
//        shares, views, video
        )

    companion object {
        fun fromDto(post: Post) : PostEntity =
            PostEntity(post.id, post.author, post.content, post.published, post.likedByMe, post.likes, post.authorAvatar
//                post.shares, post.views, post.video
                        )
    }
}