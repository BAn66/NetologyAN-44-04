package ru.netologia.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netologia.nmedia.R
import ru.netologia.nmedia.activity.AppActivity
import ru.netologia.nmedia.auth.AppAuth
import javax.inject.Inject
//import ru.netologia.nmedia.auth.AppAuth
//import ru.netologia.nmedia.di.DependencyContainer
//import ru.netologia.nmedia.service.FCMService.Actions.*
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val channelId = "server"

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() { //создаем канал по которому будет идти сообщение
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        appAuth.sendPushToken()
    }

    override fun onMessageReceived(message: RemoteMessage) { //работа с сообщениями от сервера, типа новый пост появился, или Вас лайкнули
        Log.d("FCMServices", message.data.toString())
        Log.d("FCMServices", message.data["action"].toString())

//Для лекции про пуши 44-04-14 Лекция про продвинутые пуши
        val recipientId = Gson().fromJson(message.data["content"], PushMessage::class.java).recipientId
        val id = appAuth.authStateFlow.value.id
        when {
            recipientId == null-> handPushMessage(Gson().fromJson(message.data["content"], PushMessage::class.java))
            recipientId == id ->  handPushMessage(Gson().fromJson(message.data["content"], PushMessage::class.java))
            else -> appAuth.sendPushToken()
        }
    }

    override fun onNewToken(token: String){ //для пушей, если появился новый токен2
        appAuth.sendPushToken()
        println("Это токен:")
        println(token)
    }

    private fun handPushMessage(pushMess: PushMessage) {//обработка активности Лайк полученной из сообщения
        //это для рекации на нажатие на увдомление, чтобы уйти на экран приложения
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //при новой установке уведомление, данные в нем и все такое
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.post_avatar_drawable) //установка иконки уведомления
            .setContentText(getString(R.string.notification_push_content, pushMess.content)) //что будет в сообщениии
            .setContentIntent(pi) //обработка нажатия на уведомление. чтобы уйти на экран приложения
            .setAutoCancel(true) //автозакрытие уведомления, после нажатия на него
            .build()

        if (ActivityCompat.checkSelfPermission( //проверка на согласие
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify( //формируем сообщение
                Random.nextInt(100_000), //если указать конкретное число, то сообщения будут затирать друг друга
                notification
            )
        }

    }

    data class PushMessage(
        val recipientId: Long?,
        val content: String
    )

}