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
