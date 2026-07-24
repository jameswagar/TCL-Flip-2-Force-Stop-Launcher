# TCL Flip 2 Force Stop Launcher

A small, keypad-friendly Android utility for the rooted TCL Flip 2 / Flip Plus (`Gflip6_USCC`, Android 11). It lists active or recent apps that pass a conservative safety policy and provides explicit root-backed force-stop controls.

## Interface

- Title: **Force stop**
- The launcher wallpaper remains visible behind unselected rows, matching the TCL **Recent apps** screen.
- The selected row reverses to a white background with black app and suffix text; unselected app names and suffixes use separate translucent-black badges with white text.
- The phone's native bottom menu bar shows **Stop**, **Open**, and **Stop All** at the physical bottom edge.
- Left soft key: **Stop** — immediately force-stops the selected app
- Center key: **Open** — launches the selected app
- Right soft key: **Stop All** — asks for confirmation, then force-stops every listed safe app

The list is the union of Android's recent tasks and currently running app processes. Only launchable apps that pass the safety policy are shown.

A package that Android confirms as force-stopped is hidden, even if its old task remains in Android's recent-task history. If Android starts the package again, it reappears with a bracketed launch-reason suffix. The suffix occupies a fixed-width column so it remains visible with long app names. Unselected suffixes use a compact translucent-black badge with white text; the badge disappears and the suffix reverses to black on the selected white row.

Force stopping is stronger than removing a task from Android's Recents list. It kills the package and marks it stopped. Most ordinary background work remains suppressed, but privileged or explicitly targeted delivery can restart an app. On this phone, microG/Firebase push delivery can restart Beeper and Telegram after a force-stop; those real restarts are marked **[P]** rather than mistaken for stale task history.

## Launch-reason suffixes

The suffix describes the most recently observed reason the package became active. Android 11 does not expose a single durable public launch-reason record, so Force Stop derives these categories from rooted Activity Manager logs and preserves the latest known category when old log lines roll out.

| Suffix | Category | Meaning |
| --- | --- | --- |
| **[U]** | User | Strong evidence of a user-visible launch: launcher icon, Force Stop's **Open**, or another `MAIN`/`LAUNCHER` activity start. |
| **[P]** | Push | Started by a recognized Firebase, FCM, microG, GCM, or cloud-messaging receiver. |
| **[A]** | Alarm | Started by an AlarmManager receiver or alarm-backed `PendingIntent`. |
| **[J]** | Job | Started for JobScheduler, WorkManager, or another scheduled job. |
| **[B]** | Broadcast | Started by a non-push broadcast, such as connectivity, package, charging, boot, or unlock. |
| **[S]** | Service | Started for a background, foreground, sticky, or bound service. |
| **[C]** | Content | Started because another process accessed a content provider. |
| **[I]** | Inter-app | An activity was started by another app, but user intent cannot be established. |
| **[R]** | Restart | Android or vendor software restarted a process or service. |
| **[T]** | Task only | Present only in Android's recent-task history; no live process was observed. |
| **[?]** | Unknown | Running, but the relevant start record was absent, expired, or ambiguous. |

**[U]** is strong evidence, not proof of a physical key press: accessibility automation, root, or privileged software can imitate a normal activity launch. **[?]** means insufficient evidence, not unsafe or malicious.

## Root and safety model

The app calls Android's standard command through Magisk:

```text
am force-stop --user 0 <package>
```

Magisk's superuser prompt is deliberately retained. Root access remains visible and revocable in Magisk's Superuser screen.

The app limits **Stop All** to:

- active or recent packages,
- packages with a user-launchable activity,
- user-installed apps or a small allowlist of vetted, nonessential system utilities,
- packages that are not already marked stopped by Android.

The following phone, launcher, input, root, LSPosed, push, and helper infrastructure is explicitly protected:

```text
android
com.android.dialer
com.android.mms
com.android.phone
com.android.providers.contacts
com.android.settings
com.android.systemui
com.android.vending
com.dumbphone.forcestop
com.dumbphone.mousetrap
com.dumbphone.nsfilter
com.dumbphone.recentslauncher
com.google.android.gms
com.google.android.gsf
com.offlineinc.dumbdownlauncher
com.offlineinc.dumbtt9
com.offlineinc.voicetotext
com.topjohnwu.magisk
inc.whew.android.fakegapps
org.lsposed.manager
```

Package names are validated before being passed to the root shell. The APK installs to internal storage only.

**Operational note:** messaging, navigation, charging-control, and other ordinary apps are safe for the phone to force-stop, but their background behavior and notifications cease until you open them again.

## Install

Download `ForceStop.apk` from the repository's [latest release](../../releases/latest), then install it with ADB:

```bash
adb install -r ForceStop.apk
```

Open **Force Stop** from the phone's app list. On first use, Magisk will request superuser access.

## Build and test

Requirements:

- JDK
- Android SDK Platform 35
- Android SDK Build Tools 35.0.0

Run the JVM parser and safety-policy tests:

```bash
./test.sh
```

Build and sign:

```bash
export ANDROID_SDK_ROOT="$HOME/Library/Android/sdk"
export FORCE_STOP_STOREPASS='choose-a-keystore-password'
./build.sh
```

The first build creates `force-stop-launcher.jks`. Preserve that keystore and password privately if future APKs must update an existing installation. The keystore, password, build output, and signed APK are intentionally excluded from Git.

Optional overrides:

```bash
export BUILD_TOOLS_VERSION=35.0.0
export ANDROID_PLATFORM_VERSION=35
export FORCE_STOP_KEYSTORE=/secure/path/force-stop-launcher.jks
export FORCE_STOP_KEY_ALIAS=force-stop
export FORCE_STOP_KEYPASS='key-password-if-different'
```

## Uninstall

```bash
adb uninstall com.dumbphone.forcestop
```

You can also revoke root access at any time from Magisk's Superuser screen.

## Compatibility

This utility is intentionally device-specific and requires a rooted Android device with a working `su` command and compatible Android `dumpsys activity recents` and `ps -A -o NAME` output.

## License

MIT
