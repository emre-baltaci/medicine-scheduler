package io.github.emre_baltaci.alarmengine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        Log.i(TAG, "Alarm fired: $id")

        val serviceIntent = Intent(context, AlarmService::class.java)
            .putExtra(EXTRA_ALARM_ID, id)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val TAG = "AlarmEngine"
        const val EXTRA_ALARM_ID = "io.github.emre_baltaci.alarmengine.ALARM_ID"
    }
}
