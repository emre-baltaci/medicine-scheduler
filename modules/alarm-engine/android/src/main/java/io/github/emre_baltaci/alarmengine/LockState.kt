package io.github.emre_baltaci.alarmengine

import android.app.KeyguardManager
import android.content.Context

// Treats "unknown" as locked: when in doubt, show less.
fun Context.isPhoneLocked(): Boolean =
    getSystemService(KeyguardManager::class.java)?.isKeyguardLocked ?: true
