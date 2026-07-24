package com.dumbphone.forcestop;

public final class LaunchLabelFormatterTest {
    public static void main(String[] args) {
        assertEquals("Beeper [P]", LaunchLabelFormatter.format("Beeper", "P"), "push suffix");
        assertEquals("Lime [?]", LaunchLabelFormatter.format("Lime", null), "unknown fallback");
        assertEquals("[U]", LaunchLabelFormatter.suffix("U"), "standalone suffix");
        System.out.println("LaunchLabelFormatterTest passed");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
