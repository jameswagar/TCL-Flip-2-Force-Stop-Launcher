package com.dumbphone.forcestop;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class ForceStopPolicy {
    private static final Pattern PACKAGE_NAME = Pattern.compile(
            "[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+");
    private static final Set<String> BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "android",
            "com.android.phone",
            "com.android.systemui",
            "com.dumbphone.forcestop",
            "com.offlineinc.dumbdownlauncher"));

    private ForceStopPolicy() {
    }

    public static boolean isAllowed(String packageName) {
        return packageName != null
                && PACKAGE_NAME.matcher(packageName).matches()
                && !BLOCKED_PACKAGES.contains(packageName);
    }

    public static String commandFor(String packageName) {
        if (!isAllowed(packageName)) {
            throw new IllegalArgumentException("Unsafe package: " + packageName);
        }
        return "am force-stop --user 0 " + packageName;
    }
}
