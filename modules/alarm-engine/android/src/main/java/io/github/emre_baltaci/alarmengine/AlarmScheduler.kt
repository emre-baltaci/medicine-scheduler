package io.github.emre_baltaci.alarmengine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import expo.modules.kotlin.exception.CodedException

object AlarmScheduler {
    fun schedule(context: Context, spec: AlarmSpec) {
        validate(spec)
        if (spec.triggerAtMillis <= System.currentTimeMillis()) {
            throw CodedException("ERR_TIME_IN_PAST", "Alarm time must be in the future", null)
        }

        val alarmManager = context.getSystemService(AlarmManager::class.java)
            ?: throw CodedException("ERR_NO_ALARM_MANAGER", "AlarmManager is not available", null)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            throw CodedException("ERR_EXACT_ALARM_DENIED", "Exact alarm permission is not granted", null)
        }

        val fireIntent = spec.putInto(Intent(context, AlarmReceiver::class.java))

        val firePending = PendingIntent.getBroadcast(
            context,
            spec.id,
            fireIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val showIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: throw CodedException("ERR_NO_LAUNCH_INTENT", "App launch intent not found", null)

        val showPending = PendingIntent.getActivity(context, 0, showIntent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(spec.triggerAtMillis, showPending), firePending)
    }

    private const val MAX_TEXT_LENGTH = 200

    private fun validate(spec: AlarmSpec) {
        requireOption(spec.snoozeMinutes in 1..120, "snoozeMinutes must be 1-120")
        requireOption(spec.confirmationDelayMinutes in 0..120, "confirmationDelayMinutes must be 0-120")
        requireOption(spec.autoStopMinutes in 1..60, "autoStopMinutes must be 1-60")
        for (text in listOf(spec.title, spec.lockedTitle, spec.snoozeLabel, spec.okLabel)) {
            requireOption(
                text.isNotBlank() && text.length <= MAX_TEXT_LENGTH,
                "texts must be 1-$MAX_TEXT_LENGTH characters"
            )
        }
        requireOption(spec.body.length <= MAX_TEXT_LENGTH, "body must be at most $MAX_TEXT_LENGTH characters")
    }

    private fun requireOption(valid: Boolean, message: String) {
        if (!valid) throw CodedException("ERR_INVALID_OPTION", message, null)
    }
}
