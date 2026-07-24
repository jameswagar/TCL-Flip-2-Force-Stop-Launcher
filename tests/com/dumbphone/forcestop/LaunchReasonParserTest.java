package com.dumbphone.forcestop;

import java.util.Map;

public final class LaunchReasonParserTest {
    public static void main(String[] args) {
        String logs =
                "I/ActivityManager( 1119): Start proc 7024:com.beeper.android/u0a113 for broadcast {com.beeper.android/com.google.firebase.iid.FirebaseInstanceIdReceiver}\n" +
                "I/ActivityManager( 1119): Start proc 7148:org.telegram.messenger/u0a108 for broadcast {org.telegram.messenger/com.google.firebase.iid.FirebaseInstanceIdReceiver}\n" +
                "I/ActivityManager( 1119): Start proc 7200:com.limebike/u0a114 for broadcast {com.limebike/.ConnectivityReceiver}\n" +
                "I/ActivityManager( 1119): Start proc 7201:com.spotify.music/u0a148 for service {com.spotify.music/.PlaybackService}\n" +
                "I/ActivityManager( 1119): Start proc 7202:com.tcl.tct.weather/u0a60 for broadcast {com.tcl.tct.weather/.AlarmReceiver}\n" +
                "I/ActivityManager( 1119): Start proc 7203:org.fdroid.fdroid/u0a90 for service {org.fdroid.fdroid/androidx.work.impl.background.systemjob.SystemJobService}\n" +
                "I/ActivityManager( 1119): Start proc 7204:org.chromium.chrome/u0a80 for content provider {org.chromium.chrome/.ChromeBrowserProvider}\n" +
                "I/ActivityManager( 1119): Start proc 7205:com.ubercab.uberlite/u0a109 for activity {com.ubercab.uberlite/.RootActivity}\n" +
                "I/ActivityManager( 1119): Start proc 7206:com.ble.chargie/u0a145 for restart com.ble.chargie/.ChargeService\n" +
                "I/ActivityManager( 1119): Start proc 7207:proton.android.authenticator/u0a107 for top-activity\n" +
                "I/ActivityTaskManager( 1119): START u0 {act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=org.telegram.messenger/.DefaultIcon} from uid 10105\n";

        Map<String, String> reasons = LaunchReasonParser.parse(logs);

        assertEquals("P", reasons.get("com.beeper.android"), "push");
        assertEquals("U", reasons.get("org.telegram.messenger"), "later user launch wins");
        assertEquals("B", reasons.get("com.limebike"), "broadcast");
        assertEquals("S", reasons.get("com.spotify.music"), "service");
        assertEquals("A", reasons.get("com.tcl.tct.weather"), "alarm");
        assertEquals("J", reasons.get("org.fdroid.fdroid"), "job");
        assertEquals("C", reasons.get("org.chromium.chrome"), "content provider");
        assertEquals("I", reasons.get("com.ubercab.uberlite"), "inter-app activity");
        assertEquals("R", reasons.get("com.ble.chargie"), "restart");
        assertEquals("?", reasons.get("proton.android.authenticator"), "unknown");
        System.out.println("LaunchReasonParserTest passed");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
