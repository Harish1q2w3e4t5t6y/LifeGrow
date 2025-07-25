package com.example.lifegrow.ui.others;
import android.accessibilityservice.AccessibilityService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

public class AppBlockerAccessibilityService extends AccessibilityService {

    private String lastPackageName = "";
    private long lastTimestamp = 0;
    private SharedPreferences sharedPreferences;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        sharedPreferences = getSharedPreferences("BlockedAppsPrefs", MODE_PRIVATE);
        //Toast.makeText(this, "🚀 AppBlocker Service Connected", Toast.LENGTH_SHORT).show();
        Log.d("AppBlocker", "Service connected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String currentPackage = String.valueOf(event.getPackageName());

            // Log the current package and display "Hi" Toast
            Log.d("AppBlocker", "Current package: " + currentPackage);
            //Toast.makeText(this, "Hi! Current app: " + currentPackage, Toast.LENGTH_SHORT).show();

            // Track usage if switching apps
            long now = System.currentTimeMillis();
            if (!lastPackageName.isEmpty() && !lastPackageName.equals(currentPackage)) {
                long timeSpent = now - lastTimestamp;
                SharedPreferences prefs = getSharedPreferences("BlockedAppsPrefs", MODE_PRIVATE);
                long prevUsage = prefs.getLong(lastPackageName + "_usage", 0);
                prefs.edit().putLong(lastPackageName + "_usage", prevUsage + timeSpent).apply();
            }

            lastPackageName = currentPackage;
            lastTimestamp = now;

            // Optionally, check if this app is being blocked
            checkAndBlock(currentPackage);
            // Example of scheduling a notification after blocking
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("AppBlocker", "Service interrupted");
    }

    private void checkAndBlock(String pkg) {
        String limitKey = pkg + "_limit";

        if (sharedPreferences.contains(limitKey)) {
            long limit = sharedPreferences.getLong(limitKey, -1);
            long usageMillis = getDailyUsageForApp(pkg);

            //Toast.makeText(this, "⚠️ " + getAppName(pkg) + " is being monitored", Toast.LENGTH_SHORT).show();
            Log.d("AppBlocker", "Usage for " + pkg + ": " + usageMillis + " / " + limit);

            if (usageMillis >= limit) {
                //Toast.makeText(this, "🚫 " + getAppName(pkg) + " usage limit reached!", Toast.LENGTH_LONG).show();
                Log.d("AppBlocker", "🚫 Blocking " + pkg);

                Intent overlayIntent = new Intent(this, BlockOverlayService.class);
                overlayIntent.putExtra("blocked_app", pkg);
                overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(overlayIntent);

                // Send the user to the home screen
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
            }
        } else {
            //Toast.makeText(this, "✅ " + getAppName(pkg) + " is not in SharedPreferences", Toast.LENGTH_SHORT).show();
            Log.d("AppBlocker", pkg + " is not monitored. (Searched key: " + limitKey + ")");
        }
    }

    private long getDailyUsageForApp(String packageName) {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (1000L * 60 * 60 * 24); // last 24 hours

        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        for (UsageStats usage : stats) {
            if (usage.getPackageName().equals(packageName)) {
                return usage.getTotalTimeInForeground();
            }
        }
        return 0;
    }

    // This method returns the application label, which is used only for display in Toasts and logs.
    private String getAppName(String pkg) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }
}
