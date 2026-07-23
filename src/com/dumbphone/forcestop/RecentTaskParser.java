package com.dumbphone.forcestop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RecentTaskParser {
    private static final Pattern TASK_LINE = Pattern.compile(
            "^\\s*\\* Recent #\\d+: Task\\{[^#]*#(\\d+).*?\\btype=(\\w+).*?(?:\\bA=\\d+:([A-Za-z0-9._]+)|\\bI=([A-Za-z0-9._]+)/).*\\}$");
    private static final Set<String> BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "android",
            "com.android.phone",
            "com.android.systemui",
            "com.dumbphone.forcestop",
            "com.offlineinc.dumbdownlauncher"));

    private RecentTaskParser() {
    }

    public static List<RecentTaskInfo> parse(String dump) {
        List<RecentTaskInfo> tasks = new ArrayList<>();
        for (String line : dump.split("\\r?\\n")) {
            if (line.trim().startsWith("Visible recent tasks")) {
                break;
            }
            Matcher matcher = TASK_LINE.matcher(line);
            if (!matcher.matches() || !"standard".equals(matcher.group(2))) {
                continue;
            }
            String packageName = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            if (!BLOCKED_PACKAGES.contains(packageName)) {
                tasks.add(new RecentTaskInfo(Integer.parseInt(matcher.group(1)), packageName));
            }
        }
        return tasks;
    }
}
