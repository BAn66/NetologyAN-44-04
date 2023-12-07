package ru.netologia.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netologia.nmedia.R
import ru.netologia.nmedia.databinding.FragmentNewPostBinding
import ru.netologia.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netologia.nmedia.util.StringArg
import ru.netologia.nmedia.viewmodel.PostViewModel

/** Работа через фрагменты */
class NewPostFragment : Fragment() {


    companion object {
        var Bundle.text by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()
    private val photoResultContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { // Контракт для картинок
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val file = uri.toFile()
                viewModel.setPhoto(uri, file)
            }
        }

    fun toastErrMess(viewModel: PostViewModel) {
        if (viewModel.dataState.value!!.error) {
            Snackbar.make(
                FragmentNewPostBinding.inflate(layoutInflater).root,
                "Ошибка в добавлении : ${viewModel.errorMessage.first} - ${viewModel.errorMessage.second}",
//                    R.string.error_loading,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                .show()
//                viewModel.errorMessage = Pair(0, "")
//                findNavController().popBackStack(R.id.feedFragment, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater)

        val text =
            arguments?.text//получение аргументов между фрагментами при создании нового поста из старого в choosere
        if (text != null) {
            binding.editTextNewPost.setText(text)
        }

//        Для загрузки черновика
        setFragmentResultListener("requestSavedTmpContent") { key, bundle ->
            toastErrMess(viewModel)
            val savedTmpContent = bundle.getString("savedTmpContent")
            binding.editTextNewPost.setText(savedTmpContent)
        }

        //Для редактирования поста
        setFragmentResultListener("requestIdForNewPostFragment") { key, bundle ->
            toastErrMess(viewModel)
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val resultId = bundle.getLong("id")
            if (resultId != 0L) {
                val resultPost =
                    viewModel.data.value?.posts!!.filter { it -> it.id == resultId }[0].copy()
                viewModel.edit(resultPost)
                binding.editTextNewPost.setText(resultPost.content)
            }
        }

        setFragmentResultListener("requestIdForNewPostFragmentFromPost") { key, bundle ->
            toastErrMess(viewModel)
            // Здесь можно передать любой тип, поддерживаемый Bundle-ом
            val resultId2 = bundle.getLong("id")
            if (resultId2 != 0L) {
                val resultPost =
                    viewModel.data.value?.posts!!.filter { it -> it.id == resultId2 }[0].copy()
                viewModel.edit(resultPost)
                binding.editTextNewPost.setText(resultPost.content)
            }
        }

        binding.editTextNewPost.requestFocus()
        binding.editTextNewPost.focusAndShowKeyboard()

// Старая версия кнопки сохранения поста
        //А если пост пустой с 0 ID то будет сохранятся как новый
//        binding.ok.setOnClickListener {
//            if (binding.editTextNewPost.text.isNotBlank()) {
//                val content = binding.editTextNewPost.text.toString()
//                viewModel.changeContentAndSave(content)
//                toastErrMess(viewModel)
//            } else {
//                Snackbar.make(binding.root, R.string.error_empty_content,
//                    BaseTransientBottomBar.LENGTH_INDEFINITE
//                )
//                    .setAction(android.R.string.ok) {
//                    }.show()
//                return@setOnClickListener
//            }
//
////            возвращаемся на предыдущий фрагмент
//            viewModel.postCreated.observe(viewLifecycleOwner){ //Работа с SingleLiveEvent: Остаемся на экране редактирования пока не придет ответ с сервера
//                viewModel.loadPosts()// не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
//                toastErrMess(viewModel)
//                findNavController().popBackStack(R.id.feedFragment, false)
//            }
//
//        }
        //Версия меню для сохранения поста
//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menu.clear()
//                menuInflater.inflate(R.menu.save_post, menu)
//                menu.close()
//
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
//                when (menuItem.itemId) {
//                    R.id.save -> {
//                        if (!binding.editTextNewPost.text.isNotBlank()
////                            && !binding.imageContainer.isVisible
//                            ) {
//                            Snackbar.make(
//                                binding.root, R.string.error_empty_content,
//                                BaseTransientBottomBar.LENGTH_INDEFINITE
//                            )
//                                .setAction(android.R.string.ok) {
//                                }.show()
//                        } else {
//                            val content = binding.editTextNewPost.text.toString()
//                            viewModel.changeContentAndSave(content)
////                            toastErrMess(viewModel)
//                        }
//                        true
//                    }
//
//                    else -> false
//                }
//
//        })

        var showMenu = false
        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                showMenu = true
                menuInflater.inflate(R.menu.save_post, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.save).isVisible = showMenu
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        val content = binding.editTextNewPost.text.toString()
                        viewModel.changeContentAndSave(content)
                        showMenu = false
                        activity?.invalidateOptionsMenu()
                        true
                    }
                    else -> false
                }

        })

        viewModel.postCreated.observe(viewLifecycleOwner) { //Работа с SingleLiveEvent: Остаемся на экране редактирования пока не придет ответ с сервера
            viewModel.loadPosts()// не забываем обновить значения вью модели (запрос с сервера и загрузка к нам)
            toastErrMess(viewModel)
            findNavController().popBackStack(R.id.feedFragment, false)
            activity?.invalidateOptionsMenu()
        }

        binding.remove.setOnClickListener {
            viewModel.clearPhoto()
        }

        binding.takePhoto.setOnClickListener { //Берем фотку черзе камеру
            ImagePicker.Builder(this)
                .crop()
                .cameraOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoResultContract::launch)
        }

        binding.pickPhoto.setOnClickListener { //Берем фотку через галерею
            ImagePicker.Builder(this)
                .crop()
                .galleryOnly()
                .maxResultSize(2048, 2048)
                .createIntent(photoResultContract::launch)
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it == null) {
                binding.imageContainer.isGone = true
                return@observe
            }
            binding.imageContainer.isVisible = true
            binding.preview.setImageURI(it.uri)
        }


        // При нажатии системной кнопки назад
//        val callback =
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            Toast.makeText(context, "Что то происходит", Toast.LENGTH_SHORT).show()
            val tmpContent = if (binding.editTextNewPost.text.isNotBlank()) {
                binding.editTextNewPost.text.toString()
            } else {
                ""
            }
            setFragmentResult("requestTmpContent", bundleOf("tmpContent" to tmpContent))
            findNavController().popBackStack(R.id.feedFragment, false)
            activity?.invalidateOptionsMenu()
        }
// Старая версия кнопки отмены сохранения поста
//        binding.cancelAddPost.setOnClickListener{
//            viewModel.emptyNew()
//            findNavController().popBackStack(R.id.feedFragment, false)
//        }

        return binding.root
    }
}
