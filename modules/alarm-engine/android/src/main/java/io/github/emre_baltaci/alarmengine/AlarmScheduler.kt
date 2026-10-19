package io.github.emre_baltaci.alarmengine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import expo.modules.kotlin.exception.CodedException

object AlarmScheduler {
    fun schedule(context: Context, id: Int, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) {
            throw CodedException("ERR_TIME_IN_PAST", "Alarm time must be in the future", null)
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: throw CodedException("ERR_NO_ALARM_MANAGER", "AlarmManager is not available", null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            throw CodedException("ERR_EXACT_ALARM_DENIED", "Exact alarm permission is not granted", null)
        }

        val fireIntent = Intent(context, AlarmReceiver::class.java)
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)

        val firePending = PendingIntent.getBroadcast(
            context,
            id,
            fireIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val showIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: throw CodedException("ERR_NO_LAUNCH_INTENT", "App launch intent not found", null)

        val showPending = PendingIntent.getActivity(context, 0, showIntent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showPending), firePending)
    }
}
