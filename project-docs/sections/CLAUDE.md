# project-docs/sections/ — Feature and Area Documents

One document per feature or area. It holds the details that are too specific for `02-requirements.md`
or `03-architecture.md`. General doc rules (R-7, numbering) are in `../CLAUDE.md`.

## Current Documents
| File | Prefix | Content |
|---|---|---|
| `scheduling.md` | `S-` | Scheduling model and decisions (phases, timing, time zones, warnings) |
| `alarm-research.md` | — | Verified alarm research (Android now, iOS for later) and the design direction |
| `alarm-poc.md` | `P-` | Alarm proof of concept: purpose, scope, pass criteria, test plan |

*(Add new docs here. Planned: `notifications.md`.)*

## Conventions
- **Naming:** `kebab-case.md`, named after the area.
- **Header:** the title (`# Section: <Name>`), then a status line: **Draft**, **Decided**, or **Research
  complete**.
- **Prefix:** if the doc has its own numbered items, define the prefix in the header and add it to the
  table above.
- **Open items:** keep an "Open / TBD" list at the end. When an item is resolved, move it into the
  decisions in place and remove it from the list (R-7).

## Research Docs
- Every factual claim needs an **official source** (URL) and a label:
  - **[V✓]** checked again directly by the main session
  - **[V]** verified by a research agent, with a verbatim quote
  - **[V-AOSP]** true in Android open-source code, not a documented guarantee
  - **[U]** unverified or inferred; must be tested before we rely on it
- Put a plain-language verdict at the top. The user wants clear answers, not only details.
