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

    // screen off → locked texts at once; unlock → title and body (NFR-4)
    private val lockStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> render(locked = true)
                Intent.ACTION_USER_PRESENT -> render(locked = false)
            }
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
        ContextCompat.registerReceiver(
            this,
            lockStateReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            },
            // exported: USER_PRESENT comes from SystemUI, not the system uid; both actions are protected broadcasts
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onResume() {
        super.onResume()
        render(isPhoneLocked())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        show(AlarmSpec.from(intent))
    }

    override fun onDestroy() {
        unregisterReceiver(stopReceiver)
        unregisterReceiver(lockStateReceiver)
        super.onDestroy()
    }

    private fun show(newSpec: AlarmSpec) {
        spec = newSpec
        snoozeButton.text = spec.snoozeLabel
        okButton.text = spec.okLabel
        render(isPhoneLocked())
    }

    private fun render(locked: Boolean) {
        val body = spec.bodyFor(locked)
        titleView.text = spec.titleFor(locked)
        bodyView.text = body
        bodyView.visibility = if (body.isEmpty()) View.GONE else View.VISIBLE
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
