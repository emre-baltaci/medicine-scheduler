package io.github.emre_baltaci.alarmengine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat

class AlarmService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentSpec: AlarmSpec? = null

    private val alarmAudio: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    // screen off → locked texts at once; unlock → title and body (NFR-4)
    private val lockStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val spec = currentSpec ?: return
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> showNotification(spec, locked = true)
                Intent.ACTION_USER_PRESENT -> showNotification(spec, locked = false)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        val spec = AlarmSpec.from(intent)
        currentSpec = spec

        createChannel()
        showNotification(spec, isPhoneLocked())

        if (player == null) startSound()
        if (vibrator == null) startVibration()

        Log.i(AlarmReceiver.TAG, "AlarmService started for alarm ${spec.id}")
        return START_NOT_STICKY
    }

    private fun showNotification(spec: AlarmSpec, locked: Boolean) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(spec, locked), type)
    }

    private fun buildNotification(spec: AlarmSpec, locked: Boolean): Notification {
        val screenIntent = spec.putInto(Intent(this, AlarmActivity::class.java))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val screenPending = PendingIntent.getActivity(
            this,
            spec.id,
            screenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // what a lock screen shows when Android's own settings hide sensitive content
        val publicVersion = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(spec.lockedTitle)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(spec.titleFor(locked))
            .setContentText(spec.bodyFor(locked).ifEmpty { null })
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(0, spec.snoozeLabel, actionPending(AlarmActions.ACTION_SNOOZE, spec))
            .addAction(0, spec.okLabel, actionPending(AlarmActions.ACTION_OK, spec))
            .setFullScreenIntent(screenPending, true)
            .setContentIntent(screenPending)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(null, null)
            channel.enableVibration(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun actionPending(action: String, spec: AlarmSpec): PendingIntent {
        val intent = spec.putInto(Intent(this, AlarmActionReceiver::class.java).setAction(action))
        return PendingIntent.getBroadcast(
            this,
            spec.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun startSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(alarmAudio)
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(AlarmReceiver.TAG, "Alarm sound failed", e)
        }
    }

    private fun startVibration() {
        val v = getSystemService(Vibrator::class.java) ?: return
        // wait 0 ms, vibrate 800 ms, pause 800 ms; repeat from index 0
        val pattern = longArrayOf(0, 800, 800)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> v.vibrate(
                VibrationEffect.createWaveform(pattern, 0),
                VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                @Suppress("DEPRECATION") v.vibrate(VibrationEffect.createWaveform(pattern, 0), alarmAudio)

            else ->
                @Suppress("DEPRECATION") v.vibrate(pattern, 0)
        }
        vibrator = v
    }

    override fun onDestroy() {
        unregisterReceiver(lockStateReceiver)
        vibrator?.cancel()
        vibrator = null
        player?.release()
        player = null
        sendBroadcast(Intent(ACTION_RINGING_STOPPED).setPackage(packageName))
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "alarms"
        const val NOTIFICATION_ID = 1
        const val ACTION_RINGING_STOPPED = "io.github.emre_baltaci.alarmengine.RINGING_STOPPED"
    }
}
