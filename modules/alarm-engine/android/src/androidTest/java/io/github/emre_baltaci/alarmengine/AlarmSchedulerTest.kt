package io.github.emre_baltaci.alarmengine

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmSchedulerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    @Before
    fun grantPermissions() {
        shell("appops set ${context.packageName} SCHEDULE_EXACT_ALARM allow")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        }
    }

    @After
    fun cleanUp() {
        cancelAlarm(TEST_ID)
        context.stopService(Intent(context, AlarmService::class.java))
    }

    @Test
    fun scheduleRegistersAnAlarmClock() {
        val time = System.currentTimeMillis() + 10 * 60_000L
        AlarmScheduler.schedule(context, AlarmSpec(id = TEST_ID, triggerAtMillis = time))

        val next = alarmManager.nextAlarmClock
        assertEquals(time, next?.triggerTime)
        assertEquals(context.packageName, next?.showIntent?.creatorPackage)
    }

    @Test
    fun sameIdReplacesTheAlarm() {
        val now = System.currentTimeMillis()
        AlarmScheduler.schedule(context, AlarmSpec(id = TEST_ID, triggerAtMillis = now + 10 * 60_000L))
        AlarmScheduler.schedule(context, AlarmSpec(id = TEST_ID, triggerAtMillis = now + 20 * 60_000L))

        // without replacement, the +10 min alarm would still be the next one
        assertEquals(now + 20 * 60_000L, alarmManager.nextAlarmClock?.triggerTime)
    }

    @Test
    fun realAlarmStartsRinging() {
        val spec = AlarmSpec(id = TEST_ID, triggerAtMillis = System.currentTimeMillis() + 3_000L)
        AlarmScheduler.schedule(context, spec)

        val notifications = context.getSystemService(NotificationManager::class.java)
        val deadline = System.currentTimeMillis() + 15_000L
        var ringing = false
        while (!ringing && System.currentTimeMillis() < deadline) {
            ringing = notifications.activeNotifications.any { it.id == AlarmService.NOTIFICATION_ID }
            if (!ringing) Thread.sleep(250)
        }
        assertTrue("alarm did not start ringing within 15 s", ringing)
    }

    // runs a command with adb shell rights and waits until it has finished
    private fun shell(command: String) {
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }

    // the engine has no cancel function yet (part 4); match the alarm's PendingIntent and cancel it
    private fun cancelAlarm(id: Int) {
        val pending = PendingIntent.getBroadcast(
            context,
            id,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    companion object {
        private const val TEST_ID = 9_001
    }
}
