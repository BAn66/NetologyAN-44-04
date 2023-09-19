package ru.netologia.nmedia.activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentFeedBinding
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.viewmodel.OnIteractionLister
import ru.netologia.nmedia.viewmodel.PostViewModel
import ru.netologia.nmedia.viewmodel.PostsAdapter


/** Работа через фрагменты*/
class FeedFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentFeedBinding.inflate(layoutInflater) // Работаем через надутый интерфейс с buildFeatures.viewBinding = true из build,gradle app


        val viewModel: PostViewModel by activityViewModels()

        val adapter = PostsAdapter(object : OnIteractionLister {

            override fun like(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun share(post: Post) { //создаем актвити Chooser для расшаривания текста поста через Intent
                viewModel.shareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
//                startActivity(intent) //Более скромный вариант ниже более симпатичный вариант
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.description_shared))
                startActivity(shareIntent)
            }

            override fun remove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
            }

            override fun playVideo(post: Post) {
                val intentV = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intentV)
            }

            override fun openPost(post: Post) {
                // Здесь мы можем использовать Kotlin экстеншен функцию из fragment-ktx
                val resultId = post.id
                setFragmentResult("requestIdForPostFragment", bundleOf("id" to resultId))
                findNavController().navigate(R.id.action_feedFragment_to_postFragment)

            }
        })

        binding.list.adapter = adapter
        // Работаем с скролвью
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            val newPost = posts.size > adapter.currentList.size //флаг если добавился новый пост
            adapter.submitList(posts) {
                if (newPost) {//прокрутка до начала при добавлении поста/ иначе будет мотать наверх при любом изменении в ScrollView

                    val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }
                    smoothScroller.setTargetPosition(0)
                    binding.list.layoutManager!!.startSmoothScroll(smoothScroller)
                    //прокрутка до начала при добавлении поста и при новом запуске
                }
            }
        }

//          Работа редактирования через EditView
//        val editPostLauncher = registerForActivityResult(EditPostResultContract()) { result ->
//            result ?: return@registerForActivityResult
//            viewModel.changeContentAndSave(result)
//            viewModel.emptyNew()
//        }
//        viewModel.edited.observe(viewLifecycleOwner) {
//            if (it.id != 0L) {
//                editPostLauncher.launch(it.content)
//            }
//        }

//        Работа редактирования через фрагменты (конкретно все в фрагменте NewPost)
        viewModel.edited.observe(viewLifecycleOwner) { it ->// Начало редактирования
            // viewModel.emptyNew()
            // Здесь мы можем использовать Kotlin экстеншен функцию из fragment-ktx
            val resultId = it.id
            setFragmentResult("requestIdForNewPostFragment", bundleOf("id" to resultId))
            if (it.id != 0L) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
//                editPostLauncher.launch(it.content)
            }
        }

        //Добавление нового поста через активити и интенты
//        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { result ->
//            result ?: return@registerForActivityResult
//            viewModel.emptyNew()
//            viewModel.changeContentAndSave(result)
//        }

        binding.fab.setOnClickListener {
            setFragmentResultListener("requestTmpContent") { key, bundle ->
                val tmpContent = bundle.getString("tmpContent")
                setFragmentResult("requestSavedTmpContent", bundleOf("savedTmpContent" to tmpContent))
            }
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        println("onStart $this")
    }

    override fun onStop() {
        super.onStop()
        println("onStop $this")
    }

    override fun onResume() {
        super.onResume()
        println("onResume $this")
    }

    override fun onPause() {
        super.onPause()
        println("onPause $this")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy $this")
    }
}

/** Старая версия через активити*/
//
//package ru.netologia.nmedia.activity
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.result.launch
//import androidx.activity.viewModels
//import androidx.constraintlayout.widget.Group
//import ru.netologia.nmedia.R
//import ru.netologia.nmedia.databinding.ActivityMainBinding
//import ru.netologia.nmedia.dto.Post
//import ru.netologia.nmedia.util.AndroidUtils.hideKeyboard
//import ru.netologia.nmedia.viewmodel.OnIteractionLister
//import ru.netologia.nmedia.viewmodel.PostViewModel
//import ru.netologia.nmedia.viewmodel.PostsAdapter
//
//class FeedFragment : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val prefs = getPreferences(Context.MODE_PRIVATE) //Запись данных для сохранения между запуском приложений
//        prefs.edit().apply{
//            putString("key", "test")
//            apply()
//        }
//
//        val binding =
//            ActivityMainBinding.inflate(layoutInflater) // Работаем через надутый интерфейс с buildFeatures.viewBinding = true из build,gradle app
//        setContentView(binding.root)
//
//        val viewModel: PostViewModel by viewModels()
//
//        val adapter = PostsAdapter(object : OnIteractionLister {
//
//            //            viewModel.likeById(it.id)
////        }, {
////            viewModel.shareById(it.id)
////        }, {
////            viewModel.removeById(it.id)
////        })
//            override fun like(post: Post) {
//                viewModel.likeById(post.id)
//            }
//
//            override fun share(post: Post) { //создаем актвити Chooser для расшаривания текста поста через Intent
//                viewModel.shareById(post.id)
//                val intent = Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_TEXT, post.content)
//                    type = "text/plain"
//                }
////                startActivity(intent) //Более скромный вариант ниже более симпатичный вариант
//                val shareIntent =
//                    Intent.createChooser(intent, getString(R.string.description_shared))
//                startActivity(shareIntent)
//            }
//
//            override fun remove(post: Post) {
//                viewModel.removeById(post.id)
//            }
//
//            override fun edit(post: Post) {
//                viewModel.edit(post)
//            }
//
//            override fun playVideo(post: Post) {
//               val intentV = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
//               startActivity(intentV)
//            }
//        })
//
//        binding.list.adapter = adapter
//        // Работаем с скролвью
//        viewModel.data.observe(this) { posts ->
//            val newPost = posts.size > adapter.currentList.size //флаг если добавился новый пост
//            adapter.submitList(posts) {
//                if (newPost) {//прокрутка до начала при добавлении поста/ иначе будет мотать наверх при любом изменении в ScrollView
//                    binding.list.smoothScrollToPosition(0)//прокрутка до начала при добавлении
//                }
//            }
//        }
//
//        val editPostLauncher = registerForActivityResult(EditPostResultContract()){result ->
//            result ?: return@registerForActivityResult
//            viewModel.changeContentAndSave(result)
//        }
//
//
//        viewModel.edited.observe(this) { it ->// Начало редактирования
//            if (it.id != 0L) {
//                editPostLauncher.launch(it.content)
//
////                Редактирование и сохранение в текствью внизу окна
////                binding.content.setText(it.content)
////                binding.content.focusAndShowKeyboard()
////                binding.postOneLine.text = it.content
////                binding.editCancelGroup.visibility = Group.VISIBLE
//            }
//        }
//
//
//
//
//        binding.cancel.setOnClickListener { //Отменяем редактирование
//            viewModel.emptyNew()
//            binding.content.setText("")
//            binding.content.clearFocus()
//            hideKeyboard(it) //скрываем клаву
//            binding.editCancelGroup.visibility = Group.GONE
//        }
//
//        binding.btnSave.setOnClickListener { //Сохраняем редактированное или новое
//            with(binding.content) {
//                if (text.isNullOrBlank()) { // проверка на пустой текст нового поста
//                    Toast.makeText(
//                        this@FeedFragment,
//                        "Content can`t be empty DUDE",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }
//                viewModel.changeContentAndSave(text.toString())
//                binding.content.setText("")
//                binding.content.clearFocus()
//                hideKeyboard(it) //скрываем клаву
//                binding.editCancelGroup.visibility = Group.GONE
//            }
//
//        }
//
//
//        val newPostLauncher = registerForActivityResult(NewPostResultContract()) { result ->
//            result ?: return@registerForActivityResult
//            viewModel.emptyNew()
//            viewModel.changeContentAndSave(result)
//        }
//
//        binding.fab.setOnClickListener {
//            newPostLauncher.launch()
//        }
//
//
//// Код для SroolView
////        viewModel.data.observe(this) { posts ->
////            binding.container.removeAllViews()
////            posts.map { post ->
////                CardPostBinding.inflate(layoutInflater, binding.container, true).apply {
////                    author.text = post.author
////                    published.text = post.published
////                    content.text = post.content
////                    countOfLiked.text = eraseZero(post.likes)
////                    countOfShared.text = eraseZero(post.shares)
////                    countOfView.text = eraseZero(post.views)
////                    btnLike.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
////                    btnLike.setOnClickListener {
////                        println("like clicked")
////                        viewModel.likeById(post.id)
////                    }
////                    btnShared.setOnClickListener {
////                        println("share clicked")
////                        viewModel.sharedById(post.id)
////                    }
////                }.root
////            }
////        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        println("onStart $this")
//    }
//
//    override fun onStop() {
//        super.onStop()
//        println("onStop $this")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        println("onResume $this")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        println("onPause $this")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        println("onDestroy $this")
//    }
//}
