package io.github.emre_baltaci.alarmengine

import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

class AlarmOptions : Record {
    @Field
    val snoozeMinutes: Int? = null

    @Field
    val confirmationDelayMinutes: Int? = null

    @Field
    val autoStopMinutes: Int? = null

    @Field
    val title: String? = null

    @Field
    val body: String? = null

    @Field
    val lockedTitle: String? = null

    @Field
    val snoozeLabel: String? = null

    @Field
    val okLabel: String? = null

    @Field
    val lockScreenDetails: LockScreenDetails? = null

    fun toSpec(id: Int, triggerAtMillis: Long) = AlarmSpec(
        id = id,
        triggerAtMillis = triggerAtMillis,
        snoozeMinutes = snoozeMinutes ?: AlarmSpec.DEFAULT_SNOOZE_MINUTES,
        confirmationDelayMinutes = confirmationDelayMinutes ?: AlarmSpec.DEFAULT_CONFIRMATION_DELAY_MINUTES,
        autoStopMinutes = autoStopMinutes ?: AlarmSpec.DEFAULT_AUTO_STOP_MINUTES,
        title = title ?: AlarmSpec.DEFAULT_TITLE,
        body = body ?: AlarmSpec.DEFAULT_BODY,
        lockedTitle = lockedTitle ?: AlarmSpec.DEFAULT_LOCKED_TITLE,
        snoozeLabel = snoozeLabel ?: AlarmSpec.DEFAULT_SNOOZE_LABEL,
        okLabel = okLabel ?: AlarmSpec.DEFAULT_OK_LABEL,
        lockScreenDetails = lockScreenDetails ?: LockScreenDetails.HIDDEN,
    )
}
