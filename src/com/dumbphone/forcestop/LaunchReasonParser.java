package com.dumbphone.forcestop;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LaunchReasonParser {
    private static final Pattern PROCESS_START = Pattern.compile(
            "Start proc\\s+\\d+:([A-Za-z_][A-Za-z0-9_.]*)(?::[^/\\s]+)?/[^\\s]+\\s+for\\s+(.+)$");
    private static final Pattern ACTIVITY_COMPONENT = Pattern.compile(
            "\\bcmp=([A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+)/");

    private LaunchReasonParser() {
    }

    public static Map<String, String> parse(String logs) {
        Map<String, String> reasons = new LinkedHashMap<>();
        if (logs == null) {
            return reasons;
        }
        for (String line : logs.split("\\r?\\n")) {
            Matcher activity = ACTIVITY_COMPONENT.matcher(line);
            if (line.contains("START u") && activity.find()) {
                String reason = line.contains("act=android.intent.action.MAIN")
                        && line.contains("android.intent.category.LAUNCHER") ? "U" : "I";
                reasons.put(activity.group(1), reason);
                continue;
            }

            Matcher process = PROCESS_START.matcher(line);
            if (!process.find()) {
                continue;
            }
            String packageName = process.group(1);
            int secondaryProcess = packageName.indexOf(':');
            if (secondaryProcess >= 0) {
                packageName = packageName.substring(0, secondaryProcess);
            }
            reasons.put(packageName, classify(process.group(2)));
        }
        return reasons;
    }

    private static String classify(String startDescription) {
        String value = startDescription.toLowerCase();
        if (value.contains("firebase") || value.contains("messagingevent")
                || value.contains("cloudmessaging") || value.contains("gcm")) {
            return "P";
        }
        if (value.contains("alarm")) {
            return "A";
        }
        if (value.contains("job") || value.contains("workmanager")
                || value.contains("systemjobservice")) {
            return "J";
        }
        if (value.startsWith("broadcast")) {
            return "B";
        }
        if (value.startsWith("content provider") || value.startsWith("provider")) {
            return "C";
        }
        if (value.startsWith("service")) {
            return "S";
        }
        if (value.startsWith("activity")) {
            return "I";
        }
        if (value.startsWith("restart") || value.contains("sticky")) {
            return "R";
        }
        return "?";
    }
}
