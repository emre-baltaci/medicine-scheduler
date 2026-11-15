# TODO: Alarm proof of concept, part 3: confirmation flow (D-11)

> Task type: implementation. Created 2026-10-25 per R-5. Scope and pass criteria: `../sections/alarm-poc.md`
> (P-2, P-3). Part 2 and the carried-over items: `../CHANGELOG.md`, entry 2026-10-25.

## Goal
After the alarm, make sure the dose is confirmed: a follow-up notification asks whether the medicine was
taken, asks again on "Not yet" or a swipe, and an unanswered alarm stops by itself and becomes the same
confirmation as a missed alarm. JS gets live events for every answer (FR-2.4, FR-2.8, FR-2.9, NFR-4).

## Working Mode
- All code (Kotlin and TypeScript): Claude explains the concepts and shows the code in small pieces, with
  an explicit instruction per block ("replace the whole file", "add … below …"); the user types it;
  Claude reviews. Claude does not write code files itself.
- Claude may still run checks (compile, type check, adb, logs) and edit docs / task files.

## Security (R-8): MASVS groups touched
- **MASVS-PLATFORM:** new `PendingIntent`s (answers, delete intent), a new notification channel, scheduled
  follow-up alarms; all components stay `exported="false"`.
- **MASVS-PRIVACY:** the confirmation shows medicine texts, so the lock-screen levels apply (NFR-4); logs
  carry no texts.
- **MASVS-CODE:** validation of the new texts from JS.
- **MASVS-STORAGE:** none; nothing is stored in this part (the event queue is part 4).

## Checklist
- [x] 1. Create the first branch (now `feature/alarm-poc-tests`) and this TODO file
- [x] 1a. Kotlin test setup in `modules/alarm-engine/android/build.gradle` (JUnit, AndroidX Test); both test
      commands run (first tests: `LockScreenDetails.fromValue`, part of 1c; `AlarmSpec` intent round trip, part
      of 1d; passed on the Android 16 emulator, 2026-11-15)
- [x] 1b. Make the logic testable: validation and time calculations as pure logic that receives `now`
      (`AlarmScheduler.validate(spec, now)` internal, `AlarmSpec.snoozed(now)`)
- [x] 1c. JUnit tests: `titleFor` / `bodyFor` for all lock levels, validation limits, snooze time
      (`LockScreenDetails.fromValue`: done in 1a)
- [x] 1d. Instrumented tests: scheduling sets `nextAlarmClock`, the same id replaces the alarm, a short real
      alarm starts ringing (`AlarmSpec` intent round trip: done in 1a). The test app gets a test-only launcher
      activity. "No permission gives `ERR_EXACT_ALARM_DENIED`" can't run in-process (revoking the permission stops
      the app's process); it moved to the part 5 adb scenarios (`alarm-poc.md`). Run 2026-11-15, Android 16
      emulator: 15 / 15 JUnit, 5 / 5 instrumented, lint 0 errors (2 expected warnings: `showWhenLocked` /
      `turnScreenOn` are API 27+, older versions handled in code), `tsc` OK
- [ ] 1e. PR 1 (test foundation) from `feature/alarm-poc-tests`
- [ ] 1f. CI (D-14), PR 2 from `feature/alarm-poc-ci`: GitHub Actions workflow on every PR: `pnpm install`,
      JDK 17, `expo prebuild --platform android`, then unit tests + lint; instrumented tests on an emulator (one
      retry allowed); workflow permissions read-only
- [ ] 1g. New branch `feature/alarm-poc-confirmation` from `main` for items 2–7 (PR 3)
- [ ] 2. Follow-up confirmation notification `confirmationDelayMinutes` after OK (0 = off): Yes / Not yet /
      Skipped, on its own quiet channel, texts from JS with English fallbacks, lock-screen levels
- [ ] 3. Re-ask: "Not yet" and a swipe (delete intent) ask again after the delay, without limit; a swiped
      ringing notification keeps ringing
- [ ] 4. Auto-stop after `autoStopMinutes`: stop ringing and post the confirmation as a missed-alarm
      notification
- [ ] 5. Live events to JS (OK, Yes / Not yet / Skipped, missed): Expo `Events` + `sendEvent`, shown on
      the test screen; until part 4 they reach JS only while it runs
- [ ] 5a. Test run (R-9): unit tests, instrumented tests, lint, and `tsc`; record the results
- [ ] 6. Security check (R-8): MASVS-PLATFORM, MASVS-PRIVACY, MASVS-CODE
- [ ] 7. Move a summary to `CHANGELOG.md` (carry over the items for the next parts) and delete this TODO
      file (R-5)

## Notes
- PR split (three PRs, one topic each): tests (1–1e), CI (1f), confirmation flow (2–7). This TODO file travels
  with each PR; the changelog entry and its deletion come only in the last one (R-5).
- Testing (R-9): every item below 1d includes its tests in the same commit.
- The follow-up and missed-alarm notifications are posted by the engine, not JS (D-10): they must work
  while the app is closed.
- A swipe is never a decision (FR-2.4, `modules/alarm-engine/CLAUDE.md`). Only Yes or Skipped close a dose.
- The open question "a dose still open at the medicine's next dose" belongs to the detailed notification
  design (`02-requirements.md`, Deferred / Open); the PoC keeps one open confirmation per alarm id.
- The test button in `App.tsx` rings after 30 seconds with a 1-minute snooze.
