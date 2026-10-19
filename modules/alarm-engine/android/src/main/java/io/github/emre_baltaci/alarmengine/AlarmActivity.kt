package io.github.emre_baltaci.alarmengine

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class AlarmActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val id = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)

        val title = TextView(this).apply {
            text = "Medication reminder"
            textSize = 32f
            gravity = Gravity.CENTER
        }
        val detail = TextView(this).apply {
            text = "Alarm $id"
            textSize = 20f
            gravity = Gravity.CENTER
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(title)
            addView(detail)
        }
        setContentView(layout)
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
