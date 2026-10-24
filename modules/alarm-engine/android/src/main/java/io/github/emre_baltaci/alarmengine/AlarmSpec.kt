package io.github.emre_baltaci.alarmengine

import android.content.Intent

data class AlarmSpec(
    val id: Int,
    val triggerAtMillis: Long,
    val snoozeMinutes: Int = DEFAULT_SNOOZE_MINUTES,
    val confirmationDelayMinutes: Int = DEFAULT_CONFIRMATION_DELAY_MINUTES,
    val autoStopMinutes: Int = DEFAULT_AUTO_STOP_MINUTES,
    val title: String = DEFAULT_TITLE,
    val body: String = DEFAULT_BODY,
    val lockedTitle: String = DEFAULT_LOCKED_TITLE,
    val snoozeLabel: String = DEFAULT_SNOOZE_LABEL,
    val okLabel: String = DEFAULT_OK_LABEL,
    val lockScreenDetails: LockScreenDetails = LockScreenDetails.HIDDEN
) {
    fun putInto(intent: Intent): Intent = intent
        .putExtra(EXTRA_ID, id)
        .putExtra(EXTRA_TRIGGER_AT, triggerAtMillis)
        .putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes)
        .putExtra(EXTRA_CONFIRMATION_DELAY_MINUTES, confirmationDelayMinutes)
        .putExtra(EXTRA_AUTO_STOP_MINUTES, autoStopMinutes)
        .putExtra(EXTRA_TITLE, title)
        .putExtra(EXTRA_BODY, body)
        .putExtra(EXTRA_LOCKED_TITLE, lockedTitle)
        .putExtra(EXTRA_SNOOZE_LABEL, snoozeLabel)
        .putExtra(EXTRA_OK_LABEL, okLabel)
        .putExtra(EXTRA_LOCK_SCREEN_DETAILS, lockScreenDetails.value)

    fun titleFor(locked: Boolean): String =
        if (locked && lockScreenDetails == LockScreenDetails.HIDDEN) lockedTitle else title

    fun bodyFor(locked: Boolean): String =
        if (locked && lockScreenDetails != LockScreenDetails.FULL) "" else body
    
    companion object {
        const val DEFAULT_SNOOZE_MINUTES = 5
        const val DEFAULT_CONFIRMATION_DELAY_MINUTES = 5 // 0 = confirmation off
        const val DEFAULT_AUTO_STOP_MINUTES = 10

        // English fallbacks; JS passes translated texts (FR-4)
        const val DEFAULT_TITLE = "Medication reminder"
        const val DEFAULT_BODY = ""
        const val DEFAULT_LOCKED_TITLE = "Medication reminder"
        const val DEFAULT_SNOOZE_LABEL = "Snooze"
        const val DEFAULT_OK_LABEL = "OK"

        private const val PREFIX = "io.github.emre_baltaci.alarmengine."
        const val EXTRA_ID = PREFIX + "ALARM_ID"
        private const val EXTRA_TRIGGER_AT = PREFIX + "TRIGGER_AT"
        private const val EXTRA_SNOOZE_MINUTES = PREFIX + "SNOOZE_MINUTES"
        private const val EXTRA_CONFIRMATION_DELAY_MINUTES = PREFIX + "CONFIRMATION_DELAY_MINUTES"
        private const val EXTRA_AUTO_STOP_MINUTES = PREFIX + "AUTO_STOP_MINUTES"
        private const val EXTRA_TITLE = PREFIX + "TITLE"
        private const val EXTRA_BODY = PREFIX + "BODY"
        private const val EXTRA_LOCKED_TITLE = PREFIX + "LOCKED_TITLE"
        private const val EXTRA_SNOOZE_LABEL = PREFIX + "SNOOZE_LABEL"
        private const val EXTRA_OK_LABEL = PREFIX + "OK_LABEL"
        private const val EXTRA_LOCK_SCREEN_DETAILS = PREFIX + "LOCK_SCREEN_DETAILS"

        fun from(intent: Intent) = AlarmSpec(
            id = intent.getIntExtra(EXTRA_ID, -1),
            triggerAtMillis = intent.getLongExtra(EXTRA_TRIGGER_AT, 0L),
            snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES),
            confirmationDelayMinutes = intent.getIntExtra(
                EXTRA_CONFIRMATION_DELAY_MINUTES,
                DEFAULT_CONFIRMATION_DELAY_MINUTES
            ),
            autoStopMinutes = intent.getIntExtra(EXTRA_AUTO_STOP_MINUTES, DEFAULT_AUTO_STOP_MINUTES),
            title = intent.getStringExtra(EXTRA_TITLE) ?: DEFAULT_TITLE,
            body = intent.getStringExtra(EXTRA_BODY) ?: DEFAULT_BODY,
            lockedTitle = intent.getStringExtra(EXTRA_LOCKED_TITLE) ?: DEFAULT_LOCKED_TITLE,
            snoozeLabel = intent.getStringExtra(EXTRA_SNOOZE_LABEL) ?: DEFAULT_SNOOZE_LABEL,
            okLabel = intent.getStringExtra(EXTRA_OK_LABEL) ?: DEFAULT_OK_LABEL,
            lockScreenDetails = LockScreenDetails.fromValue(intent.getStringExtra(EXTRA_LOCK_SCREEN_DETAILS))
        )
    }
}
