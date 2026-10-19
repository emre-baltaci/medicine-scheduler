package io.github.emre_baltaci.alarmengine

import android.content.Context
import android.content.Intent
import android.util.Log

object AlarmActions {
    const val ACTION_SNOOZE = "io.github.emre_baltaci.alarmengine.SNOOZE"
    const val ACTION_OK = "io.github.emre_baltaci.alarmengine.OK"

    // PoC test value; item 4 passes the snooze minutes from JS (default 5)
    private const val SNOOZE_MINUTES = 1L

    fun handle(context: Context, action: String?, id: Int) {
        when (action) {
            ACTION_SNOOZE -> snooze(context, id)
            ACTION_OK -> ok(context, id)
            else -> Log.w(AlarmReceiver.TAG, "Unknown alarm action: $action")
        }
    }

    private fun snooze(context: Context, id: Int) {
        val triggerAt = System.currentTimeMillis() + SNOOZE_MINUTES * 60 * 1000
        try {
            AlarmScheduler.schedule(context, id, triggerAt)
        } catch (e: Exception) {
            // keep ringing: a failed snooze must not silence the alarm
            Log.e(AlarmReceiver.TAG, "Snooze failed for alarm $id", e)
            return
        }
        stopRinging(context)
        Log.i(AlarmReceiver.TAG, "Alarm $id snoozed for $SNOOZE_MINUTES min")
    }

    private fun ok(context: Context, id: Int) {
        stopRinging(context)
        Log.i(AlarmReceiver.TAG, "Alarm $id: OK")
    }

    private fun stopRinging(context: Context) {
        context.stopService(Intent(context, AlarmService::class.java))
    }
}
