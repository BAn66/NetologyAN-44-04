package ru.netologia.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.filter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentPostBinding
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.viewmodel.OnInteractionListener
import ru.netologia.nmedia.viewmodel.PostViewModel
import ru.netologia.nmedia.viewmodel.PostsAdapter


@AndroidEntryPoint
class PostFragment : Fragment() {
    val viewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater)

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun like(post: Post) {
                viewModel.likeById(post.id, post.likedByMe)
            }

            override fun remove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun edit(post: Post) {
                viewModel.edit(post)
            }

            override fun openPost(post: Post) {}

            override fun openImage(post: Post) {
                // Здесь мы можем использовать Kotlin экстеншен функцию из fragment-ktx
                val resultId = post.id
                setFragmentResult("requestIdForImageFragment", bundleOf("id" to resultId))
                findNavController().navigate(R.id.action_postFragment_to_imageFragment)

            }
        })

        binding.listPost.adapter = adapter

        //Получаем айди поста для заполнения данных

        //TODO Реккомендуют вместо RecyclerView для отображения одного поста, можно добавить в репозиторий (и во вьюмодель) метод, который возвращал бы подписку на пост из базы данных по id.
        setFragmentResultListener("requestIdForPostFragment") { key, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val result = bundle.getLong("id")
            val idPost = MutableStateFlow(result).value
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.data.collectLatest {
                        adapter.submitData(
                            it.filter {
                                it.id == idPost
                            }
                        )
                    }
                }
            }
        }

        viewModel.edited.observe(viewLifecycleOwner) { it ->// Начало редактирования
            val resultId = it.id
            setFragmentResult("requestIdForNewPostFragmentFromPost", bundleOf("id" to resultId))
            if (it.id != 0L) {
                findNavController().navigate(R.id.action_postFragment_to_newPostFragment2)
            }
        }

        return binding.root
    }
}
