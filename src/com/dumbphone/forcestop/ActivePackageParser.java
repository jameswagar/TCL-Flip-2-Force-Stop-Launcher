package com.dumbphone.forcestop;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class ActivePackageParser {
    private static final Pattern PACKAGE_NAME = Pattern.compile(
            "[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)+");

    private ActivePackageParser() {
    }

    public static List<String> parse(String output) {
        Set<String> packages = new LinkedHashSet<>();
        if (output == null) {
            return new ArrayList<>();
        }
        for (String line : output.split("\\r?\\n")) {
            String processName = line.trim();
            int secondaryProcess = processName.indexOf(':');
            if (secondaryProcess >= 0) {
                processName = processName.substring(0, secondaryProcess);
            }
            if (PACKAGE_NAME.matcher(processName).matches()) {
                packages.add(processName);
            }
        }
        return new ArrayList<>(packages);
    }
}
