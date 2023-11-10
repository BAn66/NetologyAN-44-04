package ru.netologia.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netologia.nmedia.dto.Attachment
import ru.netologia.nmedia.dto.Post

//Для ROOM
@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment? = null
//    val shares: Long = 0L,
//    val views: Long = 0L,
//    val video: String = ""
){
    fun toDto() = Post(id, author, authorAvatar, content, published, likedByMe, likes, attachment
//        shares, views, video
        )

    companion object {
        fun fromDto(post: Post) : PostEntity =
            PostEntity(post.id, post.author, post.authorAvatar, post.content, post.published, post.likedByMe, post.likes, post.attachment
//                post.shares, post.views, post.video
                        )
    }
}


//Для удобства конвертации списков при использовании room с корутинами добавляем две extention функции
fun List<PostEntity>.toDto(): List<Post> = map(PostEntity:: toDto)

fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity :: fromDto)

