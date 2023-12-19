package ru.netologia.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import ru.netologia.nmedia.auth.AppAuth

class AuthViewModel: ViewModel() {
    val data = AppAuth.getInstance().authState

    val authenticated: Boolean
        get() = data.value.id != 0L
}