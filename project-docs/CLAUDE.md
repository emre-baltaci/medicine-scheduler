# project-docs/ — Project Knowledge Base

The single source of truth for the project's context, rules, requirements, and decisions.
The work order is in the root `CLAUDE.md` ("Current Phase and Next Steps").

## Files and When to Update Them
| File | Holds | Update when… |
|---|---|---|
| `01-project-description.md` | What the app is, scope, context | scope or goals change |
| `02-requirements.md` | FR-x (functional), NFR-x (non-functional) | a requirement is added, changed, or removed |
| `03-architecture.md` | Tech stack, components, data model *(not written yet)* | the architecture changes |
| `04-rules-and-conventions.md` | R-x strict rules (full text) | the user adds or changes a rule |
| `05-decisions-log.md` | D-x decisions affecting plans or structure | a decision is made or changed |
| `CHANGELOG.md` | History of completed tasks (newest first) | a task's TODO is completed (R-5) |
| `sections/` | One doc per feature or area | see `sections/CLAUDE.md` |
| `tasks/` | TODO checklist of the task in progress | see `tasks/CLAUDE.md` |

## Numbering
- `D-` decisions, `R-` rules, `FR-` / `NFR-` requirements.
- Section-specific prefixes are defined in the section doc (for example `S-` in `sections/scheduling.md`).
- Numbers are **never reused**. A merged or removed item leaves a gap.

## Editing Rules (R-7)
- Every doc except `CHANGELOG.md` shows the **current state**. Rewrite in place; don't stack
  "revised by" notes.
- Record only what changes plans, structure, requirements, or decisions. Not every answer.
- When something changes, update **every** affected doc in the same task (search for its number and
  keywords), or list the remaining inconsistencies for the user.
- History goes only into `CHANGELOG.md`.
- Create a file only when it has real content.
