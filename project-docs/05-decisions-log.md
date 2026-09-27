# Decisions Log — medicine-scheduler

> Current decisions only (R-7). When a decision changes, its row is rewritten in place; the history of
> changes is in `CHANGELOG.md`. Numbers are never reused; gaps mean a decision was merged or removed.

| # | Date | Decision | Reason | Status |
|---|------|----------|--------|--------|
| D-1 | 2026-09-27 | **Android first.** iOS is postponed, not dropped. The architecture stays iOS-ready: shared TypeScript code and a platform-neutral `alarm-engine` interface. Only the Android half is built now | iOS build prerequisites aren't available yet (Apple Developer Program membership, a Mac that can run Xcode 26.4+, an iPhone on iOS 26+) | Accepted |
| D-2 | 2026-09-27 | React Native + Expo (development builds) + TypeScript; small native modules for alarms | One codebase that can later cover iOS too; real alarms need native OS APIs | Accepted |
| D-3 | 2026-09-27 | Personal data stored only on the phone (offline-first) | Alarms need no server; health data privacy | Accepted |
| D-4 | 2026-09-27 | Future medicine catalog on a separate server | Reference data only, kept separate from personal data | Accepted (future) |
| D-5 | 2026-09-27 | Start with one person; data model supports several | Room to grow without migrating data | Accepted |
| D-6 | 2026-09-27 | Languages: EN, TR, DE; localization built to scale | User requirement | Accepted |
| D-7 | 2026-09-27 | Past doses and plan changes are always stored; a history screen comes after the MVP | Needed for later tracking features; keeps the MVP small | Accepted |
| D-8 | 2026-09-27 | Phase-change warning is shown after the last dose of the current phase is taken | User decision (S-7) | Accepted |
| D-9 | 2026-09-27 | When iOS is built, the minimum iOS version is 26 (iPhone 11 / SE 2nd gen and newer) | Real alarms (AlarmKit) on every supported iPhone; no weak fallback | Accepted (applies when iOS starts) |
| D-10 | 2026-09-27 | Our own native alarm module `alarm-engine`, as a local Expo module: **Kotlin (Android) now**, Swift/AlarmKit when iOS starts. All ring-time logic is native; the rest of the app is React Native/TypeScript | No mature alarm library exists (see `sections/alarm-research.md`) | Accepted |
| D-11 | 2026-09-27 | The first build step is an alarm proof of concept, built as a **tracer bullet**: the `alarm-engine` code is **kept** and structured for production (in `modules/alarm-engine/`); the test screen is throwaway. The full app is built only after it passes. Scope and pass criteria: `sections/alarm-poc.md` | Turns "feasible on paper" into "proven on device" before the big investment, without wasting the engine work | Accepted |
| D-13 | 2026-09-27 | Alarm testing = Android Emulator (Android 14–17, adb-driven scenario scripts with system-reported checks, PASS/FAIL table) + a physical Android phone with an aggressive OEM battery manager, for OEM behavior + unit tests. Every adb command is verified against official docs before use. Test plan: `sections/alarm-poc.md` (P-4) | The physical phone runs an older Android version and can't test Android 12+ rules; the emulator can't test OEM battery managers; together they cover both | Accepted |
