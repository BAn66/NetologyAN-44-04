package ru.netologia.nmedia.viewmodel
import ru.netologia.nmedia.dto.Post
interface OnIteractionLister{
    fun like(post: Post)
//    fun share(post: Post)
    fun remove(post: Post)
    fun edit(post: Post)
//    fun playVideo(post: Post)
    fun openPost(post: Post)
    fun openImage(post: Post)
}