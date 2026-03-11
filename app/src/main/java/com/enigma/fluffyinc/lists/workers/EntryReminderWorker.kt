package com.enigma.fluffyinc.lists.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.enigma.fluffyinc.lists.di.AppModule
import com.enigma.fluffyinc.lists.domain.model.RecurrenceType
import com.enigma.fluffyinc.lists.domain.model.Reminder
import java.util.concurrent.TimeUnit

const val REMINDER_CHANNEL_ID = "entry_reminders"
const val WORK_TAG_REMINDER = "entry_reminder"

class EntryReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString("reminder_id") ?: return Result.failure()
        val reminderRepo = AppModule.provideReminderRepository(applicationContext)
        val reminder = reminderRepo.getReminderById(reminderId) ?: return Result.failure()
        if (!reminder.isEnabled) return Result.success()

        createNotificationChannel()
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ Reminder: ${reminder.label.ifBlank { "You have a reminder" }}")
            .setContentText(recurrenceDescription(reminder.recurrence))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(reminderId.hashCode(), notification)

        // Schedule next occurrence if recurring
        if (reminder.recurrence != RecurrenceType.NONE) {
            val nextTrigger = nextOccurrence(reminder.triggerAt, reminder.recurrence)
            val updatedReminder = reminder.copy(triggerAt = nextTrigger)
            reminderRepo.upsertReminder(updatedReminder)
            scheduleReminderWork(applicationContext, updatedReminder)
        }
        return Result.success()
    }

    private fun recurrenceDescription(r: RecurrenceType) = when (r) {
        RecurrenceType.NONE -> "One-time reminder"
        RecurrenceType.DAILY -> "Repeats daily"
        RecurrenceType.WEEKLY -> "Repeats weekly"
        RecurrenceType.MONTHLY -> "Repeats monthly"
        RecurrenceType.YEARLY -> "Repeats yearly"
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(REMINDER_CHANNEL_ID, "Entry Reminders", NotificationManager.IMPORTANCE_HIGH)
        (applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}

fun nextOccurrence(from: Long, recurrence: RecurrenceType): Long {
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = from }
    when (recurrence) {
        RecurrenceType.DAILY -> cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        RecurrenceType.WEEKLY -> cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
        RecurrenceType.MONTHLY -> cal.add(java.util.Calendar.MONTH, 1)
        RecurrenceType.YEARLY -> cal.add(java.util.Calendar.YEAR, 1)
        RecurrenceType.NONE -> {}
    }
    return cal.timeInMillis
}

fun scheduleReminderWork(context: Context, reminder: Reminder) {
    val now = System.currentTimeMillis()
    if (reminder.triggerAt <= now) return
    val delay = reminder.triggerAt - now
    val work = OneTimeWorkRequestBuilder<EntryReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(workDataOf("reminder_id" to reminder.id))
        .addTag("$WORK_TAG_REMINDER-${reminder.id}")
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "$WORK_TAG_REMINDER-${reminder.id}",
        ExistingWorkPolicy.REPLACE,
        work
    )
}

fun cancelReminderWork(context: Context, reminderId: String) {
    WorkManager.getInstance(context).cancelUniqueWork("$WORK_TAG_REMINDER-$reminderId")
}
