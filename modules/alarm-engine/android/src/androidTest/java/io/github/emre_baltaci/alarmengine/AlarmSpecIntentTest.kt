package io.github.emre_baltaci.alarmengine

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmSpecIntentTest {
    @Test
    fun customSpecSurvivesIntentRoundTrip() {
        val spec = AlarmSpec(
            id = 42,
            triggerAtMillis = 1_800_000_000_000L,
            snoozeMinutes = 7,
            confirmationDelayMinutes = 0,
            autoStopMinutes = 15,
            title = "Aspirin",
            body = "1 pill",
            lockedTitle = "Reminder",
            snoozeLabel = "Later",
            okLabel = "Done",
            lockScreenDetails = LockScreenDetails.FULL,
        )
        assertEquals(spec, AlarmSpec.from(spec.putInto(Intent())))
    }

    @Test
    fun defaultSpecSurvivesIntentRoundTrip() {
        val spec = AlarmSpec(id = 1, triggerAtMillis = 1_800_000_000_000L)
        assertEquals(spec, AlarmSpec.from(spec.putInto(Intent())))
    }
}
