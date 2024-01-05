package ru.netologia.nmedia.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netologia.nmedia.R
import ru.netologia.nmedia.activity.NewPostFragment.Companion.text
import ru.netologia.nmedia.auth.AppAuth
import ru.netologia.nmedia.databinding.ActivityAppBinding
import ru.netologia.nmedia.viewmodel.AuthViewModel
import javax.inject.Inject

@AndroidEntryPoint //Работа с зависимостями через HILT
class AppActivity : AppCompatActivity() {

    @Inject//Внедряем зависимость для авторизации
    lateinit var appAuth: AppAuth

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability


    private val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.let { intent ->
            if (intent.action != Intent.ACTION_SEND) {
                return@let
            }
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(binding.root, R.string.error_empty_content, LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        finish()
                    }.show()
                return@let
            }
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            navController.navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().also { it.text = text }//проброс аргументов между фрагментами и активити
            )
        }



        lifecycleScope.launch {// принудительное обновление меню, при различных действиях
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { //Наблюдает за авторизацией только когда активити доступно для взаимодействия
                viewModel.data.collect {
                    invalidateOptionsMenu()
                }
            }
        }


        //ANDAD_01 Д/з №2
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("some stuff happened: ${task.exception}")
                return@addOnCompleteListener
            }

            val token = task.result
            println(token)
        }

        checkGoogleApiAvailability()

        requestNotificationsPermission()


        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onPrepareMenu(menu: Menu) { //Видимость пунктов меню в зависимости от того залоген юзер или нет
                menu.setGroupVisible(R.id.authenticated, viewModel.authenticated)
                menu.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.signin -> {
                        findNavController(R.id.nav_host_fragment).navigate(R.id.authFragment)
                        true
                    }

                    R.id.signup -> {
                        appAuth.setAuth(5, "x-token")
                        true
                    }

                    R.id.signout -> {
                        appAuth.removeAuth()
                        true
                    }

                    else -> false
                }
            }
        })

        requestNotificationsPermission()
        checkGoogleApiAvailability()
    }


    override fun onStart() {
        super.onStart()
        //Кнопка навигации меню слева от имени приложения при добавлении нового поста
        findNavController(R.id.nav_host_fragment)
            .addOnDestinationChangedListener { controller,
                                               destination,
                                               arguments ->
                supportActionBar?.setDisplayHomeAsUpEnabled(destination.id == R.id.newPostFragment)
                //TODO не переходит назад
            }
    }

    private fun requestNotificationsPermission() {
//        проверка на версию андроида выше 13 версии, для
//        получения разрешений для работы с приложением
//        токен eT3Cv-eKTJiKu1knufWCEJ:APA91bFX1Se1NZteaU7nCWfM3jKItD5PecOGImRHyxn-ZIParoULkXa78wudJU4I7p_AqKcLoSCRJJRVYKFc607QyCi2f79wmIgvFXiWwOPsTdUZU6Ujvf0YUaFbITdKIGoNSDD_xZbf
        println("запрос разрешений")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1) //вот это вызывает окно запроса разрешений
    }

    //проверка на установленную google Api, на Huawei нет такого апи например
    private fun checkGoogleApiAvailability() {
        println("запрос апи")
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, "Google Api Unavailable", Toast.LENGTH_LONG).show()
        }
    }
}
