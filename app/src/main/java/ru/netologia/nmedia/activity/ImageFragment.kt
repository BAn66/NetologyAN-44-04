package ru.netologia.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentImageBinding
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.viewmodel.OnIteractionLister
import ru.netologia.nmedia.viewmodel.PostViewModel
import ru.netologia.nmedia.viewmodel.PostsAdapter

class ImageFragment: Fragment (){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(layoutInflater)
        val viewModel: PostViewModel by activityViewModels()

        val adapter = PostsAdapter(object : OnIteractionLister {

            override fun like(post: Post) {
            }


            override fun remove(post: Post) {
            }

            override fun edit(post: Post) {
            }


            override fun openPost(post: Post) {
            }

            override fun openImage(post: Post) {
            }
        })



        //Получаем айди поста для заполнения данных
        setFragmentResultListener("requestIdForImageFragment") { key, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val result = bundle.getLong("id")
            viewModel.data.observe(viewLifecycleOwner) { feedModelState ->
                // Работаем с скролвью
                val listOnePost = feedModelState.posts.filter { it.id == result }
                if (listOnePost[0].attachment != null) {
                    val urlImages = "http://10.0.2.2:9999/media/${listOnePost[0].attachment!!.url}"
                    binding.imageFromPost.contentDescription = listOnePost[0].attachment!!.url

                    Glide.with(binding.imageFromPost)
                        .load(urlImages)
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(binding.imageFromPost)
                }
                binding.btnLike.text = eraseZero(listOnePost[0].likes.toLong())
                binding.btnShare.text = "5"
                binding.btnViews.text = "5"

            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.feedFragment, false)
        }

        return binding.root
    }
}

private fun eraseZero(number: Long): String {
    var s = ""

    when (number) {
        in 0..999 -> s = number.toString()
        in 1000..9999 -> s =
            (number.toDouble() / 1000).toString().substring(0, number.toString().length - 1)
                .replace(".0", "") + "K"

        in 10000..999999 -> s =
            number.toString().substring(0, number.toString().length - 3) + "K"

        in 1000000..Int.MAX_VALUE -> s =
            (number.toDouble() / 1000000).toString().substring(0, number.toString().length - 4)
                .replace(".0", "") + "M"
    }
    return s
}
