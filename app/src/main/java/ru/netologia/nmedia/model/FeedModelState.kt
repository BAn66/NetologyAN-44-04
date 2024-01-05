package ru.netologia.nmedia.model
//import ru.netologia.nmedia.dto.Post

//data class FeedModel( //отвечает непосредственно за  данные -список постов
//    val posts: List<Post>  = emptyList(),
//    val empty: Boolean = false,
//)

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)