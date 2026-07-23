package com.dumbphone.forcestop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<AppTask> tasks = new ArrayList<>();
    private ListView listView;
    private TaskAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());
        loadRecentTasks();
    }

    private View createContentView() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        TextView title = textView("Force Stop", 20, Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        listView = new ListView(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(android.R.color.darker_gray);
        adapter = new TaskAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSelected(position);
            }
        });

        emptyView = textView("Loading…", 17, Gravity.CENTER);
        emptyView.setTextColor(Color.LTGRAY);
        content.addView(emptyView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        content.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        listView.setVisibility(View.GONE);

        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.HORIZONTAL);
        menu.setBackgroundColor(Color.rgb(28, 28, 28));
        menu.addView(menuLabel("Stop", Gravity.START), weighted());
        menu.addView(menuLabel("Open", Gravity.CENTER), weighted());
        menu.addView(menuLabel("Stop All", Gravity.END), weighted());
        root.addView(menu, new LinearLayout.LayoutParams(
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

    private TextView textView(String value, int sizeSp, int gravity) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setGravity(gravity);
        return view;
    }

    private void loadRecentTasks() {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    RootShell.Result result = RootShell.run("dumpsys activity recents");
                    if (!result.succeeded()) {
                        showLoadError("Root access is required");
                        return;
                    }
                    List<RecentTaskInfo> recent = RecentTaskParser.parse(result.stdout);
                    final List<AppTask> loaded = loadAppDetails(recent);
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
                    showLoadError("Unable to read recent apps");
                }
            }
        });
    }

    private List<AppTask> loadAppDetails(List<RecentTaskInfo> recent) {
        List<AppTask> loaded = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Set<String> seen = new HashSet<>();
        for (RecentTaskInfo task : recent) {
            if (!seen.add(task.packageName) || !ForceStopPolicy.isAllowed(task.packageName)) {
                continue;
            }
            try {
                ApplicationInfo application = packageManager.getApplicationInfo(task.packageName, 0);
                loaded.add(new AppTask(
                        task.taskId,
                        task.packageName,
                        application.loadLabel(packageManager).toString(),
                        application.loadIcon(packageManager)));
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return loaded;
    }

    private void updateEmptyState() {
        boolean empty = tasks.isEmpty();
        emptyView.setText("No recent apps");
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
                .setMessage("Force stop all recent apps?")
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
                            loadRecentTasks();
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

            ImageView icon = new ImageView(MainActivity.this);
            icon.setImageDrawable(tasks.get(position).icon);
            row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));

            TextView label = textView(tasks.get(position).label, 17, Gravity.CENTER_VERTICAL);
            label.setTextColor(Color.WHITE);
            label.setSingleLine(true);
            label.setPadding(dp(10), 0, 0, 0);
            row.addView(label, new LinearLayout.LayoutParams(0, dp(46), 1f));
            return row;
        }
    }

    private static final class AppTask {
        final int taskId;
        final String packageName;
        final String label;
        final Drawable icon;

        AppTask(int taskId, String packageName, String label, Drawable icon) {
            this.taskId = taskId;
            this.packageName = packageName;
            this.label = label;
            this.icon = icon;
        }
    }
}
