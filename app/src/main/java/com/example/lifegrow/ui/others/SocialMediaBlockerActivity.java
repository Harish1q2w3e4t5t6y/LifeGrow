package com.example.lifegrow.ui.others;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifegrow.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SocialMediaBlockerActivity extends AppCompatActivity {

    private static final String PREF_NAME = "BlockedAppsPrefs";
    private static final String BLOCKED_DATA_KEY = "blocked_data";
    private SharedPreferences sharedPreferences;
    private Handler handler = new Handler();
    private Runnable usageChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media_blocker);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        ListView appListView = findViewById(R.id.appListView);
        Button clearPrefsButton = findViewById(R.id.clearPrefsButton);

        TextView blockedAppsHeader = findViewById(R.id.blockedAppsHeader);
        LinearLayout blockedAppsContainer = findViewById(R.id.blockedAppsContainer);

        clearPrefsButton.setOnClickListener(v -> {
            // Clear blocked data
            String json = sharedPreferences.getString(BLOCKED_DATA_KEY, "[]");

            try {
                JSONArray array = new JSONArray(json);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Remove each app's individual limit & usage keys
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String pkg = obj.getString("package").trim().toLowerCase();
                    editor.remove(pkg + "_limit");
                    editor.remove(pkg + "_usage");
                }

                // Remove the blocked app list
                editor.remove(BLOCKED_DATA_KEY);
                editor.apply();

                //Toast.makeText(this, "Blocked apps cleared!", Toast.LENGTH_SHORT).show();
                blockedAppsContainer.removeAllViews();
                blockedAppsHeader.setVisibility(View.GONE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Load blocked data
        loadBlockedAppsUI(blockedAppsContainer, blockedAppsHeader);

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> launchableApps = new ArrayList<>();

        for (ApplicationInfo appInfo : installedApps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 &&
                    pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                launchableApps.add(appInfo);
            }
        }

        ArrayAdapter<ApplicationInfo> adapter = new ArrayAdapter<ApplicationInfo>(
                this,
                android.R.layout.simple_list_item_1,
                launchableApps
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);

                ApplicationInfo appInfo = getItem(position);
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable appIcon = pm.getApplicationIcon(appInfo);

                int iconSize = (int) (64 * getResources().getDisplayMetrics().density);
                appIcon.setBounds(0, 0, iconSize, iconSize);
                textView.setCompoundDrawables(appIcon, null, null, null);
                textView.setCompoundDrawablePadding(16);

                textView.setText("  " + appName);
                textView.setTextSize(16);
                textView.setPadding(16, 16, 16, 16);

                return textView;
            }
        };

        appListView.setAdapter(adapter);
        checkOverlayPermission();

        appListView.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo app = launchableApps.get(position);
            String pkgName = app.packageName;

            if (!isAccessibilityServiceEnabled()) {
                //Toast.makeText(this, "Please enable Accessibility Service for LifeGrow", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }

            if (!hasUsageAccess()) {
                //Toast.makeText(this, "Please enable usage access permission", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return;
            }

            long realTimeUsage = sharedPreferences.getLong(pkgName.trim().toLowerCase() + "_usage", 0);
            long usageStatsUsage = getDailyUsageForApp(pkgName);

            long usageMillis;
            String source;

            if (realTimeUsage >= usageStatsUsage) {
                usageMillis = realTimeUsage;
                source = "Real-time";
            } else {
                usageMillis = usageStatsUsage;
                source = "Usage Stats";
            }

            long minutes = usageMillis / (1000 * 60);
            long seconds = (usageMillis / 1000) % 60;

            String appName = pm.getApplicationLabel(app).toString();
            Toast.makeText(this,
                    minutes + " min " + seconds + " sec\n" +"Today's usage for " + appName + ":\n" +
                            "(Source: " + source + ")",
                    Toast.LENGTH_LONG).show();


            promptForTimeLimit(pkgName, blockedAppsContainer, blockedAppsHeader);
        });

        startMonitoringAll();
    }

    private void loadBlockedAppsUI(LinearLayout container, TextView header) {
        String json = sharedPreferences.getString(BLOCKED_DATA_KEY, "[]");
        try {
            JSONArray blockedArray = new JSONArray(json);
            if (blockedArray.length() > 0) {
                header.setVisibility(View.VISIBLE);
                for (int i = 0; i < blockedArray.length(); i++) {
                    JSONObject obj = blockedArray.getJSONObject(i);
                    String pkg = obj.getString("package");
                    long limit = obj.getLong("limit");
                    long mins = limit / (60 * 1000);

                    TextView textView = new TextView(this);
                    textView.setText("• " + pkg + ": " + mins + " min/day");
                    textView.setTextSize(15);
                    textView.setPadding(4, 8, 4, 8);
                    container.addView(textView);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        String service = getPackageName() + "/" + AppBlockerAccessibilityService.class.getCanonicalName();
        String enabledServices = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServices != null && enabledServices.contains(service);
    }

    private boolean hasUsageAccess() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void promptForTimeLimit(String packageName, LinearLayout blockedAppsContainer, TextView blockedAppsHeader) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Daily Limit (in minutes)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("e.g., 30");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String timeStr = input.getText().toString().trim();
            if (!timeStr.isEmpty()) {
                long limitMillis = Long.parseLong(timeStr) * 60 * 1000;

                try {
                    JSONArray data = new JSONArray(sharedPreferences.getString(BLOCKED_DATA_KEY, "[]"));
                    boolean updated = false;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = data.getJSONObject(i);
                        if (obj.getString("package").equals(packageName)) {
                            obj.put("limit", limitMillis);
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        JSONObject newObj = new JSONObject();
                        newObj.put("package", packageName);
                        newObj.put("limit", limitMillis);
                        data.put(newObj);
                    }

                    // Save direct key-values for service compatibility
                    sharedPreferences.edit()
                            .putString(BLOCKED_DATA_KEY, data.toString())
                            .putLong(packageName.trim().toLowerCase() + "_limit", limitMillis)  // 👈 ADD THIS LINE
                            .apply();


                    //Toast.makeText(this, "Set daily limit for " + packageName, Toast.LENGTH_SHORT).show();

                    long mins = limitMillis / (60 * 1000);
                    for (int i = 0; i < blockedAppsContainer.getChildCount(); i++) {
                        TextView child = (TextView) blockedAppsContainer.getChildAt(i);
                        if (child.getText().toString().contains(packageName)) {
                            blockedAppsContainer.removeViewAt(i);
                            break;
                        }
                    }

                    TextView newEntry = new TextView(this);
                    newEntry.setText("• " + packageName + ": " + mins + " min/day");
                    newEntry.setTextSize(15);
                    newEntry.setPadding(4, 8, 4, 8);
                    blockedAppsContainer.addView(newEntry);

                    blockedAppsHeader.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                //Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    private void startMonitoringAll() {
        usageChecker = () -> {
            try {
                JSONArray data = new JSONArray(sharedPreferences.getString(BLOCKED_DATA_KEY, "[]"));
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    String pkg = obj.getString("package");
                    long limit = obj.getLong("limit");
                    long usage = getDailyUsageForApp(pkg);
                    if (usage > limit) {
                        //showBlockOverlay(pkg);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            handler.postDelayed(usageChecker, 60000);
        };
        handler.post(usageChecker);
    }

    private long getDailyUsageForApp(String packageName) {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000L * 60 * 60 * 24);

        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        for (UsageStats usage : stats) {
            if (usage.getPackageName().equals(packageName)) {
                return usage.getTotalTimeInForeground();
            }
        }
        return 0;
    }

    private void showBlockOverlay(String packageName) {
        if (!isFinishing()) {
            new AlertDialog.Builder(this)
                    .setTitle("Time Limit Reached")
                    .setMessage("You have reached your daily time limit for: " + packageName)
                    .setCancelable(false)
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(usageChecker);
        super.onDestroy();
    }
}
