package com.dumbphone.forcestop;

public final class LaunchLabelFormatter {
    private LaunchLabelFormatter() {
    }

    public static String format(String label, String reason) {
        return label + " " + suffix(reason);
    }

    public static String suffix(String reason) {
        String value = reason == null || reason.length() != 1 ? "?" : reason;
        return "[" + value + "]";
    }
}
