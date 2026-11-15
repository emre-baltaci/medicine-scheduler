package io.github.emre_baltaci.alarmengine

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSpecTest {
    private val spec = AlarmSpec(
        id = 1,
        triggerAtMillis = 1_000_000L,
        title = "Aspirin",
        body = "1 pill",
        lockedTitle = "Reminder",
    )

    private fun withLevel(level: LockScreenDetails) = spec.copy(lockScreenDetails = level)

    @Test
    fun `unlocked shows title and body at every level`() {
        for (level in LockScreenDetails.entries) {
            assertEquals("Aspirin", withLevel(level).titleFor(locked = false))
            assertEquals("1 pill", withLevel(level).bodyFor(locked = false))
        }
    }

    @Test
    fun `locked HIDDEN shows only the locked title`() {
        val s = withLevel(LockScreenDetails.HIDDEN)
        assertEquals("Reminder", s.titleFor(locked = true))
        assertEquals("", s.bodyFor(locked = true))
    }

    @Test
    fun `locked TITLE shows the title but no body`() {
        val s = withLevel(LockScreenDetails.TITLE)
        assertEquals("Aspirin", s.titleFor(locked = true))
        assertEquals("", s.bodyFor(locked = true))
    }

    @Test
    fun `locked FULL shows title and body`() {
        val s = withLevel(LockScreenDetails.FULL)
        assertEquals("Aspirin", s.titleFor(locked = true))
        assertEquals("1 pill", s.bodyFor(locked = true))
    }

    @Test
    fun `snoozed rings snoozeMinutes after now and keeps everything else`() {
        val s = spec.copy(snoozeMinutes = 7)
        val result = s.snoozed(now = 5_000_000L)
        assertEquals(5_420_000L, result.triggerAtMillis)
        assertEquals(s.copy(triggerAtMillis = 5_420_000L), result)
    }
}
