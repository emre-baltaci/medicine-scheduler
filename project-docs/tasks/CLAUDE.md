# project-docs/tasks/ — Task TODO Checklists (R-5)

Holds the TODO checklist of the task in progress, for implementation and documentation tasks alike.
Full rule text: `../04-rules-and-conventions.md` (R-3, R-5).

## Life Cycle
1. The task's plan is agreed with the user.
2. Create a TODO file here: `YYYY-MM-DD-<short-task-name>.md`.
3. Work through it **one item at a time** (R-3): explain what and how, do it, explain why, tick it, and
   wait for the user's OK.
4. **End every reply with the TODO list**: ✅ done, ⬜ remaining, ➡️ next.
5. If new work is discovered, **add it as a new item** (for example `2a`, `2b`) before doing it.
6. When every item is ticked, add a summary to `../CHANGELOG.md` (entry format is defined there).
7. **Delete the TODO file.** This happens **before** the task's commit, so commits never contain a
   completed TODO file.

## Template
```markdown
# TODO: <Task title>

> Task type: implementation | documentation. Created YYYY-MM-DD per R-5.

## Goal
<1–3 sentences>

## Checklist
- [ ] 1. <small, reviewable step>
- [ ] 2. …
- [ ] N. Move a summary to CHANGELOG.md and delete this TODO file (R-5)
```

## Session Rule
- An unfinished TODO file here means **work is in progress**. Continue it before starting anything new.
- **One task at a time**, unless the user decides otherwise.

*(Later: the user will add more process details, such as reviewer agents.)*
