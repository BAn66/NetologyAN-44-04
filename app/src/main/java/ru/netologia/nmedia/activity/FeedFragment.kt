package ru.netologia.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.google.android.material.snackbar.Snackbar
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentFeedBinding
import ru.netologia.nmedia.dto.Post
//import ru.netologia.nmedia.model.FeedModel
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.viewmodel.OnIteractionLister
import ru.netologia.nmedia.viewmodel.PostViewModel
import ru.netologia.nmedia.viewmodel.PostsAdapter


/** Работа через фрагменты*/
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentFeedBinding.inflate(layoutInflater) // Работаем через надутый интерфейс с buildFeatures.viewBinding = true из build,gradle app


        fun toastErrMess(state: FeedModelState) {
            if (state.error) {
                Snackbar.make(
                    binding.root,
                    "Ошибка : ${viewModel.errorMessage.first} - ${viewModel.errorMessage.second}",
//                    R.string.error_loading,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
//            viewModel.errorMessage = Pair(0, "")
        }

        val adapter = PostsAdapter(object : OnIteractionLister {


            override fun like(post: Post) {
                viewModel.likeById(post.id, post.likedByMe)
            }

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

            override fun remove(post: Post) {
                viewModel.removeById(post.id)
                viewModel.loadPosts() // не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
                viewModel.loadPosts()
            }

//            override fun playVideo(post: Post) {
//                val intentV = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
//                startActivity(intentV)
//            }

            override fun openPost(post: Post) {
                // Здесь мы можем использовать Kotlin экстеншен функцию из fragment-ktx
                val resultId = post.id
                setFragmentResult("requestIdForPostFragment", bundleOf("id" to resultId))
                findNavController().navigate(R.id.action_feedFragment_to_postFragment)

            }
        })

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            toastErrMess(state)
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            //         Работаем с скролвью
            val newPost =
                state.posts.size > adapter.currentList.size //флаг если добавился новый пост
            // Адаптер для списка постов ROOM
            adapter.submitList(state.posts)
            {
                if (newPost) {
                    //прокрутка до начала при добавлении поста/ иначе будет мотать наверх при любом изменении в ScrollView
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
            binding.empty.isVisible = state.empty
        }

        binding.swiperefresh.setOnRefreshListener { // Обновляшка по свайпу
            viewModel.refreshPosts()
        }

        binding.retryButton.setOnClickListener {
            viewModel.refreshPosts()
        }

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


        binding.fab.setOnClickListener {
            setFragmentResultListener("requestTmpContent") { key, bundle ->
                val tmpContent = bundle.getString("tmpContent")
                setFragmentResult(
                    "requestSavedTmpContent",
                    bundleOf("savedTmpContent" to tmpContent)
                )
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
