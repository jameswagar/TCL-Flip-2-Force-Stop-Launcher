# TCL Flip 2 Force Stop Launcher

A small, keypad-friendly Android utility for the rooted TCL Flip 2 / Flip Plus (`Gflip6_USCC`, Android 11). It lists safe recent third-party apps and provides explicit root-backed force-stop controls.

## Interface

The app mirrors the phone's simple recent-apps layout:

- Title: **Force Stop**
- Left soft key: **Stop** — immediately force-stops the selected app
- Center key: **Open** — launches the selected app
- Right soft key: **Stop All** — asks for confirmation, then force-stops all listed apps

Force stopping is stronger than removing a task from Android's Recents list. A force-stopped app's background services, jobs, alarms, sync, and notifications generally remain stopped until the app is opened again.

## Root and safety model

The app calls Android's standard command through Magisk:

```text
am force-stop --user 0 <package>
```

Magisk's superuser prompt is deliberately retained. Root access remains visible and revocable in Magisk's Superuser screen.

The following critical packages are excluded from both the list and **Stop All**:

```text
android
com.android.phone
com.android.systemui
com.dumbphone.forcestop
com.offlineinc.dumbdownlauncher
```

Package names are validated before being passed to the root shell. The APK installs to internal storage only.

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

This utility is intentionally device-specific and requires a rooted Android device with a working `su` command and the expected Android 11 `dumpsys activity recents` format.

## License

MIT
