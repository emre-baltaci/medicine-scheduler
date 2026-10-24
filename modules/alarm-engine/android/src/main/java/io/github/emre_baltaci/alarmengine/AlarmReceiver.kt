package io.github.emre_baltaci.alarmengine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val spec = AlarmSpec.from(intent)
        Log.i(TAG, "Alarm fired: ${spec.id}")

        val serviceIntent = spec.putInto(Intent(context, AlarmService::class.java))
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val TAG = "AlarmEngine"
    }
}
