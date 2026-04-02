package com.enigma.fluffyinc.apps.finance.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

const val FINANCE_CHANNEL_ID = "finance_reminders"
const val WORK_TAG_FINANCE = "finance_reminder"

class FinanceReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val id = inputData.getLong("id", -1L)
        val title = inputData.getString("title") ?: "Upcoming Payment"
        val message = inputData.getString("message") ?: "You have a payment due soon."
        val type = inputData.getString("type") ?: "payment"

        if (id == -1L) return Result.failure()

        createNotificationChannel()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, FINANCE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify("${type}_$id".hashCode(), notification)
        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            FINANCE_CHANNEL_ID,
            "Finance Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming scheduled payments and loans"
        }
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}

fun scheduleFinanceReminder(
    context: Context,
    id: Long,
    title: String,
    message: String,
    paymentDate: Long,
    type: String
) {
    val now = System.currentTimeMillis()
    val workManager = WorkManager.getInstance(context)

    // Schedule a reminder 1 day before
    val oneDayBefore = paymentDate - TimeUnit.DAYS.toMillis(1)
    if (oneDayBefore > now) {
        val delay = oneDayBefore - now
        val request = OneTimeWorkRequestBuilder<FinanceReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "id" to id,
                "title" to "Upcoming $title",
                "message" to "Reminder: $message is due tomorrow.",
                "type" to type
            ))
            .addTag("$WORK_TAG_FINANCE-$type-$id")
            .build()
        workManager.enqueueUniqueWork(
            "$WORK_TAG_FINANCE-day-before-$type-$id",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // Schedule a reminder on the day
    if (paymentDate > now) {
        val delay = paymentDate - now
        val request = OneTimeWorkRequestBuilder<FinanceReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "id" to id,
                "title" to "Payment Due: $title",
                "message" to "Your $message is due today.",
                "type" to type
            ))
            .addTag("$WORK_TAG_FINANCE-$type-$id")
            .build()
        workManager.enqueueUniqueWork(
            "$WORK_TAG_FINANCE-on-day-$type-$id",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

fun cancelFinanceReminder(context: Context, id: Long, type: String) {
    WorkManager.getInstance(context).cancelAllWorkByTag("$WORK_TAG_FINANCE-$type-$id")
}
