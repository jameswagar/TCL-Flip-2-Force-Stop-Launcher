package com.dumbphone.forcestop;

public final class LaunchReasonResolver {
    private LaunchReasonResolver() {
    }

    public static String resolve(String currentReason, String savedReason) {
        if (currentReason != null && currentReason.length() == 1) {
            return currentReason;
        }
        if (savedReason != null && savedReason.length() == 1) {
            return savedReason;
        }
        return "?";
    }
}
