package io.github.emre_baltaci.alarmengine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        AlarmActions.handle(context, intent.action, id)
    }
}
