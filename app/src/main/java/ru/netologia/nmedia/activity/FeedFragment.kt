package ru.netologia.nmedia.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netologia.nmedia.R
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.databinding.FragmentFeedBinding
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.model.FeedModelState
import ru.netologia.nmedia.viewmodel.OnIteractionLister
import ru.netologia.nmedia.viewmodel.PostViewModel
import ru.netologia.nmedia.viewmodel.PostsAdapter
import javax.inject.Inject


/** Работа через фрагменты*/

@AndroidEntryPoint
class FeedFragment : Fragment() {
    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth

    private val viewModel: PostViewModel by activityViewModels()

    fun toastErrMess(state: FeedModelState) {
        if (state.error) {
            Snackbar.make(
                FragmentFeedBinding.inflate(layoutInflater).root,
                "Ошибка в основном : ${viewModel.errorMessage.first} - ${viewModel.errorMessage.second}",

                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding =
            FragmentFeedBinding.inflate(layoutInflater) // Работаем через надутый интерфейс с buildFeatures.viewBinding = true из build,gradle app

        val adapter = PostsAdapter(object : OnIteractionLister {


            override fun like(post: Post) {
                viewModel.likeById(post.id, post.likedByMe)
            }

            override fun remove(post: Post) {
                viewModel.removeById(post.id)
                viewModel.loadPosts() // не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
                viewModel.loadPosts()
            }

            override fun openPost(post: Post) {
                val resultId = post.id
                setFragmentResult("requestIdForPostFragment", bundleOf("id" to resultId))
                findNavController().navigate(R.id.action_feedFragment_to_postFragment)

            }

            override fun openImage(post: Post) {
                val resultId = post.id
                setFragmentResult("requestIdForImageFragment", bundleOf("id" to resultId))
                findNavController().navigate(R.id.action_feedFragment_to_imageFragment)

            }
        })

        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            toastErrMess(state)
        }

        lifecycleScope.launchWhenCreated { //После paging
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {// Обновляшка по свайпу //c Paging
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing =
                it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        binding.swiperefresh.setOnRefreshListener { // Обновляшка по свайпу //c Paging
            adapter.refresh()
        }

        binding.retryButton.setOnClickListener {
            adapter.refresh()
        }

        lifecycleScope.launchWhenCreated {
            appAuth.authStateFlow.collectLatest {
                Log.d("REFRESH", "я обновил")
                adapter.refresh()
            }
        }


//        Работа редактирования через фрагменты (конкретно все в фрагменте NewPost)
        viewModel.edited.observe(viewLifecycleOwner) { it ->// Начало редактирования
            val resultId = it.id
            setFragmentResult("requestIdForNewPostFragment", bundleOf("id" to resultId))
            if (it.id != 0L) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            binding.showNew.isVisible =
                it > 0 //Условия видимости можно сменить на it > 0, в таком случае плашка не будет отображаться когда новых постов нет.
            println("$it posts add")
        }

        binding.showNew.setOnClickListener {
            binding.showNew.isVisible = viewModel.haveNew
            adapter.refresh()
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
