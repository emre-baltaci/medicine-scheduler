# Section: Alarm Implementation Research

> Status: **Research complete** (2026-09-27).
> **Scope:** Android is built now. The iOS parts are kept as **reference for later** (iOS is postponed, D-1;
> minimum iOS 26 when built, D-9).
>
> Labels:
> - **[V✓]** checked again by the main session directly against the official source
> - **[V]** verified by a research agent with an official source and verbatim quote
> - **[V-AOSP]** true in Android open-source code, but not a documented API guarantee
> - **[U]** unverified or inferred; must be tested on real devices

---

## 0. Feasibility verdict (plain answer)

**YES, it is feasible.** In normal use the alarm **rings**, including when the phone is locked, the app is
closed, the phone is on silent or vibrate, or it restarted. This holds on Android and on iOS 26+.

The alarm fails **only** in the cases below. Each one is caused by the user's own action or setting, not
at random. The app can detect most of them and warn the user.

| Case | Rings? | Detectable by the app? |
|---|---|---|
| Android: user denies or revokes a required permission | ❌ | ✅ checked on every app open |
| Android: user force-stops the app (Android 15+) | ❌ until the app is opened again | ⚠️ only afterwards |
| Android: aggressive OEM battery settings (some Xiaomi/Huawei/…) | ⚠️ may be blocked | ⚠️ partly; user guidance needed |
| Android: user blocks alarms in Do Not Disturb, or sets alarm volume to 0 | ❌ (the user's choice) | ✅ volume can be checked |
| iOS 26+: user denies alarm permission | ❌ | ✅ |
| iOS 26+: user hides the app or locks it with Face ID | ❌ **silently** | ❌ not reliably, so warn in onboarding |

iOS below 26 cannot ring in silent mode, which is why the iOS minimum is 26 (D-9).

**The final confirmation is a real-device test.** The documents confirm feasibility. A small proof-of-concept
alarm on the user's own phones must confirm it in practice before the full app is built.

---

## 1. Platform baseline

| Fact | Source | Label |
|---|---|---|
| Expo SDK **57** is stable (`expo@57.0.25`, 2026-09-24). SDK 58 is in preview. | registry.npmjs.org/expo | [V✓] |
| SDK 57: Android 7+, iOS 16.4+, compile/target SDK 36, Xcode ≥ 26.4, RN 0.86 | github.com/expo/expo `packages/@expo/sdk-compatibility/src/sdk-compatibility.json` | [V✓] |
| New Architecture is mandatory from SDK 55 on. Every native library must support it. | docs.expo.dev/guides/new-architecture | [V] |
| Custom native code needs a **development build**. Expo Go can't run it. | docs.expo.dev/develop/development-builds/introduction | [V] |
| Local Expo modules: `npx create-expo-module@latest --local` (Swift/Kotlin) | docs.expo.dev/modules/get-started | [V] |

---

## 2. Android

### 2.1 Scheduling API
- **`AlarmManager.setAlarmClock`** is the right API. "the system never adjusts their delivery time. The
  system identifies these alarms as the most critical ones and leaves low-power modes if necessary to
  deliver the alarms." (developer.android.com/develop/background-work/services/alarms/schedule) [V✓]
- It can start a foreground service from the background, and it is exempt from standby buckets and the
  user's "Restricted" battery setting. [V] / [V-AOSP]
- `setExactAndAllowWhileIdle` is throttled in Doze and can be reordered, so it is only a fallback. [V]
- There is a limit of **500 concurrent alarms per app** (exceeding it throws an error), so we schedule a
  **rolling window**. [V-AOSP]

### 2.2 Permissions and Google Play policy
- `SCHEDULE_EXACT_ALARM` is **denied by default on Android 14+** for new installs (targeting 33+), except
  for calendar and alarm-clock apps. (…/about/versions/14/changes/schedule-exact-alarms) [V✓]
- If the permission is revoked, "your app stops, and all future exact alarms are canceled". The app gets
  no broadcast when it's revoked. It does get `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`
  when the permission is granted. [V✓]
- `USE_EXACT_ALARM` is granted at install and the user can't revoke it, but Play only allows: "The app is
  an alarm or timer app. The app is a calendar app that shows event notifications."
  (support.google.com/googleplay/android-developer/answer/16558241) [V✓]
- **Medicine reminders are not named anywhere. Whether Play accepts one: [U], a review risk.**
- Full-screen intent (the alarm screen over the lock screen): for apps targeting Android 14+, it is
  "limited to those that provide calling and alarms only". The user can turn it off.
  Check `canUseFullScreenIntent()`; settings page `ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT`. [V✓]
  - Without it, the alarm shows as a heads-up notification.
  - While the phone is in use, it shows as a heads-up instead of full screen. [V]
- `POST_NOTIFICATIONS` (Android 13+) is off by default. Without it there is **no visible alarm UI**, so
  it is a blocking onboarding step. [V]

### 2.3 Ringing
- Receiver → foreground service. The service does the following:
  - posts a notification with `CATEGORY_ALARM`, a full-screen intent, and an activity with
    `showWhenLocked` and `turnScreenOn`
  - loops sound using `AudioAttributes.USAGE_ALARM`
- Foreground service type: `systemExempted` (allowed for apps with an exact-alarm permission) or
  `mediaPlayback`. **Not `shortService`.** [V]
- **Android 17 audio hardening:** background audio fails **silently** unless it runs from a visible
  activity or a non-short foreground service. When targeting API 37, the extra requirement is waived
  only if the app "has been granted the exact alarm permission, and it is making changes to audio streams
  that have the USAGE_ALARM attribute." (…/about/versions/17/changes/bg-audio) [V✓]
  Test with `adb shell cmd audio set-enable-hardening throw`.
- Whether alarm-stream audio plays through default Do Not Disturb: [U]. The user can block alarms in
  DND.

### 2.4 Persistence and re-arming
- Alarms are **cleared on reboot**. Re-arm on `BOOT_COMPLETED`, `LOCKED_BOOT_COMPLETED`,
  `MY_PACKAGE_REPLACED`, `TIME_SET`, `TIMEZONE_CHANGED`, and on API 37+ `TIMEZONE_OFFSET_CHANGED`. [V]
- To ring **before the first unlock after a reboot**, the schedule must be in device-protected storage
  and the receivers must be `directBootAware`. Default RN storage is credential-encrypted [U], so this
  needs native storage. [V]
- **Android 15+ force-stop cancels all pending intents.** Nothing rings until the user opens the app
  again. (…/about/versions/15/behavior-changes-all) [V✓]
- Never start a `mediaPlayback` foreground service from the boot receiver (Android 15). Only re-arm
  there. [V]
- OEM battery killers (Xiaomi, Samsung, Huawei…): the official docs only say the restrictions "are
  determined by the device manufacturer". Specific behavior: [U] (dontkillmyapp.com, unofficial). Needs
  onboarding guidance and a re-arm when the app starts cold.

---

## 3. iOS

### 3.1 AlarmKit (iOS/iPadOS 26+): the only real alarm API
- Available from iOS 26.0, iPadOS 26.0, Mac Catalyst 26.0. [V✓]
- "It overrides both a device's focus and silent mode, if necessary." [V✓]
- Needs the `NSAlarmKitUsageDescription` Info.plist key plus user authorization. If the user says no,
  "all attempts to schedule alarms fail". No special entitlement is needed. [V]
- Alarms **persist through reboot, force quit, and app crash** (Apple DTS AlarmKit FAQ,
  developer.apple.com/forums/thread/797158). [V✓]
- **Hidden or passcode-locked apps: "any scheduled alarms by such apps will silently fail."** [V✓]
  Users must be warned.
- There is no fixed alarm limit. The device may impose one depending on its state, and exceeding it
  gives `maximumLimitReached`. [V✓]
- Recurrence supports **only `never` and `weekly([weekdays])`**. [V✓]
  - Every-N-hours, every-other-day, tapering, and end dates must be expanded by the app into one-shot
    alarms, in a rolling window.
- Schedules:
  - `.relative` follows the device time zone ("08:00 local").
  - `.fixed(Date)` is absolute.
  - This maps directly onto our time zone choice (S-6). [V]
- Snooze: the secondary button with `.countdown` behavior. Countdown presentation **needs a widget
  extension with a Live Activity**, or the system "may unexpectedly dismiss alarms". [V]
- `stopIntent` (App Intent) runs when the alarm is dismissed, so the app can record the dose. No app
  code runs at the moment the alarm fires [U, highly likely].
- The alarm follows the system ringer volume; there's no custom volume. [V]
- Maximum length and looping of a custom sound: [U].
- iOS 27 adds `appEntityIdentifier` (Siri can snooze). [V]
- Community reports of regressions (alarms lost after a 26.1→26.2 update, time zone change bugs): [U].
  Test on real devices after each iOS update.

### 3.2 Below iOS 26: no real alarm (not supported by us, D-9; kept for reference)
- Local notifications: a **limit of 64 pending notifications per app**, "no way around it" (Apple staff,
  forums/thread/811171). [V✓]
- **Time Sensitive** breaks through Focus but **not** the Ring/Silent switch. Only **Critical** does
  (Apple HIG table). [V✓]
  - Time Sensitive is a capability that needs no approval. Apple uses it for Reminders' medication
    reminders. [V]
- Critical Alerts need an entitlement issued by Apple. The HIG says they "typically come from
  governmental and public agencies or apps that help people manage their health or home" [V✓], but
  Apple staff also said critical alerts are "not suitable for alarm apps". [V] **Don't design around
  getting it.**
- Notification sound must be under 30 s. [V]
- No app code runs when a notification fires. Background refresh isn't guaranteed. The window can only
  be topped up on app open, on an action tap, or opportunistically. [V]

### 3.3 App Store Review points (for a future release)
- 1.4.1: medical apps get extra scrutiny and must remind users to check with a doctor.
- 1.4.2: **no dose calculation**; only remind about doses the user entered.
- 5.1.1: privacy policy required. 5.1.3: health data is especially sensitive.
- 5.1.2: the app must still work if the user denies notifications or alarms.
- 2.5.4 / 2.5.9: no silent-audio keep-alive tricks; don't interfere with hardware switches. [V]

---

## 4. React Native / Expo libraries

| Package | What it does for alarms | Status (checked 2026-09-27) | Verdict |
|---|---|---|---|
| `expo-notifications` 57.0.21 | `setExactAndAllowWhileIdle` (falls back to inexact without permission). **No full-screen intent, no `setAlarmClock`**. `setAlarmClock` arrives in 58 (preview). No AlarmKit. | Maintained [V✓] | Fine for **secondary notifications** (before and after the dose); not the alarm engine |
| `@notifee/react-native` 9.1.8 | Had full-screen and AlarmManager support | **Archived** on GitHub, last release 2024-12-20 [V✓] | Don't use |
| `react-native-notify-kit` 10.7.2 | Notifee fork: `SET_ALARM_CLOCK`, full-screen, reboot re-arm | Active, released 2026-09-23 [V✓]; single maintainer; no AlarmKit, no looping ring service | Possible fallback option |
| `react-native-alarm-scheduler` 1.0.1 | AlarmKit + setAlarmClock + foreground service + full-screen | Released 2026-09-04 [V✓]; 1 star, first release 2026-05 | **Reference only**: too young |
| `react-native-wake-alarm` 1.2.0 | Same idea; no snooze yet | Released 2026-09-17 [V✓]; weeks old | **Reference only** |
| `expo-alarm-kit` 0.1.11 | iOS AlarmKit only; forces deployment target 26 | Released 2026-04-15 [V✓] | Possible **reference** when iOS starts (its iOS 26 target matches D-9) |
| Older Android alarm libraries (`react-native-alarm-notification` and others) | Old bridge | Abandoned | Not usable (New Architecture is mandatory) |

**Conclusion:** there is **no mature library for real alarms** on either platform. [V✓ versions and status;
suitability judgment is ours]

---

## 5. Design direction
> The module decision is D-10. Details are finalized in `03-architecture.md` and the proof of concept.

1. **Our own local Expo module, `alarm-engine`** (Kotlin now, Swift later) that handles *only* alarms. All ring-time
   logic stays native; JS never has to run when an alarm fires.
   - JS → native: "the next N doses" (a rolling window), synced whenever the schedule changes and when
     the app opens.
   - Native → JS: a log of what fired, was snoozed, or was dismissed while the app was closed. JS reads it
     on launch.
2. **Android:** `setAlarmClock` for every dose and snooze → receiver → foreground service
   (`systemExempted`) → full-screen alarm activity + looping `USAGE_ALARM` sound + Snooze/Taken
   actions.
   - Re-arm receivers for boot, update, time, and time zone events.
   - Schedule stored in device-protected storage.
3. **iOS 26+ (later, when iOS starts):** AlarmKit. `.relative` for "same local clock time" doses, `.fixed` for "same real interval"
   doses (S-6). Snooze through `.countdown` + a widget extension. Dose logging through `stopIntent`.
4. **Before and after notifications** (FR-2.3 / FR-2.4) use `expo-notifications`.
5. **Onboarding permission checks**, re-checked every time the app opens:
   - notifications
   - exact alarms
   - full-screen
   - battery / OEM guidance
   - later on iOS: AlarmKit authorization, and a warning not to hide or lock the app
6. **Rolling window:** Android has a 500-alarm limit (AlarmKit, later, has an unpublished one). The window is refilled on every app open, every alarm action, boot, and
   time change.

## 5a. iOS facts behind D-1 and D-9
- **Minimum iOS 26** (D-9).
  - **Devices that support iOS 26** (Apple Support, support.apple.com/guide/iphone/iphe3fa5df43/26/ios/26) [V✓]:
    - Oldest supported: **iPhone 11 / 11 Pro / 11 Pro Max** (2019) and **iPhone SE (2nd gen)** (2020).
    - Everything newer is supported: 12, 13, SE 3rd gen, 14, 15, 16, 16e, 17, Air, 17e.
    - **Excluded:** iPhone XS, XS Max, XR and older, which stop at iOS 18.

- **iOS postponed** (D-1).
  - Reason: the iOS build prerequisites aren't available yet: Apple Developer Program membership, a Mac
    that can run Xcode 26.4+ (which needs macOS Tahoe 26.2+,
    developer.apple.com/xcode/system-requirements [V✓]), and an iPhone on iOS 26+.
  - Routes later: a paid account + EAS Build (cloud, no Mac needed), or a newer Mac. Either way, an
    iPhone on iOS 26+ is needed for testing.

## 5b. Test devices
| Device | OS | Notes |
|---|---|---|
| Physical phone | An older Android version with an OEM skin that has aggressive battery management | A **good worst-case OEM test**. It **cannot** test Android 12+ behavior (exact-alarm permission, 13 notification permission, 14 full-screen/exact defaults, 15 force-stop, 17 audio hardening) |
| Android Emulator | Android 14, 15, 16, 17 | Covers the newer Android rules; can't test OEM battery managers |

## 6. Open risks
1. **Google Play:** a medicine app may not count as an "alarm app" for `USE_EXACT_ALARM` or full-screen
   auto-grant. Then the user grants both manually and can revoke them. *(Irrelevant for personal use;
   matters for publishing.)*
2. **Silent failures:** an Android force-stop, Android 17 audio in the wrong context, a revoked
   permission (later on iOS: a hidden or locked app). The app must detect what it can and warn the user.
3. **OEM battery killers** on some Android brands. Needs user guidance and a cold-start re-arm.
4. **We maintain native code** (Kotlin now, Swift later). Keep the module small and well tested.
5. *(iOS, later)* **AlarmKit is young:** reported regressions; test after every iOS update.
6. Items marked **[U]** must be tested on real devices before we rely on them.
