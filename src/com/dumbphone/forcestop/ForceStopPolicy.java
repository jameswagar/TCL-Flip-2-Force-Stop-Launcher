package com.dumbphone.forcestop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class ForceStopPolicy {
    private static final Pattern PACKAGE_NAME = Pattern.compile(
            "[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+");

    private static final int FLAG_SYSTEM = 1;
    private static final int FLAG_UPDATED_SYSTEM_APP = 1 << 7;
    private static final int FLAG_STOPPED = 1 << 21;

    private static final Set<String> BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "android",
            "com.android.dialer",
            "com.android.mms",
            "com.android.phone",
            "com.android.providers.contacts",
            "com.android.settings",
            "com.android.systemui",
            "com.android.vending",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.offlineinc.dumbdownlauncher",
            "com.offlineinc.dumbtt9",
            "com.offlineinc.voicetotext",
            "com.topjohnwu.magisk",
            "inc.whew.android.fakegapps",
            "org.lsposed.manager"));

    private static final Set<String> BLOCKED_PACKAGE_PREFIXES = new HashSet<>(Arrays.asList(
            "com.dumbphone."));

    private static final Set<String> SAFE_SYSTEM_APPS = new HashSet<>(Arrays.asList(
            "com.android.calculator2",
            "com.android.gallery3d",
            "com.android.music",
            "com.android.note",
            "com.android.soundrecorder",
            "com.jrdcom.filemanager",
            "com.tcl.camera",
            "com.tcl.tct.weather",
            "org.chromium.chrome"));

    private ForceStopPolicy() {
    }

    public static boolean isAllowed(String packageName) {
        return packageName != null
                && PACKAGE_NAME.matcher(packageName).matches()
                && !BLOCKED_PACKAGES.contains(packageName)
                && !hasBlockedPrefix(packageName);
    }

    private static boolean hasBlockedPrefix(String packageName) {
        for (String prefix : BLOCKED_PACKAGE_PREFIXES) {
            if (packageName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCandidate(
            String packageName, int applicationFlags, boolean hasLaunchIntent, boolean active) {
        if (!isAllowed(packageName) || !hasLaunchIntent || !active) {
            return false;
        }
        if ((applicationFlags & FLAG_STOPPED) != 0) {
            return false;
        }
        boolean systemApp = (applicationFlags & (FLAG_SYSTEM | FLAG_UPDATED_SYSTEM_APP)) != 0;
        return !systemApp || SAFE_SYSTEM_APPS.contains(packageName);
    }

    public static String commandFor(String packageName) {
        if (!isAllowed(packageName)) {
            throw new IllegalArgumentException("Unsafe package: " + packageName);
        }
        return "am force-stop --user 0 " + packageName;
    }
}
