package com.dumbphone.forcestop;

public final class ForceStopPolicyTest {
    public static void main(String[] args) {
        assertTrue(ForceStopPolicy.isAllowed("org.telegram.messenger"), "normal package allowed");
        assertFalse(ForceStopPolicy.isAllowed("com.android.systemui"), "SystemUI blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.android.phone"), "phone service blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.example.app; reboot"), "shell injection blocked");
        assertEquals(
                "am force-stop --user 0 org.telegram.messenger",
                ForceStopPolicy.commandFor("org.telegram.messenger"),
                "force-stop command");

        boolean threw = false;
        try {
            ForceStopPolicy.commandFor("com.android.systemui");
        } catch (IllegalArgumentException expected) {
            threw = true;
        }
        assertTrue(threw, "blocked package command rejected");
        System.out.println("ForceStopPolicyTest passed");
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }

    private static void assertFalse(boolean value, String label) {
        assertTrue(!value, label);
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
