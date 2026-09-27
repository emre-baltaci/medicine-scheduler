# Section: Scheduling

> Status: **Draft**. Numbered items use the prefix **S-**.

## Model (proposed direction)
- A medicine plan is a **list of phases** in order. Each phase has a repeat rule, one or more dose
  times, and an amount for each time.
- Dose going up or down, cycles, and one-off changes are all expressed as phases.
- Easy templates in the UI (for example, "increase by X every Y days") create phases underneath.
- Edit scopes follow calendar apps: this dose only, this and following, a given period, the whole plan
  (FR-1.4).

## Decisions

### S-1 Phase length
- A phase can be **temporary** (fixed length) or **lifelong / open-ended** (for example, blood pressure
  medicine). It lasts until the user changes or stops it.
- Exactly how a temporary phase ends (number of days, end date, number of doses) is chosen when the
  medicine is created. *(Full option list to be finalized.)*

### S-2 Dose changes: planned or manual only, no conditions
- A dose change is either **planned when the schedule is created** (a phase that starts on a set date)
  or **made manually** by the user later.
- **No automatic or conditional logic** (for example, "increase if tolerated", or asking for confirmation
  before a step). The user sees no use for it.

### S-3 Timing methods: all three supported
Offered as options when creating a schedule:
- (a) **Fixed clock times** (08:00, 20:00)
- (b) **Interval from the first dose** ("every 8 hours")
- (c) **Relative to daily events** ("30 min before breakfast"); the user defines event times

### S-4 Missed doses and phase counting *(draft)*
- **Calendar-based**: phases and cycles follow the calendar. A missed dose does **not** shift the plan.
- Reason: doctors' instructions are usually date-based, alarms stay predictable, and counting taken
  doses needs "taken" tracking, which is a post-MVP feature.
- Possible later option: "count by doses taken" for each phase, once tracking exists.

### S-5 Grouping medicines due together
- Medicines due at the same time are **listed in one alarm**.

### S-6 Time zone changes
- When the phone's time zone changes, the app **asks the user** to either:
  - keep the same local clock times (08:00 stays 08:00 in the new zone), or
  - keep the real interval (hours shift to the new local time).

### S-7 Warnings at change points, and holding the current dose
- A schedule that is already set **warns the user at each change point** (when the dose is about to
  change), so the change never happens unnoticed.
- The user must be able to easily **keep the current dose for a certain period** instead of moving on
  (for example, the doctor changed their mind: "stay on 1 pill for one more week").
- **Warning timing** (D-8): the warning is shown **after the last dose of the current
  phase is taken**. It tells the user that the dose changes from the next dose on.
  - Note: "taken" tracking is post-MVP. Until then, "taken" means the user dismissed the last alarm of
    the phase or confirmed it in the post-dose notification (FR-2.4). *(To be confirmed in the
    notification design.)*

## Open / TBD
- **S-4 detail:** Claude must explain "calendar-based" in detail (exact rules and edge cases) in a later
  discussion.
- **S-7 extension:** when the current dose is extended, do later phases shift back by the same amount, or
  keep their original dates? *(Proposal: shift by default, since the doctor's plan usually continues
  from where it was paused.)*
- **S-7 warning form:** a notification or a note on the alarm screen. Decided with the notification design.
- Full list of ways a phase can end (S-1).
- Whether the time zone choice applies to all medicines or is asked per medicine (S-6).
- Pausing or resuming a medicine, and start dates in the future.
- Units and amounts (deferred, implementation detail).
