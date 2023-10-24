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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentNewPostBinding
import ru.netologia.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netologia.nmedia.util.StringArg
import ru.netologia.nmedia.viewmodel.PostViewModel

/** Работа через фрагменты */
class NewPostFragment : Fragment(){

    companion object{
        var Bundle.text by StringArg
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater)


        val viewModel: PostViewModel by activityViewModels()

        val text = arguments?.text//получение аргументов между фрагментами при создании нового поста из старого в choosere
        if (text !=null){
            binding.editTextNewPost.setText(text)
        }

//        Для загрузки черновика
        setFragmentResultListener("requestSavedTmpContent") { key, bundle ->
            val savedTmpContent = bundle.getString("savedTmpContent")
            binding.editTextNewPost.setText(savedTmpContent)
        }

        //Для редактирования поста
        setFragmentResultListener("requestIdForNewPostFragment") { key, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val resultId = bundle.getLong("id")
            if (resultId != 0L) {
                val resultPost = viewModel.data.value?.posts!!.filter { it -> it.id == resultId }[0].copy()
                viewModel.edit(resultPost)
                binding.editTextNewPost.setText(resultPost.content)
            }
        }

        setFragmentResultListener("requestIdForNewPostFragmentFromPost") { key, bundle ->
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val resultId2 = bundle.getLong("id")
            if (resultId2 != 0L) {
                val resultPost = viewModel.data.value?.posts!!.filter { it -> it.id == resultId2 }[0].copy()
                viewModel.edit(resultPost)
                binding.editTextNewPost.setText(resultPost.content)
            }
        }

        binding.editTextNewPost.requestFocus()
        binding.editTextNewPost.focusAndShowKeyboard()


        //А если пост пустой с 0 ID то будет сохранятся как новый
        binding.ok.setOnClickListener {
            if (binding.editTextNewPost.text.isNotBlank()) {
                val content = binding.editTextNewPost.text.toString()
                viewModel.changeContentAndSave(content)
            } else {
                Snackbar.make(binding.root, R.string.error_empty_content,
                    BaseTransientBottomBar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                    }.show()
                return@setOnClickListener
            }

//            возвращаемся на предыдущий фрагмент
            viewModel.postCreated.observe(viewLifecycleOwner){ //Работа с SingleLiveEvent: Остаемся на экране редактирования пока не придет ответ с сервера
                viewModel.load() // не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
                findNavController().popBackStack(R.id.feedFragment, false)
            }

        }

        // При нажатии системной кнопки назад
//        val callback =
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
//            Toast.makeText(context, "Что то происходит", Toast.LENGTH_SHORT).show()
            val tmpContent = if (binding.editTextNewPost.text.isNotBlank()) {
                binding.editTextNewPost.text.toString()
            } else {
                ""
            }
            setFragmentResult("requestTmpContent", bundleOf("tmpContent" to tmpContent))
            findNavController().popBackStack(R.id.feedFragment, false)
        }

        binding.cancelAddPost.setOnClickListener{
            viewModel.emptyNew()
            findNavController().popBackStack(R.id.feedFragment, false)
        }

        return binding.root
    }
}
