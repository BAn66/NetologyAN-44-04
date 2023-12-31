package ru.netologia.nmedia.dto

import ru.netologia.nmedia.enumeration.AttachmentType


data class Post(
    val id: Long = 0L,
    val authorId: Long,
    val author: String = "",
    val authorAvatar: String = "",
    val content: String = "",
    val published: Long = 0L,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false
){
    @Override
    override fun toString(): String {
        return "id = $id , author = $author, content = $content ,published = $published, likes = $likes,likedByMe = $likedByMe, authorAvatar = $authorAvatar"
    }
}


data class Attachment(
    val url: String ,
    val type: AttachmentType,
)

