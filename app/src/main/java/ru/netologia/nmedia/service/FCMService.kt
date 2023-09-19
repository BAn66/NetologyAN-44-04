package ru.netologia.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netologia.nmedia.R
import ru.netologia.nmedia.activity.AppActivity
import ru.netologia.nmedia.service.FCMService.Actions.*
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val channelId = "server"
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
    }

    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
//        message.data["action"]?.let {
//            when (valueOf(it)) {
//                LIKE -> handleLike(Gson().fromJson(message.data["content"], Like::class.java))
//                SHARE -> TODO()
//                VIEW -> TODO()
//            }
//
//        }
        val action = message.data["action"]
        when (action){
            LIKE.toString() -> handleLike(Gson().fromJson(message.data["content"], Like::class.java))
//            SHARE.toString() -> TODO()
//            VIEW.toString()-> TODO()
            NEWPOST.toString() ->handNewPost(Gson().fromJson(message.data["content"], NewPost::class.java))
            else -> handNotEnum()
        }
        println(Gson().toJson(message))
    }

    private fun handleLike(like: Like) {//обработка активности Лайк полученной из сообщения
       //это для рекации на нажатие на увдомление, чтобы уйти на экран приложения
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //устанавливаем уведомление данные в нем и все такое
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.post_avatar_drawable) //установка иконки уведомления
            .setContentText(getString(R.string.notification_user_like, like.userName, like.postAuthor)) //что будет в сообщениии
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

    private fun handNotEnum() {//обработка сообщения если передано что либо не из ENUM
        //это для рекации на нажатие на увдомление, чтобы уйти на экран приложения
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //устанавливаем уведомление данные в нем и все такое
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.post_avatar_drawable) //установка иконки уведомления
            .setContentText("Мы не знаем что это такое, если бы мы знали что это, но мы не знаем что это такое") //что будет в сообщениии
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

    private fun handNewPost(newPost: NewPost) {//обработка сообщения если передано что либо не из ENUM
        //это для рекации на нажатие на увдомление, чтобы уйти на экран приложения
        val intent = Intent(this, AppActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //устанавливаем уведомление данные в нем и все такое
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.post_avatar_drawable) //установка иконки уведомления
            .setContentTitle(getString(R.string.notification_user_new_post_tittle, newPost.userName))
//            .setContentText(getString(R.string.notification_user_new_post, newPost.postContent)) //что будет в сообщениии
//            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.post_avatar_drawable))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(getString(R.string.notification_user_new_post, newPost.postContent)))
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

    data class Like(
        val userId: Int, val userName: String, val postId: Int, val postAuthor: String
    )

    data class NewPost(
        val userId: Int, val userName: String,  val postContent: String
    )

    override fun onNewToken(token: String) {
//        super.onNewToken(token)
        println("Это токен:")
        println(token)
    }

    enum class Actions {
        LIKE,
//        SHARE,
//        VIEW,
        NEWPOST
    }

}