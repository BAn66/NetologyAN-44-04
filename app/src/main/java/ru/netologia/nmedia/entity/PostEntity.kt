package ru.netologia.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netologia.nmedia.dto.Attachment
import ru.netologia.nmedia.dto.Post

//import ru.netologia.nmedia.enumeration.AttachmentType
//import ru.netologia.nmedia.dto.Attachment


//Для ROOM
@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String = "",
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    @Embedded
    val attachment: Attachment? = null,
    val savedOnServer: Boolean = false,
    val showed: Boolean = true,
//    val shares: Long = 0L,
//    val views: Long = 0L,
//    val video: String = ""
) {
    fun toDto() = Post(
        id, authorId, author, authorAvatar, content, published, likedByMe, likes, attachment
//        shares, views, video
    )

    companion object {
        fun fromDto(postDto: Post): PostEntity =
            PostEntity(
                postDto.id,
                postDto.authorId,
                postDto.author,
                postDto.authorAvatar,
                postDto.content,
                postDto.published * 1000,
                postDto.likedByMe,
                postDto.likes,
                postDto.attachment,
//                post.shares, post.views, post.video
            )
    }
}

//Для удобства конвертации списков при использовании room с корутинами добавляем две extention функции
fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)

fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)




