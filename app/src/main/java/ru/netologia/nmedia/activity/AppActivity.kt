package ru.netologia.nmedia.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import ru.netologia.nmedia.R
import ru.netologia.nmedia.activity.NewPostFragment.Companion.text
import ru.netologia.nmedia.databinding.ActivityAppBinding

//Проверка работы гита, добавил комментарий в AppActivity
class AppActivity : AppCompatActivity() {
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
                supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
            val navController = navHostFragment.navController

            navController.navigate(R.id.action_feedFragment_to_newPostFragment,
//                bundleOf("content" to text) //проброс аргументов между фрагментами и активити
                Bundle().also { it.text = text }
            )
        }

        requestNotificationsPermission()
        checkGoogleApiAvailability()

    }

    private fun requestNotificationsPermission(){
//        проверка на версию андроида выше 13 версии, для
//        получения разрешений для работы с приложением
//        токен eT3Cv-eKTJiKu1knufWCEJ:APA91bFX1Se1NZteaU7nCWfM3jKItD5PecOGImRHyxn-ZIParoULkXa78wudJU4I7p_AqKcLoSCRJJRVYKFc607QyCi2f79wmIgvFXiWwOPsTdUZU6Ujvf0YUaFbITdKIGoNSDD_xZbf
        println("запрос разрешений")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            return
        }
        val permission = android.Manifest.permission.POST_NOTIFICATIONS
        if(checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED){
            return
        }

        requestPermissions(arrayOf(permission), 1) //вот это вызывает окно запроса разрешений
    }

    //проверка на установленную google Api, на Huawei нет такого апи например
    private fun checkGoogleApiAvailability() {
        println("запрос апи")
        with(GoogleApiAvailability.getInstance()){
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS){
                return@with
            }
            if (isUserResolvableError(code)){
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, "Google Api Unavailable", Toast.LENGTH_LONG).show()
        }
    }
}
