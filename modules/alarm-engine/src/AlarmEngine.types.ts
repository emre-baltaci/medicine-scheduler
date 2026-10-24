/** Per-alarm settings and texts. Missing values use the engine's defaults. */
export type AlarmOptions = {
  snoozeMinutes?: number; // 1–120, default 5
  confirmationDelayMinutes?: number; // 0–120, 0 = off, default 5
  autoStopMinutes?: number; // 1–60, default 10
  title?: string;
  body?: string;
  lockedTitle?: string; // neutral text for the lock screen
  snoozeLabel?: string;
  okLabel?: string;
  lockScreenDetails?: 'hidden' | 'title' | 'full'; // while locked; default 'hidden'
};
