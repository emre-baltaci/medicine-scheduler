# Project Description — medicine-scheduler

> The user's project description: what the app is, its scope, and its context.

## Summary
A mobile app (Android first; iOS postponed, see D-1) for scheduling medicines, with alarms and reminders. Its main purpose is
to make **dose changes over time** easy to set up, follow, change, and get notified about.

## Core Idea
- A medicine schedule can change the dose at set points in time.
  - Example: take 1 pill per dose, then go up to 2 pills after 1 week.
- Scheduling must be **very flexible**: every dose-change pattern and every timing pattern must be possible.
- Adding a schedule must still be **simple**. The goal is a basic, easy-to-use way to build a schedule
  that can still be fully customized.
- A schedule must be **easy to change**, over any scope (this one dose, from now on, a given week or
  month, the whole plan).
- The user is **alerted** with an alarm when a dose is due, and gets helper notifications around it.

## Context
- **Personal project** for the user's own use first. It may be published to the Play Store (and the App Store once iOS is built)
  later if the result is good. Design decisions should not block publishing later (privacy, disclaimers,
  store rules).
- **One person** to start. Must be able to grow to **several people** (for example, family members)
  tracked in the same app.
- **Offline-first**: all personal data stays on the phone. No online storage needed for schedules or
  alarms.
- **Languages**: English, Turkish, German at launch. Adding languages later must be easy.

## Planned Later Features (not in initial scope)
- Tracking doses and amounts, so the user can confirm whether a medicine was taken.
- A catalog of medicines available on the local market, served from a **separate server** (read-only
  reference data; personal data stays on the phone).
- Tracking more people in the same app.
- More features to be defined later.
