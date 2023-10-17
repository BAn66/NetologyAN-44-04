package ru.netologia.nmedia.model

import ru.netologia.nmedia.dto.Post

data class FeedModelState(
    val posts: List<Post> = emptyList(),
    val error: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val empty: Boolean = false
) {
}