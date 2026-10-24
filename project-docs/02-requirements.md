# Requirements — medicine-scheduler

> Status: **Draft**. High-level requirements.
> Detailed requirements for each area will go in `sections/`.

## Functional Requirements

### FR-1 Scheduling
- FR-1.1 Support **every dose-change pattern**: increasing steps, decreasing steps, cycles, one-off
  changes, and so on. *(Exact list to be clarified in `sections/scheduling.md`.)*
- FR-1.2 Support **every timing pattern**: several times per day, specific weekdays, every N days,
  and so on. *(To be clarified.)*
- FR-1.3 Creating a schedule must be simple, using one basic, general building method that can still be
  fully customized.
- FR-1.4 Schedules must be editable over any scope:
  - a single dose (one time only)
  - from now on (all future doses)
  - a specific period (for example, one week or one month)
  - the whole plan
- FR-1.5 History of past doses and plans must be **stored** when a schedule is changed (D-7).
  - MVP: history is stored only. No screen lists it yet.
  - Later: history can be listed and viewed.
- FR-1.6 The user can **keep the current dose for a certain period** instead of moving to the next phase
  (S-7).
- FR-1.7 When the phone's time zone changes, the user **chooses** between keeping local clock times and
  keeping the real interval (S-6).

### FR-2 Alarms and Notifications *(scaffold, to be designed in detail)*
- FR-2.1 A due dose triggers an **alarm** (not just a normal notification).
- FR-2.2 The alarm can be **postponed (snoozed)**. The snooze duration has a default (5 min) and is
  **user-adjustable**.
- FR-2.3 **Pre-dose notification** to remind the user what they need (for example, water for a pill).
- FR-2.4 **Follow-up confirmation:** the alarm itself only offers **Snooze** and **OK** (OK means "seen",
  not "taken"). A user-adjustable time after OK (default 5 min; can be turned off), a quiet notification
  asks whether the medicine was taken: **Yes** / **Not yet** / **Skipped**, answered in the notification
  without opening the app. "Not yet" asks again after the same delay, as often as the user wants. Only a
  deliberate **Yes** or **Skipped** closes the dose; until then it stays **open** and is listed in the app
  (not as a blocking screen).
  - **A swipe is never a decision:** swiping any alarm-related notification away counts as "no answer".
    For the follow-up it acts like "Not yet"; the ringing alarm keeps ringing.
- FR-2.5 FR-2.1 and FR-2.3 are still proposals. They will be tested and kept or dropped during the
  detailed notification design.
- FR-2.6 Medicines due at the same time are **listed in one alarm** (S-5).
- FR-2.7 The user is **warned at each dose change point**, after the last dose of the current phase is
  taken (S-7, D-8).
- FR-2.8 A ringing alarm **stops by itself** after a user-adjustable time (default 10 min). It is then
  logged as missed and shown as a **missed-alarm notification**: the follow-up confirmation of FR-2.4 with
  "missed" text (Yes / Not yet / Skipped), so a swipe asks again and the dose stays open.
- FR-2.9 **Reminder behavior is customizable:** snooze, auto-stop, follow-up confirmation, and similar
  reminders have defaults and are user-adjustable in the app; extra reminders (such as the follow-up) can
  be turned off. The app passes these settings to `alarm-engine` with each alarm (D-10).

### FR-3 People / Profiles
- FR-3.1 v1 supports one person.
- FR-3.2 The data model must support several people from the start, so that multi-person tracking can
  be added later without migrating data.

### FR-4 Localization
- FR-4.1 Languages: English, Turkish, German.
- FR-4.2 Adding a language must only require adding translation resources, with no code changes.
- FR-4.3 Dates, times, and numbers are formatted for the user's locale.

### FR-5 App Security
- FR-5.1 **Optional app lock:** opening the app asks for the phone's biometrics or device credential
  (fingerprint, face, PIN, pattern), and again after an adjustable time in the background. Offered in
  onboarding. It **never blocks alarms**: ringing, the alarm screen, and notification actions work
  without it (NFR-3, D-10). If the phone has no screen lock, the app tells the user it can't be used.
- FR-5.2 **Re-authentication for privacy-reducing actions** (for example showing details on the lock
  screen, NFR-4, or exporting data): always required, with the same biometrics or device credential.
- FR-5.3 The app lock is not encryption. Data at rest relies on the phone's encryption; an encrypted
  database is decided in the architecture step (NFR-4).

## Non-Functional Requirements
- NFR-1 **Platforms**: **Android first** (D-1). iOS is **postponed, not dropped**. The architecture must stay
  iOS-ready (shared TypeScript code; a platform-neutral `alarm-engine` interface). If built, iOS minimum is 26 (D-9).
- NFR-2 **Offline-first**: all personal data stored on the phone. Alarms must work with no network.
- NFR-3 **Alarm reliability** is the top quality requirement. Alarms must fire on time even when the
  app is closed, after the phone restarts, and in battery-saver modes, as far as each OS allows.
- NFR-4 **Privacy**: medicine data is sensitive health data. It stays on the phone. This must stay
  compatible with a later store release (privacy policy, medical disclaimer, GDPR/KVKK).
  - **Lock screen:** while the phone is locked, notifications and the alarm screen show one of three
    levels, a user setting: **Hidden** (default: only a neutral text such as "Medication reminder"),
    **Title only**, or **Title and body**. When the phone is unlocked, title and body are shown.
    - Choosing a level other than Hidden needs re-authentication (FR-5.2) and shows a privacy warning
      ("anyone who sees your locked phone sees this"), plus a hint that Android's own lock-screen
      settings can still hide the content, with a button to the app's notification settings.
    - The app does not detect Android's global "hide sensitive content" setting (no public API); it may
      read its own notification channel's lock-screen visibility.
    - Verified on the Android 16 emulator (2026-10-24): by default the lock screen shows all
      notification content (`lock_screen_allow_private_notifications = 1`), so Android's public version
      applies only when the user hides sensitive content. The app enforces the levels itself; the
      stricter of the app and Android settings wins.
- NFR-5 **Extensibility**: must be ready for later features (dose-taken tracking, medicine catalog
  server, multiple people).

## Deferred / Open
- Units and amounts (half pills, ml, mg, drops, ...): implementation detail, to be discussed later.
- Exact list of scheduling patterns: to be clarified.
- Detailed notification design: to be done. It includes what happens when an open dose (FR-2.4) is still
  open at the medicine's next dose.
