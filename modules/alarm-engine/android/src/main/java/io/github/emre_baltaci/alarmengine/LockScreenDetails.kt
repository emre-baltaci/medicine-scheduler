package io.github.emre_baltaci.alarmengine

import expo.modules.kotlin.types.Enumerable

enum class LockScreenDetails(val value: String) : Enumerable {
    HIDDEN("hidden"),
    TITLE("title"),
    FULL("full");

    companion object {
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: HIDDEN
    }
}
