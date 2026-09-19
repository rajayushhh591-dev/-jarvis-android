package com.jarvis.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;
import java.util.Locale;

public class AppLauncher {

    private final Context context;
    private final PackageManager packageManager;

    public AppLauncher(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    public boolean openApp(String appName) {

        if (appName == null || appName.trim().isEmpty()) {
            return false;
        }

        String wanted = appName.trim().toLowerCase(Locale.ROOT);

        Intent launcherIntent = new Intent(
                Intent.ACTION_MAIN,
                null
        );

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> apps =
                packageManager.queryIntentActivities(
                        launcherIntent,
                        0
                );

        // Exact name
        for (ResolveInfo info : apps) {

            if (info.activityInfo == null) {
                continue;
            }

            ApplicationInfo appInfo =
                    info.activityInfo.applicationInfo;

            String label =
                    appInfo.loadLabel(
                            packageManager
                    ).toString();

            if (label.toLowerCase(Locale.ROOT)
                    .equals(wanted)) {

                return launch(appInfo.packageName);
            }
        }

        // Partial name
        for (ResolveInfo info : apps) {

            if (info.activityInfo == null) {
                continue;
            }

            ApplicationInfo appInfo =
                    info.activityInfo.applicationInfo;

            String label =
                    appInfo.loadLabel(
                            packageManager
                    ).toString();

            String lowerLabel =
                    label.toLowerCase(Locale.ROOT);

            if (lowerLabel.contains(wanted) ||
                    wanted.contains(lowerLabel)) {

                return launch(appInfo.packageName);
            }
        }

        return false;
    }

    private boolean launch(String packageName) {

        try {

            Intent intent =
                    packageManager.getLaunchIntentForPackage(
                            packageName
                    );

            if (intent == null) {
                return false;
            }

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            );

            context.startActivity(intent);

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
                }
