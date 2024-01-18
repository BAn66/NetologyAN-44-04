package ru.netologia.nmedia.dto

import ru.netologia.nmedia.enumeration.AttachmentType
import java.time.OffsetDateTime

sealed interface FeedItem { // интерфейс объединяющий рекламу и пост, sealde интерфейс ограниченный имеющий только две заданные нами реализации
    val id: Long
}

data class Post(
    override val id: Long = 0L,
    val authorId: Long,
    val author: String = "",
    val authorAvatar: String = "",
    val content: String = "",
    val published: OffsetDateTime,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
) : FeedItem {
    @Override
    override fun toString(): String {
        return "id = $id , author = $author, content = $content ,published = $published, likes = $likes,likedByMe = $likedByMe, authorAvatar = $authorAvatar"
    }
}

data class Ad( //Реклама
    override val id: Long,
    val image: String,
) : FeedItem

data class Loading(
    override val id: Long) //Процесс загрузки
 : FeedItem


data class Attachment(
    val url: String ,
    val type: AttachmentType,
)
