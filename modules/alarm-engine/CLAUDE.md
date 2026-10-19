# modules/alarm-engine/ — Native Alarm Module (D-10)

Our own Expo local module. It owns **all ring-time logic**: scheduling with `AlarmManager`, firing, the
alarm screen, sound, Snooze / OK, the follow-up confirmation (FR-2.4), auto-stop with the missed-alarm
notification (FR-2.8), the event queue to JS, and re-scheduling after reboot or time changes. JS/TS only
hands it the upcoming doses and reads back what happened. Android (Kotlin) now; iOS (Swift) later (D-1), added with
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

## Scheduling Rules
Platform facts and sources: `../../project-docs/sections/alarm-research.md` §2.2 and §2.4.
- **Time values:** `triggerAtMillis` is an absolute moment (epoch milliseconds, UTC). Any future time is
  allowed, not only today. Do time math in `Long` (`14L * 24 * 60 * 60 * 1000`); in `Int` it overflows.
- **Validate before scheduling:** a time in the past fires immediately, so reject it (or handle it on
  purpose).
- **Our storage is the source of truth.** `AlarmManager` is only the trigger and can lose alarms:
  - reboot clears all alarms
  - the user force-stopping the app cancels them until the app is opened again (can't be prevented)
  - revoking the exact-alarm permission cancels them
  - an app update may clear them

  Refill `AlarmManager` from storage on every event in §2.4: boot, locked boot, `MY_PACKAGE_REPLACED`,
  `TIME_SET`, `TIMEZONE_CHANGED` (API 37+: `TIMEZONE_OFFSET_CHANGED`), the permission being granted
  again, and every app open.
- **Per-alarm settings and texts come from JS.** JS passes them with each alarm (snooze, follow-up, and
  auto-stop minutes, FR-2.2 / FR-2.4 / FR-2.8; the follow-up can be off, FR-2.9; all user-visible texts,
  already translated, FR-4). They travel in the alarm's intent extras and are kept for a snooze. The
  engine has defaults (5 / 5 / 10 min) and English fallback texts only, and no language logic: JS is not
  running at ring time (D-10).
- **Events to JS go through a stored queue.** Every user action and outcome (OK, follow-up answer,
  missed) is written to the queue first, then sent as a live event if JS is running. JS reads the queue
  on launch and acknowledges each entry. Never rely on a live event alone: with no listener it is lost.
- **A swipe is never a decision (FR-2.4).** A notification's delete action is recorded as "no answer":
  the follow-up (and the missed alarm, which is a follow-up) asks again after its delay; a swiped ringing
  notification doesn't stop the ringing. Only Snooze / OK on the alarm and Yes / Not yet / Skipped on
  the follow-up are decisions; re-asks are unlimited.
- **Lock screen privacy (NFR-4):** while the phone is locked, notifications and the alarm screen show
  only the neutral texts by default; medicine names and doses only after unlock, unless the user's
  setting allows details on the lock screen.
- **Time zones:** an alarm fires at the same absolute moment, not at the same local clock time. For
  "07:00 local wherever the user is", recalculate the times on a time zone change (user choice: S-6 in
  `scheduling.md`).
