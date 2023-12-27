package ru.netologia.nmedia.activity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netologia.nmedia.R
//import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.databinding.FragmentAuthBinding
//import ru.netologia.nmedia.di.DependencyContainer
import ru.netologia.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netologia.nmedia.util.StringArg
import ru.netologia.nmedia.viewmodel.SignInViewModel


//import ru.netologia.nmedia.viewmodel.ViewModelFactory
@AndroidEntryPoint
class AuthFragment : Fragment() {

    companion object {
        var Bundle.textLogin by StringArg
        var Bundle.textPassword by StringArg
    }
//    private val dependencyContainer = DependencyContainer.getInstance()
    private val viewModel: SignInViewModel by activityViewModels<SignInViewModel>(
//        factoryProducer = {
//            ViewModelFactory(dependencyContainer.repository, dependencyContainer.appAuth)
//        }
    )

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

        binding.editLogin.requestFocus()
        binding.editLogin.focusAndShowKeyboard()
        binding.editPassword.focusAndShowKeyboard()

        binding.signin.setOnClickListener {
            viewModel.sendRequest(
                binding.editLogin.text.toString(),
                binding.editPassword.text.toString()
            )
            findNavController().popBackStack()
        }

        // При нажатии системной кнопки назад
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.feedFragment, false)
        }
        return binding.root
    }
}

