package ru.netologia.nmedia.activity;

import android.app.Activity;
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View;
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import ru.netologia.nmedia.R
import ru.netologia.nmedia.activity.NewPostFragment.Companion.text
import ru.netologia.nmedia.databinding.FragmentAuthBinding
import ru.netologia.nmedia.databinding.FragmentNewPostBinding
import ru.netologia.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netologia.nmedia.util.StringArg

import ru.netologia.nmedia.viewmodel.SignInViewModel;

class AuthFragment : Fragment() {
    companion object {
        var Bundle.textLogin by StringArg
        var Bundle.textPassword by StringArg
    }

    private val viewModel: SignInViewModel by activityViewModels<SignInViewModel>()

    fun signin(view: View) {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater)

        val textLogin =
            arguments?.textLogin//получение аргументов между фрагментами при создании нового поста из старого в choosere
        if (textLogin != null) {
            binding.editLogin.setText(textLogin)
        }

        val textPassword =
            arguments?.textPassword//получение аргументов между фрагментами при создании нового поста из старого в choosere
        if (textLogin != null) {
            binding.editPassword.setText(textPassword)
        }

//        binding.editLogin.requestFocus()
        binding.editLogin.focusAndShowKeyboard()

        binding.editPassword.focusAndShowKeyboard()

        binding.signin.setOnClickListener {
            viewModel.sendRequest(binding.editLogin.text.toString(), binding.editPassword.text.toString())
            findNavController().popBackStack()
        }

        // При нажатии системной кнопки назад
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            Toast.makeText(context, "Что то происходит", Toast.LENGTH_SHORT).show()

//            val tmpContent = if (binding.editTextNewPost.text.isNotBlank()) {
//                binding.editTextNewPost.text.toString()
//            } else {
//                ""
//            }

//            setFragmentResult("requestTmpContent", bundleOf("tmpContent" to tmpContent))
            findNavController().popBackStack(R.id.feedFragment, false)
        }

        return binding.root
    }
}

