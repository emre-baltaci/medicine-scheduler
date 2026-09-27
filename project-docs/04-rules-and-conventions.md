# Rules and Conventions — medicine-scheduler

> These rules are **strict** and must be followed without exception. The user adds or changes rules
> during the project. This file shows the **current** rules only; rule history is in `CHANGELOG.md`.

## General Working Rules (from 2026-09-27)

### R-1 Root CLAUDE.md
- The project root has a `CLAUDE.md` with the common project information.
- It must be **compact**, yet a good starting point for understanding **what** we are building and
  **how to find detailed information** when needed.
- It must clearly point to the project notes (`project-docs/`) and to the relevant folder-level
  `CLAUDE.md` files.
- The detailed rule text lives here (`04-rules-and-conventions.md`). CLAUDE.md summarizes and points
  to it, so nothing is duplicated.

### R-2 CLAUDE.md in each folder we create
- Every **project folder created by us** has its own `CLAUDE.md` with the information and rules that
  apply to that folder.
- It is created **together with the folder**.
- **Excluded:**
  - generated or third-party folders (for example `node_modules/`, generated native build folders,
    build outputs)
  - tool, config, and dependency folders whose purpose is obvious (for example `.githooks/`, `.vscode/`)

### R-3 Step-by-step, learning-focused work
- The user wants to **learn** from what we build.
- Changes and improvements are always made **step by step**. One step is one small, understandable
  change (for example one file, one function, or one concept).
- **No full implementations or large modifications at once**, unless the user explicitly says so.
- Before each step, explain **what** will be implemented and **how**. After it, explain **why** it works
  that way. Wait for the user's OK before the next step.
- **Glossary:** at the end of each response, list a **few** new terms used in that step (not basics the
  user already knows, such as git). The user picks which ones go into `glossary.md` (root, git-ignored,
  personal), 2–3 lines each. Nothing is added without the user's pick.
- **Explanation level, based on the user's background:**
  - React / TypeScript: experienced. No need to explain basics.
  - React Native: little experience, mostly forgotten. Explain RN-specific concepts (native modules,
    the bridge between JS and native, the Expo build flow, and so on).
  - Kotlin / Android: no experience. Explain the language and Android concepts (Activity,
    BroadcastReceiver, Service, Manifest, and so on) from the start.
  - Low-level concepts: familiar (C courses). Concepts like memory, processes, threads, and pointers
    can be referenced and compared to C.

### R-4 Change log
- `project-docs/CHANGELOG.md` records **project work** (not app release notes), filled as described in
  R-5.

### R-5 A TODO checklist for each task
- Applies to **implementation and documentation tasks**.
- After a task's plan is agreed, **create a TODO checklist file** in `project-docs/tasks/`, named
  `YYYY-MM-DD-<short-task-name>.md`.
- Work through the file item by item, ticking items off, until it is complete.
- **While a task is in progress, every reply ends with the TODO list** showing completed (✅) and
  remaining (⬜) items, with the next item marked (➡️), so the user can follow the progress.
- When it's complete, **move a summary into `CHANGELOG.md`**: date, what was done, decisions (by
  D-number), files affected, and other useful information. Then **delete the TODO file**.
- **Before any commit**, the change log entry is written and the completed TODO file is deleted, so a
  commit never contains a finished TODO file.
- Decisions are written once, in `05-decisions-log.md`; change log entries refer to them by D-number.
- *(Later: the user will add more process details, such as reviewer agents.)*

### R-6 More rules for the coding phase
The user will add more rules and restrictions when the coding phase starts.

### R-7 Documentation policy: current state only
- Docs (everything except `CHANGELOG.md`) always describe the **current state**. When something changes,
  **rewrite** the affected text in place. Do **not** stack "revised by" entries on top of old ones,
  because that blurs the context.
- **History lives only in `CHANGELOG.md`**, which always keeps it. Exception: personal data may be removed
  from it on the user's request, because the repo is public.
- **Don't record every user answer.** Record what changes our plans, structure, requirements, or
  decisions.
- **Consistency is important.** When a decision changes, update every document it affects in the same
  task, or list the remaining inconsistencies for the user.
- Numbers (D-, R-, S-, FR-, …) are never reused. A removed item leaves a gap in the numbering.
