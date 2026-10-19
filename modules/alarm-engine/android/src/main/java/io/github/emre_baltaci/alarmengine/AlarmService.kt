package io.github.emre_baltaci.alarmengine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class AlarmService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val alarmAudio: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val id = intent?.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1) ?: -1

        createChannel()

        val screenIntent = Intent(this, AlarmActivity::class.java)
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val screenPending = PendingIntent.getActivity(
            this,
            id,
            screenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Medicine alarm")
            .setContentText("Alarm $id")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .addAction(0, "Snooze", actionPending(AlarmActions.ACTION_SNOOZE, id))
            .addAction(0, "OK", actionPending(AlarmActions.ACTION_OK, id))
            .setFullScreenIntent(screenPending, true)
            .setContentIntent(screenPending)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)

        if (player == null) startSound()
        if (vibrator == null) startVibration()

        Log.i(AlarmReceiver.TAG, "AlarmService started for alarm $id")
        return START_NOT_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(null, null)
            channel.enableVibration(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun actionPending(action: String, id: Int): PendingIntent {
        val intent = Intent(this, AlarmActionReceiver::class.java)
            .setAction(action)
            .putExtra(AlarmReceiver.EXTRA_ALARM_ID, id)
        return PendingIntent.getBroadcast(
            this,
            id,
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
