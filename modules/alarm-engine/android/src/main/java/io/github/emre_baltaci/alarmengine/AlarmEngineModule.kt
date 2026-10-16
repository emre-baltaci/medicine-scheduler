package io.github.emre_baltaci.alarmengine

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class AlarmEngineModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("AlarmEngine")

    Function("hello") { name: String ->
      "Hello $name! 👋"
    }
  }
}
