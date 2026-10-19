package io.github.emre_baltaci.alarmengine

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.core.content.ContextCompat

class AlarmActivity : Activity() {
    private var alarmId = -1
    private lateinit var detail: TextView

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showOverLockScreen()

        alarmId = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)

        val title = TextView(this).apply {
            text = "Medication reminder"
            textSize = 32f
            gravity = Gravity.CENTER
        }
        detail = TextView(this).apply {
            text = "Alarm $alarmId"
            textSize = 20f
            gravity = Gravity.CENTER
        }

        val snooze = Button(this).apply {
            text = "Snooze"
            setOnClickListener { AlarmActions.handle(this@AlarmActivity, AlarmActions.ACTION_SNOOZE, alarmId) }
        }

        val ok = Button(this).apply {
            text = "OK"
            setOnClickListener { AlarmActions.handle(this@AlarmActivity, AlarmActions.ACTION_OK, alarmId) }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(title)
            addView(detail)
            addView(snooze)
            addView(ok)
        }
        setContentView(layout)

        ContextCompat.registerReceiver(
            this,
            stopReceiver,
            IntentFilter(AlarmService.ACTION_RINGING_STOPPED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        alarmId = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)
        detail.text = "Alarm $alarmId"
    }

    override fun onDestroy() {
        unregisterReceiver(stopReceiver)
        super.onDestroy()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
