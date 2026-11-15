package io.github.emre_baltaci.alarmengine

import org.junit.Assert.assertEquals
import org.junit.Test

class LockScreenDetailsTest {
    @Test
    fun `fromValue maps every value JS can send`() {
        assertEquals(LockScreenDetails.HIDDEN, LockScreenDetails.fromValue("hidden"))
        assertEquals(LockScreenDetails.TITLE, LockScreenDetails.fromValue("title"))
        assertEquals(LockScreenDetails.FULL, LockScreenDetails.fromValue("full"))
    }

    @Test
    fun `fromValue falls back to HIDDEN for null`() {
        assertEquals(LockScreenDetails.HIDDEN, LockScreenDetails.fromValue(null))
    }

    @Test
    fun `fromValue falls back to HIDDEN for unknown values`() {
        assertEquals(LockScreenDetails.HIDDEN, LockScreenDetails.fromValue("public"))
        assertEquals(LockScreenDetails.HIDDEN, LockScreenDetails.fromValue("FULL"))
        assertEquals(LockScreenDetails.HIDDEN, LockScreenDetails.fromValue(""))
    }
}
