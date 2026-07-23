package com.dumbphone.forcestop;

public final class RecentTaskInfo {
    public final int taskId;
    public final String packageName;

    public RecentTaskInfo(int taskId, String packageName) {
        this.taskId = taskId;
        this.packageName = packageName;
    }
}
