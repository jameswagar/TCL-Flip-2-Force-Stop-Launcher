package com.dumbphone.forcestop;

import java.util.List;

public final class RecentTaskParserTest {
    public static void main(String[] args) {
        String dump =
                "  * Recent #0: Task{23f269f #350 visible=true type=standard mode=fullscreen A=10037:com.android.systemui U=0 sz=1}\n" +
                "  * Recent #1: Task{8a187e0 #301 visible=false type=home mode=fullscreen I=com.offlineinc.dumbdownlauncher/.MainActivity U=0 sz=2}\n" +
                "  * Recent #2: Task{49d83a1 #346 visible=false type=standard mode=fullscreen A=10145:com.ble.chargie U=0 sz=0}\n" +
                "  * Recent #3: Task{a37d19f #306 visible=false type=standard mode=fullscreen A=1001:com.android.phone U=0 sz=0}\n" +
                "  * Recent #4: Task{abc123 #351 visible=false type=standard mode=fullscreen A=10123:org.telegram.messenger U=0 sz=1}\n" +
                "  Visible recent tasks (most recent first):\n";

        List<RecentTaskInfo> tasks = RecentTaskParser.parse(dump);

        assertEquals(2, tasks.size(), "safe standard-task count");
        assertTask(tasks.get(0), 346, "com.ble.chargie");
        assertTask(tasks.get(1), 351, "org.telegram.messenger");
        System.out.println("RecentTaskParserTest passed");
    }

    private static void assertTask(RecentTaskInfo actual, int taskId, String packageName) {
        assertEquals(taskId, actual.taskId, "task id");
        assertEquals(packageName, actual.packageName, "package name");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected=" + expected + " actual=" + actual);
        }
    }
}
