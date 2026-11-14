package com.enigma.fluffyinc.apps.processors

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

// Schedule reminder in your Event creation logic
@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleEventReminder(context: Context, triggerTimeMillis: Long) {
    val intent = Intent(context, EventReminder::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerTimeMillis,
        pendingIntent
    )
}
