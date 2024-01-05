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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentPostBinding
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.viewmodel.OnIteractionLister
import ru.netologia.nmedia.viewmodel.PostViewModel
import ru.netologia.nmedia.viewmodel.PostsAdapter


@AndroidEntryPoint
class PostFragment: Fragment (){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater)
        val viewModel: PostViewModel by activityViewModels()

        val adapter = PostsAdapter(object : OnIteractionLister {

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

            override fun openPost(post: Post) {
                TODO("Not yet implemented")
            }

            override fun openImage(post: Post) {
                // Здесь мы можем использовать Kotlin экстеншен функцию из fragment-ktx
                val resultId = post.id
                setFragmentResult("requestIdForImageFragment", bundleOf("id" to resultId))
                findNavController().navigate(R.id.action_postFragment_to_imageFragment)

            }
        })

        binding.listPost.adapter = adapter

        //Получаем айди поста для заполнения данных
        setFragmentResultListener("requestIdForPostFragment") { key, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            //            val result = bundle.getLong("id")
            lifecycleScope.launchWhenCreated { //После paging
                viewModel.data.collectLatest {
                    adapter.submitData(it)
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
