# Section: Alarm Proof of Concept

> Status: **Decided (scope)**. Test plan: to be written. Numbered items use the prefix **P-**.

## P-1 Purpose and Approach
- **Purpose (PoC):** prove on real Android devices that our own `alarm-engine` rings reliably: locked,
  silent, idle (Doze), app closed, after a restart. See `alarm-research.md` for why this needs proof.
- **Approach (tracer bullet):** a thin slice built at production quality.
  - The **`alarm-engine` code is kept** and grows into the real app's alarm module, so it is structured
    and tested properly from the start.
  - The **test screen is throwaway**. The real UI replaces it later.
- **Location:** inside the real project structure. The engine lives in `modules/alarm-engine/`; there is
  no separate PoC folder.

## P-2 Scope
**In scope**
1. Project skeleton: Expo SDK 57 + TypeScript, Android development build.
2. `alarm-engine` (Kotlin):
   - set, cancel, and list alarms (`AlarmManager.setAlarmClock`)
   - when an alarm fires: a full-screen alarm screen over the lock screen, a looping alarm-stream sound,
     and vibration
   - **Snooze** and **OK**
   - the **follow-up confirmation** notification after OK: Yes / Not yet / Skipped; a swipe counts as "no
     answer" and asks again (FR-2.4)
   - **auto-stop**, then the follow-up confirmation as a missed-alarm notification (FR-2.8)
   - an event queue to JS: live events while JS runs, read on launch, acknowledged by JS
   - snooze, follow-up, and auto-stop minutes and the texts passed per alarm from JS, with defaults
     (5 / 5 / 10 min, English texts); the follow-up can be turned off (FR-2.9)
   - neutral texts on the lock screen (NFR-4)
   - setting alarms again after a reboot, an app update, or a time / time zone change
   - permission checks (notifications, exact alarms, full-screen), with buttons that open the right
     settings pages
3. A tiny test screen (TypeScript): "ring in 1 minute", "ring at HH:MM", a list of pending alarms, and
   permission status.
4. Test plan, adb scenario scripts, and test runs (per D-13).

**Out of scope:** medicines, phases, and schedule logic; a database; languages; before and after
notifications; grouping medicines in one alarm; UI design; iOS.

## P-3 Pass Criteria
- In **every** scenario of the test plan, the alarm:
  - rings on time
  - shows the alarm screen
  - responds correctly to Snooze and OK, and the follow-up confirmation follows OK
- **Expected failures** (for example a force-stop on Android 15+, or a revoked permission) are **detected
  and reported** by the app, never silent.
- Results are recorded in the test plan below as a PASS / FAIL table per scenario and Android version.

## P-4 Test Plan
*(To be written: the scenario × Android version table, a verified adb command for each scenario, and how
each result is checked. See D-13.)*

## Open / TBD
- The exact tolerance for "on time" (for example within N seconds).
- Test plan: swipe cases (ringing notification, follow-up, missed alarm) must never close a dose.
