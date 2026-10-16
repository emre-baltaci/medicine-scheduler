# modules/alarm-engine/ — Native Alarm Module (D-10)

Our own Expo local module. It owns **all ring-time logic**: scheduling with `AlarmManager`, firing, the
alarm screen, sound, Snooze / Dismiss, and re-scheduling after reboot or time changes. JS/TS only hands it
the upcoming doses. Android (Kotlin) now; iOS (Swift) later (D-1), added with
`create-expo-module add-platform-support`. Scope of the current PoC: `../../project-docs/sections/alarm-poc.md`.

## Layout
| Path | Holds |
|---|---|
| `expo-module.config.json` | Autolinking entry: the Kotlin module class to register |
| `android/build.gradle` | Android library build; Kotlin package `io.github.emre_baltaci.alarmengine` |
| `android/src/main/AndroidManifest.xml` | Permissions, receivers, services, activities; merged into the app's manifest |
| `android/src/main/java/io/github/emre_baltaci/alarmengine/` | Kotlin sources (`AlarmEngineModule.kt` is the JS-facing module) |
| `src/AlarmEngineModule.ts` | The TypeScript face of the module (`requireNativeModule('AlarmEngine')`) |
| `src/AlarmEngine.types.ts` | Types shared by the TS side |

## Rules
- **Keep Kotlin and TypeScript in sync by hand.** TS types are not generated from Kotlin; a mismatch only
  shows at runtime. Every `Function` / `AsyncFunction` in `AlarmEngineModule.kt` has a matching declaration
  in `src/AlarmEngineModule.ts`.
- The name in `Name("AlarmEngine")` must equal the string in `requireNativeModule('AlarmEngine')`.
- **Native changes need a rebuild** (`pnpm expo run:android`); Fast Refresh only reloads JS.
- **Android Studio:** open the root `android/` folder, never `android/` inside this module (alone it cannot
  find the Android Gradle plugin).
- **Security (R-8):** every manifest entry is a MASVS-PLATFORM concern. Components are `exported="false"`
  unless the system must reach them, and every `PendingIntent` is `FLAG_IMMUTABLE` unless it has to be
  mutable.
