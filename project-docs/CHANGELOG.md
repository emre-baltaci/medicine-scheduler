# Change Log — medicine-scheduler

> Records **project work** (not app release notes), newest entry first. Filled per rule R-5: when a task's
> TODO checklist is complete, its summary is added here and the TODO file is deleted.
> Decisions are only referenced by D-number; details are in `05-decisions-log.md`.

## Entry format
```markdown
## YYYY-MM-DD — <task title>
**Type:** documentation | implementation
**Summary:** 2–3 sentences on what was done and why.
**Decisions:** D-x, D-y … (details in 05-decisions-log.md)
**Files:** created / changed / deleted
**Notes:** open items, follow-ups, anything useful for tracing back
```

---

## 2026-10-25 — Alarm proof of concept, part 2: alarm screen, Snooze / OK, settings, lock-screen privacy
**Type:** implementation + documentation

**Summary:** A ringing alarm now opens a full-screen alarm screen over the lock screen with Snooze and OK,
which work the same from the notification through one central action handler. Each alarm carries its
own settings and texts from JS (`AlarmSpec`), validated in Kotlin and kept on Snooze. While the phone is
locked, the notification and the alarm screen show only what the lock-screen level allows (neutral by
default). The confirmation flow (follow-up, auto-stop, live events) moved to part 3, so the PoC now has
five parts.

**Decisions:** D-10, D-11 (no new D-numbers). Requirements: FR-2.2 and FR-2.4 rewritten (Snooze / OK,
follow-up confirmation with unlimited re-asks, a swipe is never a decision), new FR-2.8 (auto-stop with a
missed-alarm notification), FR-2.9 (customizable reminder behavior), NFR-4 lock-screen levels, new FR-5
(app security). Within the task: plain Android Views for the PoC alarm screen; texts come only from JS,
the engine has English fallbacks; the option is named `confirmationDelayMinutes`.

**Files:**
- created: `AlarmActivity.kt`, `AlarmActions.kt`, `AlarmActionReceiver.kt`, `AlarmScheduler.kt`,
  `AlarmSpec.kt`, `AlarmOptions.kt`, `LockScreenDetails.kt`, `LockState.kt` (all in
  `modules/alarm-engine/android/.../alarmengine/`)
- changed: `AlarmEngineModule.kt`, `AlarmReceiver.kt`, `AlarmService.kt`, the module manifest, the TS
  module and types, `App.tsx` (test options); docs: root `CLAUDE.md` (work order, five parts),
  `02-requirements.md`, `sections/alarm-poc.md`, `sections/alarm-research.md`, `sections/scheduling.md`,
  `modules/alarm-engine/CLAUDE.md` (engine rules)
- deleted: `tasks/2026-10-19-alarm-poc-part-2-screen.md`

**Notes:**
- Security check (R-8), passed:
  - MASVS-PLATFORM: all four components `exported="false"` in the merged manifest; all five
    `PendingIntent`s `FLAG_IMMUTABLE`; intents explicit or package-limited; the app-internal
    `RINGING_STOPPED` receiver not exported; the lock-state receivers exported but limited to protected
    broadcasts (`USER_PRESENT` comes from SystemUI, so a not-exported receiver never gets it).
  - MASVS-PRIVACY: lock-screen levels on notification and screen; logs carry only the alarm id and
    minutes. Accepted until part 4: texts travel inside the alarm's `PendingIntent` (system-only).
  - MASVS-CODE: JS input validated in `AlarmScheduler` (future time, minute ranges, text length; the
    enum by Expo); lint has no errors.
- Carried over to **part 3, confirmation flow** (`feature/alarm-poc-confirmation`):
  - follow-up confirmation notification after OK: Yes / Not yet / Skipped; "Not yet" and a swipe ask
    again after `confirmationDelayMinutes` (unlimited; 0 = off); a swiped ringing notification keeps
    ringing
  - auto-stop after `autoStopMinutes`: stop ringing and post the confirmation as a missed-alarm
    notification on its own channel
  - live events to JS (OK, follow-up answers, missed), shown on the test screen; until part 4 they reach
    JS only while it runs
  - texts for the new notifications come from JS, like the alarm texts; lock-screen levels apply
- Carried over to **part 4, persistence** (`feature/alarm-poc-persistence`):
  - cancel and list alarms; the saved alarm list as the source of truth; move `AlarmSpec` into storage so
    intents carry only the alarm id
  - the stored event queue (fired, snoozed, OK, follow-up answers, missed): JS reads it on launch and
    acknowledges each entry; the list of open doses in the app (FR-2.4)
  - set alarms again after reboot, locked boot, app update, time or time zone change, and the exact-alarm
    permission being granted again; pending follow-ups too
  - permission checks (notifications, exact alarms, full-screen) and settings buttons, available to JS;
    onboarding must block on `POST_NOTIFICATIONS`, since without it the alarm rings with no visible UI
  - full-screen intent: granted by default on the Android 16 emulator (sideloaded); app-ops showed one
    `USE_FULL_SCREEN_INTENT` reject ~7 s after the alarm screen opened (2026-10-19): find out why
  - tighten lint in `modules/alarm-engine/android/build.gradle` (`lint {}` block, fail on errors)
- Carried over to **part 5, testing**:
  - on-time tolerance for the pass criteria (P-4); the test-plan items in `sections/alarm-poc.md`
    (swipe cases, sound with the screen on and off)
  - a force stop cancels all alarms by design; simulate a system kill with `adb shell am kill <package>`
    or a swipe
  - full-screen intent on other Android versions and the physical phone (on by default on Android 16)
  - Android 17 audio hardening: Android 16 logs "background playback would be muted … level: full" when
    the alarm sound starts (warning only); verify the exact-alarm + `USAGE_ALARM` exemption on Android 17
    and test with `adb shell cmd audio set-enable-hardening throw`
  - `pnpm audit` (2026-10-14): `braces` 3.0.3 (high), `node-forge` 1.4.0 (high), `uuid` 7.0.3 (moderate),
    all in Expo / Metro dev tooling, no fix; accepted, re-check
  - the Expo template adds `INTERNET`, `SYSTEM_ALERT_WINDOW`, `READ/WRITE_EXTERNAL_STORAGE`; block them
    for release builds (`android.blockedPermissions`), since the app is offline-first
- For the **architecture step**: Zod (or similar) for runtime validation in the TS layer (settings forms,
  stored or imported data, translated messages), Kotlin stays the gatekeeper, limits must match; consider
  `z.infer` for the TS types; Compose vs. Views for the real alarm screen; pnpm `nodeLinker: hoisted`
  (reconsider isolated mode).
- For the **MVP (app UI)**: the lock-screen setting with privacy warning and Android-settings hint
  (NFR-4); the app lock and re-authentication with `expo-local-authentication` (FR-5).
- Emulator: give it a PIN to test lock-screen behavior (it has none by default). It crashed twice in its
  graphics backend after re-installs (host-side bug); the lock screen flashes briefly before the alarm
  screen occludes it (Android's normal animation).

---

## 2026-10-18 — Alarm proof of concept, part 1: an alarm rings and can be stopped
**Type:** implementation + setup

**Summary:** The PoC (D-11) is split into three tasks so each milestone gets its own pull request. Part 1
sets up the Android toolchain and the Expo SDK 57 skeleton, and builds the first slice of `alarm-engine`:
JS schedules an exact alarm clock, and when it fires a foreground service loops the alarm sound and
vibration until the user taps Stop. Tested on the Android 16 emulator with the app open, swiped away, and
with its process killed.

**Decisions:** D-2, D-10, D-11 (no new D-numbers). Within the task: foreground service type
`systemExempted` (alarm-research §2.3); scheduling rules in `modules/alarm-engine/CLAUDE.md`.

**Files:**
- created: the Expo app skeleton (`App.tsx`, `app.json`, `package.json`, `pnpm-workspace.yaml`, …),
  `modules/alarm-engine/` (`AlarmEngineModule.kt`, `AlarmReceiver.kt`, `AlarmService.kt`, manifest,
  TS module, `CLAUDE.md`), `.editorconfig`, `.prettierrc`
- changed: root `CLAUDE.md` (folder list, work order), `sections/alarm-research.md` (Play full-screen
  intent policy)
- deleted: `tasks/2026-10-14-alarm-poc.md`

**Notes:**
- Security check (R-8): MASVS-PLATFORM checked in the merged manifest: receiver and service are
  `exported="false"`, all intents explicit, all `PendingIntent`s `FLAG_IMMUTABLE`. MASVS-STORAGE: not
  touched yet. MASVS-CODE: see the audit and lint items below.
- Carried over to part 2 (screen, persistence, re-arm, permissions):
  - open items: snooze duration (proposed 5 min, 1 min for testing); auto-stop timeout when nobody
    reacts (proposed 10 min)
  - step 7 direction: native alarm screen; one central action handler for Snooze / Taken / Stop, used by
    the notification buttons and the alarm screen
  - onboarding must block on `POST_NOTIFICATIONS`: without it the alarm rings with no visible UI
  - `alarm-engine/android/build.gradle` has `lintOptions { abortOnError false }` from the template;
    tighten it (current `lint {}` block)
- Carried over to part 3 (test screen, test plan, test runs, security):
  - on-time tolerance for the pass criteria (P-4)
  - test plan: a force stop cancels all alarms by design; simulate a system kill with
    `adb shell am kill <package>` or a swipe
  - check whether full-screen intent is on by default for a sideloaded install
  - `pnpm audit` (2026-10-14): `braces` 3.0.3 (high), `node-forge` 1.4.0 (high), `uuid` 7.0.3 (moderate),
    all in Expo / Metro dev tooling, no fix; accepted, re-check
  - the Expo template adds `INTERNET`, `SYSTEM_ALERT_WINDOW`, `READ/WRITE_EXTERNAL_STORAGE`; block them
    for release builds (`android.blockedPermissions`), since the app is offline-first
  - pnpm `nodeLinker: hoisted`; reconsider isolated mode in the architecture step
- Emulator: it crashed twice inside Android Studio's Running Devices panel; the standalone window is
  stable. Android Studio and the emulator must run on the NVIDIA GPU.

---

## 2026-10-11 — End-of-reply order for the glossary list and the TODO list
**Type:** documentation + setup

**Summary:** R-3 and R-5 both claimed the end of a reply. The glossary list now appears only after a work
step and comes before the TODO list, which is always last. This closes the Claude setup audit.

**Decisions:** none.

**Files:** changed `04-rules-and-conventions.md` (R-3 glossary wording, new order line), the root
`CLAUDE.md` (glossary row), and `tasks/CLAUDE.md` (life cycle step 4).

**Notes:**
- Security check (R-8): none; documentation wording and personal config cleanup, no app code.
- A `pre-push` hook and a pull-request workflow were considered and dropped: the GitHub ruleset
  "No direct pushes to main" already protects the remote (pull request required, no force push, no
  deletion, no bypass).
- Personal Claude config, outside the repo: a stale editor swap file and an old settings backup were
  removed.

---

## 2026-10-11 — Security rule R-8 (OWASP MASVS), root CLAUDE.md trimmed
**Type:** documentation + setup

**Summary:** Security became a project rule: R-8 makes it part of every task, with OWASP MASVS as the
standard, because the earlier security rule lived in the user's global instructions and named a web
standard (ASVS). The root `CLAUDE.md` lost two lines that restated automatic behaviour.

**Decisions:** none (R-8 is a rule, not a D-decision).

**Files:**
- changed `04-rules-and-conventions.md` (new R-8)
- changed the root `CLAUDE.md` (R-8 summary; "R-1 to R-8"; removed the "Also: global rules" line and
  Session Start step 1)
- changed `tasks/CLAUDE.md` (security check item in the TODO template)

**Notes:**
- Security check (R-8): none; documentation only.
- Open item: R-8 names no strictness level. Choose a MAS profile when the first security-relevant code
  is written.
- Personal Claude config, outside the repo: the global instructions now hold only rules that apply to
  every project (their OWASP section was removed), and two memory notes that repeated the repo were
  deleted.
- Open audit items, not done: the end-of-reply order of the glossary list and the TODO list, and a
  `pre-push` hook to match the pre-commit hook.

---

## 2026-10-11 — Claude setup fixes: rule scope for automated runs
**Type:** documentation + setup

**Summary:** An audit of the Claude instructions and setup found three broken items, fixed here. R-3 and
R-5 now state that they apply to interactive sessions only, because the GitHub Actions workflows read the
same rules and have no one to wait for.

**Decisions:** none.

**Files:** changed the root `CLAUDE.md` (Scope line under "Strict Rules") and
`04-rules-and-conventions.md` (new "Scope: interactive and automated sessions" subsection).

**Notes:**
- Personal Claude config, outside the repo: the OWASP ASVS MCP server was registered (the global rule
  referred to it, but it was not connected), and the duplicate context7 plugin was disabled.
- Open audit items, not done: redundant instructions and memory notes, the end-of-reply order of the
  glossary list and the TODO list, and a `pre-push` hook to match the pre-commit hook.

---

## 2026-09-27 — Rename to medicine-scheduler, connect GitHub, first main
**Type:** setup

**Summary:** The project name was aligned with the GitHub repo (`medicine-scheduler`) and the remote
`origin` was connected. The first `main` was formed by a one-time push of `chore/project-setup`, because
the repo was empty and a PR needs an existing target branch.

**Decisions:** none.

**Files:** changed the title lines of the root `CLAUDE.md`, `01-project-description.md`,
`02-requirements.md`, `04-rules-and-conventions.md`, `05-decisions-log.md`, `CHANGELOG.md`.

**Notes:** Next steps for the user: GitHub branch protection for `main`, and the PR review extension.

---

## 2026-09-27 — Glossary, privacy cleanup, PoC decisions, git setup
**Type:** documentation + setup

**Summary:**
- Added a personal, git-ignored glossary for the user's learning.
- Removed personal data from the public docs, since the repo will be public as a portfolio of
  AI-assisted development. `project-docs/` and the CLAUDE.md files stay public.
- Recorded the proof-of-concept decisions: a tracer bullet, where the engine is kept and the test screen is
  throwaway.
- Initialized git with a pre-commit hook that blocks commits on `main`/`master`.

**Decisions:** D-11 rewritten (tracer bullet, `modules/alarm-engine/`, scope in `sections/alarm-poc.md`);
D-13 points to the test plan in `alarm-poc.md`; D-1 reason phrased without personal details.

**Rules changed:**
- R-2 excludes obvious tool/config folders (for example `.githooks/`).
- R-3 glossary rule: Claude lists a few candidate terms at the end of a response; only the terms the user
  picks are added.
- R-5: every reply during a task ends with the TODO list (✅/⬜/➡️); the change log entry is written and the
  completed TODO file is deleted **before** committing.
- R-7: personal data may be removed from the change log on the user's request.

**Files:**
- created `glossary.md` (git-ignored), `sections/alarm-poc.md` (prefix P-), `.githooks/pre-commit`,
  `.gitignore`
- changed root `CLAUDE.md` (work order, navigation, R-2/R-5 summaries, repo setup section),
  `04-rules-and-conventions.md`, `05-decisions-log.md`, `sections/CLAUDE.md`, `tasks/CLAUDE.md`,
  `sections/alarm-research.md`, `CHANGELOG.md` (personal details removed from earlier entries)

**Notes:**
- Git: repo initialized with default branch `main`; work happens on `chore/project-setup`. Hook activation
  per clone: `git config core.hooksPath .githooks`. The hook was tested in a throwaway repo (blocked on
  `main`, allowed on a feature branch).
- `.gitignore` ignores the root-level generated `/android/` and `/ios/` (Expo prebuild) but not
  `modules/alarm-engine/android/`; verified with `git check-ignore`.
- Exact device and machine details are kept outside the repo, in Claude's private memory.
- Later: GitHub branch protection for `main` once the repo is pushed.
- Next in the work order: plan the alarm PoC implementation (test plan P-4, environment setup).

---

## 2026-09-27 — Project docs and CLAUDE.md setup
**Type:** documentation (first task run through the R-5 TODO process)

**Summary:**
- Put the working rules into practice: a change log, a compact root `CLAUDE.md` with the work order and
  a navigation map, and a `CLAUDE.md` in every folder we created.
- Introduced R-7: docs show the current state only, and history lives only here.
- Ran a full consistency pass for "Android first, iOS postponed".

**Decisions:**
- D-12 was merged into D-1. D-1 now reads "Android first; iOS postponed, not dropped"; the number D-12 is
  retired.
- D-2 and D-10 were reworded: Kotlin now, Swift later.

**Files:**
- created `CLAUDE.md` (root), `project-docs/CLAUDE.md`, `project-docs/sections/CLAUDE.md`,
  `project-docs/tasks/CLAUDE.md`, `project-docs/CHANGELOG.md`
- deleted `project-docs/README.md`: its index duplicated the CLAUDE.md files, and its work order moved to
  the root `CLAUDE.md` ("Current Phase and Next Steps")
- changed `04-rules-and-conventions.md`: R-1 to R-5 clarified, R-7 added, rule change history table
  removed (per R-7)
- changed `05-decisions-log.md`: rewritten as current-state only
- changed `01-project-description.md`: Play Store first; App Store once iOS is built
- changed `02-requirements.md`: NFR-1 made Android first; added FR-1.6 (hold the current dose), FR-1.7
  (time zone choice), FR-2.6 (grouped alarm), FR-2.7 (change-point warning)
- changed `sections/scheduling.md`: S- prefix declared, items reordered, open items gathered in
  Open / TBD
- changed `sections/alarm-research.md`: iOS <26 fallback and risk removed, iOS parts marked "later", scope
  stated in the header

**Notes:**
- Claude's memory notes were updated to match R-7 and the removal of README.
- Next in the work order: alarm proof of concept (D-11).

---

## 2026-09-27 — Project kickoff: description, requirements, alarm research
**Type:** documentation (done before the R-5 TODO process existed)

**Summary:**
- Started the project with a docs folder as the single source of truth.
- Captured the project description, the first requirements, and the first scheduling decisions.
- Ran a verified research on real alarms (Android, iOS, React Native/Expo). It concluded that real
  alarms are feasible with our own native alarm module.
- Chose Android first (iOS postponed) and a proof of concept as the first build step.

**Decisions:** D-1 to D-13. Scheduling decisions S-1 to S-7 are in `sections/scheduling.md`.

**Files:**
- created `project-docs/README.md`
- created `01-project-description.md`
- created `02-requirements.md`
- created `04-rules-and-conventions.md` (R-1 to R-6)
- created `05-decisions-log.md`
- created `sections/scheduling.md`
- created `sections/alarm-research.md`

**Notes:**
- Research method: three research agents with official sources, and 14+ key claims re-checked directly.
  Labels: [V✓] / [V] / [V-AOSP] / [U].
- Open items:
  - S-4: Claude must explain "calendar-based" in detail later.
  - S-7: when the current dose is extended, do later phases shift? What form does the warning take?
  - S-1: the full list of ways a phase can end.
  - S-6: time zone choice for all medicines or per medicine.
  - Units and amounts: deferred.
  - Detailed notification design: pending.
- Test devices: a physical OEM Android phone plus the Android Emulator (Android 14–17).
- `03-architecture.md` is not written yet.
