# TODO: Alarm proof of concept, part 2: alarm screen, Snooze / OK, follow-up, auto-stop (D-11)

> Task type: implementation. Created 2026-10-19 per R-5. Scope and pass criteria: `../sections/alarm-poc.md`
> (P-2, P-3). Part 1 and its carried-over items: `../CHANGELOG.md`, entry 2026-10-18.

## Goal
Make a ringing alarm look and behave like a real alarm: a full-screen alarm screen over the lock screen,
Snooze and OK from the screen and the notification, a follow-up confirmation after OK, an auto-stop that
leaves a missed-alarm notification, per-alarm settings and texts from JS, and live events to JS
(FR-2.2, FR-2.4, FR-2.8, FR-2.9, NFR-4).

## Working Mode
- All code (Kotlin and TypeScript): Claude explains the concepts and shows the code in small pieces; the
  user types it; Claude reviews. Claude does not write code files itself.
- Claude may still run checks (compile, type check, adb, logs) and edit docs / task files.

## Security (R-8): MASVS groups touched
- **MASVS-PLATFORM:** the new activity (shown over the lock screen, `exported="false"`), full-screen intent,
  `PendingIntent` flags of the new actions, new notification channels.
- **MASVS-PRIVACY:** neutral texts on the lock screen by default (NFR-4).
- **MASVS-CODE:** validation of the new JS-facing parameters (minutes, texts).
- **MASVS-STORAGE:** none; nothing is stored in this part (the saved list and the event queue are part 3).

## Checklist
- [x] 1. Create branch `feature/alarm-poc-screen` and this TODO file
- [x] 2. Native alarm screen (Activity) over the lock screen, opened by the notification's full-screen
      intent
- [x] 3. One central action handler for Snooze and OK, used by the notification buttons and the alarm
      screen; Snooze sets the alarm again with the same settings
  - [x] 3a. `AlarmScheduler`: scheduling code shared by the module and the actions
  - [x] 3b. `AlarmActions` + `AlarmActionReceiver`; the notification offers Snooze / OK
  - [x] 3c. Snooze / OK on the alarm screen; the screen closes when ringing stops and handles a new alarm
- [ ] 4. Per-alarm settings and texts from JS with defaults: snooze (5 min), confirmation delay (5 min, 0 = off),
      auto-stop (10 min), English fallback texts; validated; neutral texts on the lock screen; no
      `SetTextI18n` lint warnings left (texts set without joining or literals)
  - [x] 4a. `AlarmSpec`: one data class with all values of an alarm, put into / read from intents
  - [x] 4b. `scheduleAlarm(id, time, options?)` from JS; validation in `AlarmScheduler`; TS types
  - [x] 4c. The spec travels receiver → service → actions → screen; Snooze uses its minutes; texts used
  - [ ] 4d. Lock screen levels (NFR-4): `lockScreenDetails` = hidden (default) / title / full; neutral
        while locked, title and body after unlock
    - [x] 4d-1. `LockScreenDetails` enum in `AlarmSpec`, `AlarmOptions`, and the TS type
    - [x] 4d-2. Notification: texts by lock state, neutral public version, re-shown on unlock
    - [ ] 4d-3. Alarm screen: texts by lock state, refreshed on unlock
  - [x] 4e. The test button passes options (1 min snooze for testing)
- [ ] 5. Follow-up confirmation notification after OK: Yes / Not yet / Skipped; "Not yet" and a swipe ask
      again after the delay (unlimited); a swiped ringing notification keeps ringing
- [ ] 6. Auto-stop: stop ringing and post the follow-up confirmation as a missed-alarm notification on its
      own channel
- [ ] 7. Live events to JS (OK, follow-up answers, missed), shown on the test screen
- [ ] 8. Security check (R-8): MASVS-PLATFORM, MASVS-PRIVACY, MASVS-CODE
- [ ] 9. Move a summary to `CHANGELOG.md` (carry over the items for the next parts) and delete this TODO
      file (R-5)

## Next Parts (carry over in step 9)
- **Part 3, persistence** (`feature/alarm-poc-persistence`):
  - cancel and list alarms; the saved alarm list as the source of truth
  - move the alarm data (`AlarmSpec`) into storage: intents then carry only the alarm id
  - the stored event queue (fired, snoozed, OK, follow-up answers, missed): JS reads it on launch and
    acknowledges each entry; the list of open doses in the app (FR-2.4)
  - set alarms again after reboot, locked boot, app update, time or time zone change, and the exact-alarm
    permission being granted again; pending follow-ups too
  - permission checks (notifications, exact alarms, full-screen) and settings buttons, available to JS;
    onboarding must block on `POST_NOTIFICATIONS`, since without it the alarm rings with no visible UI
  - full-screen intent: granted by default on the Android 16 emulator (sideloaded); app-ops showed one
    `USE_FULL_SCREEN_INTENT` reject ~7 s after the alarm screen opened (2026-10-19): find out why
  - tighten lint in `modules/alarm-engine/android/build.gradle` (`lint {}` block, fail on errors)
- **Architecture step:** Zod (or similar) for runtime validation in the TS layer, for UX (settings forms,
  stored or imported data), with friendly translated messages; the Kotlin checks in `AlarmScheduler` stay
  the gatekeeper. Consider `z.infer` for the TS types; the limits must match the Kotlin ones.
- **MVP (app UI):** the lock-screen setting with privacy warning and Android-settings hint (NFR-4); the
  app lock and re-authentication with `expo-local-authentication` (FR-5).
- **Part 4, testing:** the items listed under "part 3" in the CHANGELOG entry 2026-10-18 (test screen,
  test plan, test runs, security check).

## Notes
- Android 16 emulator (2026-10-24): `dumpsys audio` logs "AudioHardening background playback would be
  muted for io.github.emre_baltaci.medicinescheduler, level: full" when the alarm sound starts (warning
  only on 16). Android 17 enforces it (alarm-research §2.3): verify the exact-alarm + `USAGE_ALARM`
  exemption on the Android 17 emulator, and test with `adb shell cmd audio set-enable-hardening throw`.
- The notification stays when the alarm screen exists: Android requires it for the foreground service,
  and it is the fallback while the phone is in use or full-screen intent is off.
- The follow-up and missed-alarm notifications are posted by the engine, not JS (D-10): they must work
  while the app is closed.
- Until part 3, live events reach JS only while it runs; events while the app is closed are logged only.
- Most reminder behavior becomes user-adjustable in the app UI later (FR-2.9); the PoC only passes values.
- The test button in `App.tsx` rings after 30 seconds.
