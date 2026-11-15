package io.github.emre_baltaci.alarmengine

import expo.modules.kotlin.exception.CodedException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AlarmValidationTest {
    private val now = 1_000_000L
    private val valid = AlarmSpec(id = 1, triggerAtMillis = now + 60_000L)

    // every text that must not be blank, as a way to build a spec with that text changed
    private val requiredTexts: List<(String) -> AlarmSpec> = listOf(
        { valid.copy(title = it) },
        { valid.copy(lockedTitle = it) },
        { valid.copy(snoozeLabel = it) },
        { valid.copy(okLabel = it) },
    )

    private fun assertValid(spec: AlarmSpec) {
        AlarmScheduler.validate(spec, now) // an exception here fails the test
    }

    private fun assertRejected(spec: AlarmSpec, code: String = "ERR_INVALID_OPTION") {
        val e = assertThrows(CodedException::class.java) { AlarmScheduler.validate(spec, now) }
        assertEquals(code, e.code)
    }

    @Test
    fun `defaults are valid`() {
        assertValid(valid)
    }

    @Test
    fun `snoozeMinutes must be 1 to 120`() {
        assertValid(valid.copy(snoozeMinutes = 1))
        assertValid(valid.copy(snoozeMinutes = 120))
        assertRejected(valid.copy(snoozeMinutes = 0))
        assertRejected(valid.copy(snoozeMinutes = 121))
    }

    @Test
    fun `confirmationDelayMinutes must be 0 to 120`() {
        assertValid(valid.copy(confirmationDelayMinutes = 0))
        assertValid(valid.copy(confirmationDelayMinutes = 120))
        assertRejected(valid.copy(confirmationDelayMinutes = -1))
        assertRejected(valid.copy(confirmationDelayMinutes = 121))
    }

    @Test
    fun `autoStopMinutes must be 1 to 60`() {
        assertValid(valid.copy(autoStopMinutes = 1))
        assertValid(valid.copy(autoStopMinutes = 60))
        assertRejected(valid.copy(autoStopMinutes = 0))
        assertRejected(valid.copy(autoStopMinutes = 61))
    }

    @Test
    fun `required texts must be 1 to 200 characters and not blank`() {
        for (withText in requiredTexts) {
            assertValid(withText("a"))
            assertValid(withText("a".repeat(200)))
            assertRejected(withText(""))
            assertRejected(withText("   "))
            assertRejected(withText("a".repeat(201)))
        }
    }

    @Test
    fun `body may be empty and at most 200 characters`() {
        assertValid(valid.copy(body = ""))
        assertValid(valid.copy(body = "a".repeat(200)))
        assertRejected(valid.copy(body = "a".repeat(201)))
    }

    @Test
    fun `alarm time must be after now`() {
        assertValid(valid.copy(triggerAtMillis = now + 1))
        assertRejected(valid.copy(triggerAtMillis = now), "ERR_TIME_IN_PAST")
        assertRejected(valid.copy(triggerAtMillis = now - 1), "ERR_TIME_IN_PAST")
    }
}
