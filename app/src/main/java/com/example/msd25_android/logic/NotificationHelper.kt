package com.example.msd25_android.logic

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.msd25_android.R

object NotificationHelper {


    fun showExpenseNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, "expense_channel")
            .setSmallIcon(R.drawable.ic_notification) // Replace with any valid drawable in res/drawable
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
