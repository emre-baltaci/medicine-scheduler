package io.github.emre_baltaci.alarmengine

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class AlarmActivity : Activity() {
    private lateinit var spec: AlarmSpec
    private lateinit var titleView: TextView
    private lateinit var bodyView: TextView
    private lateinit var snoozeButton: Button
    private lateinit var okButton: Button

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        titleView = TextView(this).apply {
            textSize = 32f
            gravity = Gravity.CENTER
        }
        bodyView = TextView(this).apply {
            textSize = 20f
            gravity = Gravity.CENTER
        }
        snoozeButton = Button(this).apply {
            setOnClickListener { AlarmActions.handle(this@AlarmActivity, AlarmActions.ACTION_SNOOZE, spec) }
        }
        okButton = Button(this).apply {
            setOnClickListener { AlarmActions.handle(this@AlarmActivity, AlarmActions.ACTION_OK, spec) }
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(titleView)
            addView(bodyView)
            addView(snoozeButton)
            addView(okButton)
        }
        setContentView(layout)
        show(AlarmSpec.from(intent))

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
        show(AlarmSpec.from(intent))
    }

    override fun onDestroy() {
        unregisterReceiver(stopReceiver)
        super.onDestroy()
    }

    private fun show(newSpec: AlarmSpec) {
        spec = newSpec
        titleView.text = spec.title
        bodyView.text = spec.body
        bodyView.visibility = if (spec.body.isEmpty()) View.GONE else View.VISIBLE
        snoozeButton.text = spec.snoozeLabel
        okButton.text = spec.okLabel
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
