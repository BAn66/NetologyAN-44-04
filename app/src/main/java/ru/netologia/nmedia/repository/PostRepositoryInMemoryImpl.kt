package ru.netologia.nmedia.repository

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netologia.nmedia.dto.Post
import java.util.Calendar


class PostRepositoryInMemoryImpl( //здесь по хорошему нужно переименовать в PostReposytoryInFile
    //Для работы с рефами достаточно просто указать контекст
    context: Context
    //для работы с файлами контекст делаем приватным свойством
//    private val context: Context
) : PostRepository {

    //работа с памятью с помощью преференс: Переменные
    private val postsKey= "posts"
    private val prefs = context.getSharedPreferences(postsKey, Context.MODE_PRIVATE)
    private val gson = Gson()
//    private var nextId = 1L
    private var posts = emptyList<Post>()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    //работа с памятью с помощью файлов: Переменные
//    private val gson = Gson()
//    private var nextId = 1L
//    private var posts = emptyList<Post>()
//    private val postsFileName= "post.json"
//    private var nextIdFileName = "next_id.json" //файл для айди не использую
//    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    private val data = MutableLiveData(posts)

    //работа с памятью с помощью преференс: инициализация
    init {
       posts = prefs.getString(postsKey, null)?.let{
            gson.fromJson<List<Post>>(it, type)
        }.orEmpty()

        data.value = posts
    }

    //работа с памятью с помощью файлов: инициализация
//        init {
//        val postFile = context.filesDir.resolve(postsFileName)
//
//        posts = if (postFile.exists()) {
//            postFile.reader().buffered().use {
//                gson.fromJson<List<Post>>(it, type)
//            }
//        } else {
//            emptyList()
//        }
//
//        val nextIdFile = context.filesDir.resolve(nextIdFileName) //следующий айди берем из файла (у меня не используется)
//        nextId = if (nextIdFile.exists()) {
//            nextIdFile.reader().buffered().use {
//                gson.fromJson(it, Long::class.java)
//            }
//        } else {
//            nextId
//        }
//
//        data.value = posts
//    }

    override fun getAll(): LiveData<List<Post>> = data
    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
        data.value = posts
        sync() //После каждого изменения синхронизируем данные с файлом
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
//                isShare = true,
                shares = it.shares + 1
            )
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = (posts.map { it.id }.toMutableList().maxOrNull() //поиск самого большого айди в списке постов
                        ?: 0) + 1,//Берем макс значения id в списке постов +1
                    author = "Me",
                    likedByMe = false,
                    //published = SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()).toString()
                    published = Calendar.getInstance().time.toString()
                )
            ) + posts
        } else {
            posts.map {
                if (it.id != post.id) it else it.copy(
                    content = post.content
                )
            }
        }
        data.value = posts

        sync()
        //return
    }
    override fun getPostById(id: Long): Post = posts.filter { it.id == id }[0].copy()


    //Работа с памятью с помощью преференс: синхронизация
    private fun sync(){
        prefs.edit {
            putString(postsKey, gson.toJson(posts))
        }
    }

    //Работа с памятью с помощью файлов: синхронизация
//    private fun sync(){
//        context.filesDir.resolve(postsFileName).writer().buffered().use(){
//            it.write(gson.toJson(posts))
//        }
//
//        context.filesDir.resolve(nextIdFileName).writer().buffered().use(){ //Не использую
//            it.write(gson.toJson(nextId))
//        }
//    }


}

// Хардовое объявление списка постов, без сохранения во вне
//    private var posts = listOf(
//        Post(
//            1L,
//            "Нетология. Университет интернет-профессий будущего",
//            "21 мая в 18:36",
//            "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
//            true,
//            10000L,
//            true,
//            6699999L,
//            5,
//            "https://www.youtube.com/watch?v=WhWc3b3KhnY"
//        ),
//        Post(
//            2L,
//            "Нетология. Университет интернет-профессий будущего",
//            "18 сентября в 10:12",
//            "Знаний хватит на всех: Российская компания и образовательная онлайн-платформа, запущенная в 2011 году. Одна из ведущих российских компаний онлайн-образования. Входит в IT-холдинг TalentTech, объединяющий компании по трём направлениям: EdTech, HRTech и Freelance. EdTech-сегмент холдинга, наряду с «Нетологией», представлен компаниями «Фоксфорд» и «TalentTech.Обучение»: → http://netolo.gy/fyb",
//            true,
//            10000L,
//            true,
//            6699999L,
//            5,
//            ""
//        ),
//        Post(
//            3L,
//            "Нетология. Университет интернет-профессий будущего",
//            "25 ноября в 18:36",
//            "Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. Тест, тест, тест, тест. → http://netolo.gy/fyb",
//            true,
//            10000L,
//            true,
//            6699999L,
//            5,
//            "https://www.youtube.com/watch?v=WhWc3b3KhnY"
//        ),
//        Post(
//            4L,
//            "Нетология. Университет интернет-профессий будущего",
//            "31 декабря в 18:36",
//            "Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! Новый год!!! → http://netolo.gy/fyb",
//            true,
//            10000L,
//            true,
//            6699999L,
//            5,
//            ""
//        )
//    ).reversed()
