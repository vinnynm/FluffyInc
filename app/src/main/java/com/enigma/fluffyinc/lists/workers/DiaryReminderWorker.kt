package com.enigma.fluffyinc.lists.workers

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import androidx.work.WorkerParameters
import com.enigma.fluffyinc.lists.di.AppModule
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "diary_reminders"
const val WORK_TAG_DIARY = "diary_reminder"

class DiaryReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val entryId = inputData.getString("entry_id") ?: return Result.failure()
        val entryTitle = inputData.getString("entry_title") ?: "Upcoming event"
        val isOnDay = inputData.getBoolean("is_on_day", false)

        val diaryRepository = AppModule.provideDiaryRepository(applicationContext)

        createNotificationChannel()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(if (isOnDay) "📅 Today: $entryTitle" else "⏰ Tomorrow: $entryTitle")
            .setContentText(if (isOnDay) "Your planned event is today!" else "You have an event planned for tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        nm.notify(entryId.hashCode(), notification)
        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Diary Reminders", NotificationManager.IMPORTANCE_HIGH)
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}

fun scheduleDiaryReminders(context: Context, entryId: String, entryTitle: String, eventDate: Long) {
    val now = System.currentTimeMillis()
    val oneDayBefore = eventDate - TimeUnit.DAYS.toMillis(1)
    val workManager = WorkManager.getInstance(context)

    workManager.cancelAllWorkByTag("$WORK_TAG_DIARY-$entryId")

    if (oneDayBefore > now) {
        val delay = oneDayBefore - now
        val dayBeforeRequest = OneTimeWorkRequestBuilder<DiaryReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("entry_id" to entryId, "entry_title" to entryTitle, "is_on_day" to false))
            .addTag("$WORK_TAG_DIARY-$entryId")
            .build()
        workManager.enqueue(dayBeforeRequest)
    }

    if (eventDate > now) {
        val delay = eventDate - now
        val onDayRequest = OneTimeWorkRequestBuilder<DiaryReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("entry_id" to entryId, "entry_title" to entryTitle, "is_on_day" to true))
            .addTag("$WORK_TAG_DIARY-$entryId")
            .build()
        workManager.enqueue(onDayRequest)
    }
}

fun cancelDiaryReminders(context: Context, entryId: String) {
    WorkManager.getInstance(context).cancelAllWorkByTag("$WORK_TAG_DIARY-$entryId")
}
