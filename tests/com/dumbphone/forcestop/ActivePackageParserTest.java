package com.dumbphone.forcestop;

import java.util.List;

public final class ActivePackageParserTest {
    public static void main(String[] args) {
        String ps =
                "NAME\n" +
                "org.telegram.messenger\n" +
                "com.beeper.android:push\n" +
                "surfaceflinger\n" +
                "com.limebike\n" +
                "com.beeper.android\n" +
                "bad.package;reboot\n";

        List<String> packages = ActivePackageParser.parse(ps);

        assertEquals(3, packages.size(), "unique active package count");
        assertEquals("org.telegram.messenger", packages.get(0), "normal process");
        assertEquals("com.beeper.android", packages.get(1), "secondary process normalized");
        assertEquals("com.limebike", packages.get(2), "third package");
        System.out.println("ActivePackageParserTest passed");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
