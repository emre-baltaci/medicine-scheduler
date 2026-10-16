package io.github.emre_baltaci.alarmengine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        Log.i(TAG, "Alarm fired: $id")
    }

    companion object {
        const val TAG = "AlarmEngine"
        const val EXTRA_ALARM_ID = "io.github.emre_baltaci.alarmengine.ALARM_ID"
    }
}
