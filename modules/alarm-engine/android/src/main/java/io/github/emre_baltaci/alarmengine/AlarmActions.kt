package io.github.emre_baltaci.alarmengine

import android.content.Context
import android.content.Intent
import android.util.Log

object AlarmActions {
    const val ACTION_SNOOZE = "io.github.emre_baltaci.alarmengine.SNOOZE"
    const val ACTION_OK = "io.github.emre_baltaci.alarmengine.OK"


    fun handle(context: Context, action: String?, spec: AlarmSpec) {
        when (action) {
            ACTION_SNOOZE -> snooze(context, spec)
            ACTION_OK -> ok(context, spec)
            else -> Log.w(AlarmReceiver.TAG, "Unknown alarm action: $action")
        }
    }

    private fun snooze(context: Context, spec: AlarmSpec) {
        val next = spec.copy(
            triggerAtMillis = System.currentTimeMillis() + spec.snoozeMinutes * 60_000L
        )
        try {
            AlarmScheduler.schedule(context, next)
        } catch (e: Exception) {
            // keep ringing: a failed snooze must not silence the alarm
            Log.e(AlarmReceiver.TAG, "Snooze failed for alarm ${spec.id}", e)
            return
        }
        stopRinging(context)
        Log.i(AlarmReceiver.TAG, "Alarm ${spec.id} snoozed for ${spec.snoozeMinutes} min")
    }

    private fun ok(context: Context, spec: AlarmSpec) {
        stopRinging(context)
        Log.i(AlarmReceiver.TAG, "Alarm ${spec.id}: OK")
    }

    private fun stopRinging(context: Context) {
        context.stopService(Intent(context, AlarmService::class.java))
    }
}
