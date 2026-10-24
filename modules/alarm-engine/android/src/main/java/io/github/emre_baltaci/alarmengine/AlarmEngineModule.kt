package io.github.emre_baltaci.alarmengine

import android.content.Context
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class AlarmEngineModule : Module() {
    private val context: Context
        get() = appContext.reactContext ?: throw Exceptions.ReactContextLost()

    override fun definition() = ModuleDefinition {
        Name("AlarmEngine")

        Function("hello") { name: String ->
            "Hello $name! 👋"
        }

        Function("scheduleAlarm") { id: Int, triggerAtMillis: Long, options: AlarmOptions? ->
            val spec = (options ?: AlarmOptions()).toSpec(id, triggerAtMillis)
            AlarmScheduler.schedule(context, spec)
        }
    }
}
