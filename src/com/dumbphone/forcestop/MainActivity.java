package com.dumbphone.forcestop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final String LAUNCH_REASON_PREFS = "launch_reasons";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<AppTask> tasks = new ArrayList<>();
    private ListView listView;
    private TaskAdapter adapter;
    private TextView emptyView;
    private LinearLayout fallbackMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();
        setContentView(createContentView());
        configureSystemMenuBar();
        loadActiveApps();
    }

    private void configureWindow() {
        getWindow().requestFeature(14);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private View createContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.TRANSPARENT);

        TextView title = textView("Force stop", 20, Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setBackgroundColor(Color.TRANSPARENT);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setDividerHeight(dp(1));
        adapter = new TaskAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSelected(position);
            }
        });

        emptyView = textView("Loading…", 17, Gravity.CENTER);
        emptyView.setTextColor(Color.WHITE);
        content.addView(emptyView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        content.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        listView.setVisibility(View.GONE);

        fallbackMenu = new LinearLayout(this);
        fallbackMenu.setOrientation(LinearLayout.HORIZONTAL);
        fallbackMenu.setBackgroundColor(Color.TRANSPARENT);
        fallbackMenu.addView(menuLabel("Stop", Gravity.START), weighted());
        fallbackMenu.addView(menuLabel("Open", Gravity.CENTER), weighted());
        fallbackMenu.addView(menuLabel("Stop All", Gravity.END), weighted());
        root.addView(fallbackMenu, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
        return root;
    }

    private LinearLayout.LayoutParams weighted() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
    }

    private TextView menuLabel(String value, int gravity) {
        TextView label = textView(value, 15, gravity | Gravity.CENTER_VERTICAL);
        label.setTextColor(Color.WHITE);
        label.setPadding(dp(7), 0, dp(7), 0);
        return label;
    }

    private void configureSystemMenuBar() {
        try {
            Object menuBar = Activity.class.getMethod("getMenuBar").invoke(this);
            if (menuBar == null) {
                return;
            }
            menuBar.getClass().getMethod(
                    "updateMenuBar",
                    String.class,
                    String.class,
                    String.class,
                    List.class)
                    .invoke(menuBar, "Stop", "Open", "Stop All", null);
            fallbackMenu.setVisibility(View.GONE);
        } catch (Exception ignored) {
            fallbackMenu.setVisibility(View.VISIBLE);
        }
    }

    private TextView textView(String value, int sizeSp, int gravity) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setGravity(gravity);
        return view;
    }

    private StateListDrawable rowBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable selected = new ColorDrawable(Color.WHITE);
        background.addState(new int[]{android.R.attr.state_selected}, selected);
        background.addState(new int[]{android.R.attr.state_activated}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);
        background.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        return background;
    }

    private ColorStateList rowTextColors() {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_activated},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{}
                },
                new int[]{Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE});
    }

    private StateListDrawable labelBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable selected = new ColorDrawable(Color.TRANSPARENT);
        background.addState(new int[]{android.R.attr.state_selected}, selected);
        background.addState(new int[]{android.R.attr.state_activated}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);

        GradientDrawable badge = new GradientDrawable();
        badge.setColor(Color.argb(190, 0, 0, 0));
        badge.setCornerRadius(dp(5));
        background.addState(new int[]{}, badge);
        return background;
    }

    private StateListDrawable reasonBackground() {
        StateListDrawable background = new StateListDrawable();
        ColorDrawable selected = new ColorDrawable(Color.TRANSPARENT);
        background.addState(new int[]{android.R.attr.state_selected}, selected);
        background.addState(new int[]{android.R.attr.state_activated}, selected);
        background.addState(new int[]{android.R.attr.state_pressed}, selected);

        GradientDrawable badge = new GradientDrawable();
        badge.setColor(Color.argb(210, 0, 0, 0));
        badge.setCornerRadius(dp(5));
        background.addState(new int[]{}, badge);
        return background;
    }

    private void loadActiveApps() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    RootShell.Result recentsResult = RootShell.run("dumpsys activity recents");
                    RootShell.Result processesResult = RootShell.run("ps -A -o NAME");
                    RootShell.Result startsResult = RootShell.run("logcat -d -v brief -t 5000");
                    if (!recentsResult.succeeded() || !processesResult.succeeded()) {
                        showLoadError("Root access is required");
                        return;
                    }
                    List<RecentTaskInfo> candidates = RecentTaskParser.parse(recentsResult.stdout);
                    List<String> activeList = ActivePackageParser.parse(processesResult.stdout);
                    Set<String> activePackages = new HashSet<>(activeList);
                    for (String packageName : activeList) {
                        candidates.add(new RecentTaskInfo(-1, packageName));
                    }
                    Map<String, String> launchReasons = LaunchReasonParser.parse(
                            startsResult.succeeded() ? startsResult.stdout : "");
                    final List<AppTask> loaded = loadAppDetails(
                            candidates, activePackages, launchReasons);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tasks.clear();
                            tasks.addAll(loaded);
                            adapter.notifyDataSetChanged();
                            updateEmptyState();
                        }
                    });
                } catch (Exception error) {
                    showLoadError("Unable to read active apps");
                }
            }
        });
    }

    private List<AppTask> loadAppDetails(
            List<RecentTaskInfo> recent,
            Set<String> activePackages,
            Map<String, String> launchReasons) {
        List<AppTask> loaded = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        SharedPreferences reasonHistory = getSharedPreferences(
                LAUNCH_REASON_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor reasonEditor = reasonHistory.edit();
        Set<String> seen = new HashSet<>();
        for (RecentTaskInfo task : recent) {
            if (!seen.add(task.packageName) || !ForceStopPolicy.isAllowed(task.packageName)) {
                continue;
            }
            try {
                ApplicationInfo application = packageManager.getApplicationInfo(task.packageName, 0);
                boolean hasLaunchIntent = packageManager.getLaunchIntentForPackage(task.packageName) != null;
                if (!ForceStopPolicy.isCandidate(
                        task.packageName, application.flags, hasLaunchIntent, true)) {
                    continue;
                }
                boolean running = activePackages.contains(task.packageName);
                String currentReason = launchReasons.get(task.packageName);
                String launchReason = running
                        ? LaunchReasonResolver.resolve(
                                currentReason,
                                reasonHistory.getString(task.packageName, null))
                        : "T";
                if (running && currentReason != null) {
                    reasonEditor.putString(task.packageName, currentReason);
                }
                loaded.add(new AppTask(
                        task.taskId,
                        task.packageName,
                        application.loadLabel(packageManager).toString(),
                        launchReason,
                        application.loadIcon(packageManager)));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        reasonEditor.apply();
        return loaded;
    }

    private void updateEmptyState() {
        boolean empty = tasks.isEmpty();
        emptyView.setText("No active apps");
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (!empty) {
            listView.setSelection(0);
            listView.requestFocus();
        }
    }

    private void showLoadError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tasks.clear();
                adapter.notifyDataSetChanged();
                listView.setVisibility(View.GONE);
                emptyView.setText(message);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private int selectedPosition() {
        int position = listView.getSelectedItemPosition();
        return position >= 0 ? position : 0;
    }

    private void stopSelected() {
        final int position = selectedPosition();
        if (position < 0 || position >= tasks.size()) {
            return;
        }
        final AppTask task = tasks.get(position);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final RootShell.Result result = RootShell.run(
                            ForceStopPolicy.commandFor(task.packageName));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result.succeeded()) {
                                clearLaunchReason(task.packageName);
                                tasks.remove(task);
                                adapter.notifyDataSetChanged();
                                updateEmptyState();
                                Toast.makeText(MainActivity.this,
                                        task.label + " stopped", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Could not stop " + task.label, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception error) {
                    showToast("Could not stop " + task.label);
                }
            }
        });
    }

    private void confirmStopAll() {
        if (tasks.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Stop All")
                .setMessage("Force stop all listed safe apps?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Stop All", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        stopAll();
                    }
                })
                .show();
    }

    private void stopAll() {
        final List<AppTask> toStop = new ArrayList<>(tasks);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                int stopped = 0;
                for (AppTask task : toStop) {
                    try {
                        RootShell.Result result = RootShell.run(
                                ForceStopPolicy.commandFor(task.packageName));
                        if (result.succeeded()) {
                            clearLaunchReason(task.packageName);
                            stopped++;
                        }
                    } catch (Exception ignored) {
                    }
                }
                final int stoppedCount = stopped;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stoppedCount == toStop.size()) {
                            tasks.clear();
                        } else {
                            loadActiveApps();
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyState();
                        Toast.makeText(MainActivity.this,
                                stoppedCount + " apps stopped", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void openSelected(int position) {
        if (position < 0 || position >= tasks.size()) {
            return;
        }
        AppTask task = tasks.get(position);
        Intent launch = getPackageManager().getLaunchIntentForPackage(task.packageName);
        if (launch == null) {
            showToast("Cannot open " + task.label);
            return;
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(launch);
        finish();
    }

    private void clearLaunchReason(String packageName) {
        getSharedPreferences(LAUNCH_REASON_PREFS, MODE_PRIVATE)
                .edit()
                .remove(packageName)
                .apply();
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SOFT_LEFT) {
            stopSelected();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_SOFT_RIGHT) {
            confirmStopAll();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            openSelected(selectedPosition());
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class TaskAdapter extends ArrayAdapter<AppTask> {
        TaskAdapter() {
            super(MainActivity.this, android.R.layout.simple_list_item_1, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout row = new LinearLayout(MainActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(5), dp(8), dp(5));
            row.setBackground(rowBackground());

            ImageView icon = new ImageView(MainActivity.this);
            icon.setImageDrawable(tasks.get(position).icon);
            row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));

            TextView label = textView(tasks.get(position).label, 17, Gravity.CENTER_VERTICAL);
            label.setDuplicateParentStateEnabled(true);
            label.setTextColor(rowTextColors());
            label.setSingleLine(true);
            label.setPadding(dp(10), 0, 0, 0);
            label.setBackground(labelBackground());
            row.addView(label, new LinearLayout.LayoutParams(0, dp(28), 1f));

            TextView reason = textView(
                    LaunchLabelFormatter.suffix(tasks.get(position).launchReason),
                    15,
                    Gravity.CENTER);
            reason.setDuplicateParentStateEnabled(true);
            reason.setTextColor(rowTextColors());
            reason.setBackground(reasonBackground());
            LinearLayout.LayoutParams reasonLayout = new LinearLayout.LayoutParams(dp(34), dp(28));
            reasonLayout.setMargins(dp(4), 0, 0, 0);
            row.addView(reason, reasonLayout);
            return row;
        }
    }

    private static final class AppTask {
        final int taskId;
        final String packageName;
        final String label;
        final String launchReason;
        final Drawable icon;

        AppTask(
                int taskId,
                String packageName,
                String label,
                String launchReason,
                Drawable icon) {
            this.taskId = taskId;
            this.packageName = packageName;
            this.label = label;
            this.launchReason = launchReason;
            this.icon = icon;
        }
    }
}
