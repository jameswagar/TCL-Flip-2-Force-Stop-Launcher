package com.dumbphone.forcestop;

public final class LaunchReasonResolverTest {
    public static void main(String[] args) {
        assertEquals("P", LaunchReasonResolver.resolve("P", "U"), "current reason wins");
        assertEquals("P", LaunchReasonResolver.resolve(null, "P"), "saved reason survives log rollover");
        assertEquals("?", LaunchReasonResolver.resolve(null, null), "missing reason is unknown");
        System.out.println("LaunchReasonResolverTest passed");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
