# medicine-scheduler

A mobile medicine scheduler with **real alarms**. Its core purpose is making **dose changes over time**
(for example 1 pill for a week, then 2) easy to set up, change, and get alerted about. It is offline-first:
all personal data stays on the phone. Languages: EN / TR / DE.

## Current Phase and Next Steps
*(Keep this section up to date. It is the project's work order.)*
1. **Discuss details** (scheduling, notifications, other areas): started; continues alongside the
   later steps. Open items are listed in each section doc.
2. **Alarm implementation research**: done → `project-docs/sections/alarm-research.md`.
3. **Strict rules**: R-1 to R-7 defined; the user adds more over time (coding rules in the coding phase).
4. **Alarm proof of concept** (D-11): **next**. A tracer bullet: the `alarm-engine` code is kept, the
   test screen is throwaway. Tested per D-13. The full app starts only after it passes.
5. **Architecture** (`project-docs/03-architecture.md`), then the MVP, step by step.

## Key Decisions (full list: `project-docs/05-decisions-log.md`)
- **Android first; iOS postponed, not dropped** (D-1). Stay iOS-ready. If iOS is built, its minimum is
  iOS 26 (D-9).
- React Native + **Expo SDK 57** (development builds) + **TypeScript** (D-2).
- Our own native alarm module **`alarm-engine`** (Kotlin now, Swift later). All ring-time logic is native;
  JS/TS only hands it the upcoming doses (D-10).
- Proof of concept first, tested on the Android Emulator (14–17) and a physical OEM Android phone
  (D-11, D-13).

## Strict Rules (summary; full text in `project-docs/04-rules-and-conventions.md`)
- **R-1:** This file stays compact and points to details.
- **R-2:** Every project folder we create has its own `CLAUDE.md`. Not for generated, build, or
  third-party folders, or obvious tool/config folders like `.githooks/`.
- **R-3:** Work **step by step**, one small change at a time. Explain **what** and **how** before, and
  **why** after. Wait for the user's OK. No large implementations unless the user explicitly asks.
  - User background: React/TS experienced; React Native rusty; **Kotlin/Android new**, so explain
    those concepts; knows C.
- **R-4 / R-5:** Every task (code or docs) gets a TODO checklist in `project-docs/tasks/`, worked through
  item by item. **Every reply during a task ends with the TODO list (✅ done / ⬜ remaining / ➡️ next).**
  When it's done, add a summary to `project-docs/CHANGELOG.md` and delete the TODO file, **before** committing.
- **R-6:** More coding rules will come from the user in the coding phase.
- **R-7:** Docs show the **current state only**: rewrite in place, no "revised by" stacking. History
  goes **only** in `CHANGELOG.md`. Record only plan- or structure-changing decisions. Keep all docs
  consistent.
- Also: the global rules in `~/.claude/CLAUDE.md` apply (no commits to main/master, no secrets in chat,
  OWASP ASVS for security-sensitive work).

## Navigation: Where to Find Things
| When you need… | Read |
|---|---|
| Docs overview and editing rules | `project-docs/CLAUDE.md` |
| What the app is and its scope | `project-docs/01-project-description.md` |
| Requirements (FR / NFR) | `project-docs/02-requirements.md` |
| Architecture | `project-docs/03-architecture.md` *(not written yet)* |
| Full rules | `project-docs/04-rules-and-conventions.md` |
| Decisions (D-numbers) | `project-docs/05-decisions-log.md` |
| History of completed work | `project-docs/CHANGELOG.md` |
| Scheduling model and decisions (S-numbers) | `project-docs/sections/scheduling.md` |
| Alarm feasibility and platform facts (verified) | `project-docs/sections/alarm-research.md` |
| Alarm proof of concept: scope, pass criteria, test plan | `project-docs/sections/alarm-poc.md` |
| The task in progress | `project-docs/tasks/` (one TODO file per task) |
| Terms and concepts for the user's learning | `glossary.md` (git-ignored, personal; list candidate terms at the end of a response, add only the ones the user picks) |

Folder-level CLAUDE.md files: `project-docs/CLAUDE.md`, `project-docs/sections/CLAUDE.md`,
`project-docs/tasks/CLAUDE.md`. *(Add new folders here as they are created.)*

## Repo Setup (once per clone)
- `git config core.hooksPath .githooks` activates the pre-commit hook that blocks commits on
  `main`/`master`.

## Session Start Routine
1. Read "Current Phase and Next Steps" above.
2. Check `project-docs/tasks/` for an unfinished TODO file and continue it.
3. Read the latest entry in `project-docs/CHANGELOG.md`.
4. Before working in a folder, read that folder's `CLAUDE.md`.
