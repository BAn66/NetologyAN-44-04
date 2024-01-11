package ru.netologia.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentImageBinding
import ru.netologia.nmedia.dto.Post
import ru.netologia.nmedia.util.AndroidUtils.toList
import ru.netologia.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class ImageFragment: Fragment (){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImageBinding.inflate(layoutInflater)

        val viewModel: PostViewModel by activityViewModels()

        //Получаем айди поста для заполнения данных
        setFragmentResultListener("requestIdForImageFragment") { key, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val result = bundle.getLong("id")


            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val listOnePost =
                        (viewModel.data.single().toList().filter { it -> it.id == result }[0] as Post)!!
                            .copy()
                    if (listOnePost.attachment != null) {
                        val urlImages = "http://10.0.2.2:9999/media/${listOnePost.attachment.url}"
                        binding.imageFromPost.contentDescription = listOnePost.attachment.url

                        Glide.with(binding.imageFromPost)
                            .load(urlImages)
                            .placeholder(R.drawable.ic_loading_100dp)
                            .error(R.drawable.ic_error_100dp)
                            .timeout(10_000)
                            .into(binding.imageFromPost)
                    }
                    binding.btnLike.text = eraseZero(listOnePost.likes.toLong())
                    binding.btnShare.text = "5"
                    binding.btnViews.text = "5"
                }
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
