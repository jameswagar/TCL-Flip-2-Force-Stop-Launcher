package com.dumbphone.forcestop;

public final class ForceStopPolicyTest {
    private static final int FLAG_SYSTEM = 1;
    private static final int FLAG_UPDATED_SYSTEM_APP = 1 << 7;
    private static final int FLAG_STOPPED = 1 << 21;

    public static void main(String[] args) {
        assertTrue(ForceStopPolicy.isAllowed("org.telegram.messenger"), "normal package allowed");
        assertFalse(ForceStopPolicy.isAllowed("com.android.systemui"), "SystemUI blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.android.phone"), "phone service blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.offlineinc.dumbtt9"), "default input method blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.google.android.gms"), "push infrastructure blocked");
        assertFalse(ForceStopPolicy.isAllowed("org.lsposed.manager"), "LSPosed manager blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.topjohnwu.magisk"), "Magisk manager blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.dumbphone.helper"), "helper namespace blocked");
        assertFalse(ForceStopPolicy.isAllowed("com.example.app; reboot"), "shell injection blocked");

        assertTrue(ForceStopPolicy.isCandidate(
                "org.telegram.messenger", 0, true, true), "active launchable user app shown");
        assertFalse(ForceStopPolicy.isCandidate(
                "org.telegram.messenger", FLAG_STOPPED, true, true), "stopped app hidden");
        assertFalse(ForceStopPolicy.isCandidate(
                "org.telegram.messenger", 0, true, false), "inactive app hidden");
        assertFalse(ForceStopPolicy.isCandidate(
                "org.telegram.messenger", 0, false, true), "non-launchable app hidden");
        assertFalse(ForceStopPolicy.isCandidate(
                "com.android.settings", FLAG_SYSTEM, true, true), "system app blocked by default");
        assertFalse(ForceStopPolicy.isCandidate(
                "com.android.settings", FLAG_UPDATED_SYSTEM_APP, true, true), "updated system app blocked by default");
        assertTrue(ForceStopPolicy.isCandidate(
                "com.android.soundrecorder", FLAG_SYSTEM, true, true), "vetted recorder system app shown");

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
