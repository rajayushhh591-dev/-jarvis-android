package com.jarvis.assistant;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;
import java.util.Locale;

public class AppLauncher {

    private final Context context;
    private final PackageManager packageManager;

    public AppLauncher(Context context) {

        this.context = context;
        this.packageManager =
                context.getPackageManager();
    }

    public boolean openApp(String appName) {

        if (appName == null ||
                appName.trim().isEmpty()) {

            return false;
        }

        String wanted =
                normalize(appName);

        List<ResolveInfo> apps =
                getLauncherApps();

        // Exact app-name match
        for (ResolveInfo info : apps) {

            String label =
                    String.valueOf(
                            info.loadLabel(
                                    packageManager
                            )
                    );

            if (normalize(label).equals(wanted)) {

                return launch(info);
            }
        }

        // Partial app-name match
        for (ResolveInfo info : apps) {

            String label =
                    String.valueOf(
                            info.loadLabel(
                                    packageManager
                            )
                    );

            String normalized =
                    normalize(label);

            if (normalized.contains(wanted) ||
                    wanted.contains(normalized)) {

                if (normalized.length() >= 3) {

                    return launch(info);
                }
            }
        }

        // Known app aliases
        String packageName =
                findKnownPackage(wanted);

        if (packageName != null) {

            Intent intent =
                    packageManager
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (intent != null) {

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                try {

                    context.startActivity(intent);

                    return true;

                } catch (Exception ignored) {
                }
            }
        }

        return false;
    }

    private boolean launch(
            ResolveInfo info) {

        Intent intent =
                new Intent(
                        Intent.ACTION_MAIN
                );

        intent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        intent.setClassName(
                info.activityInfo.packageName,
                info.activityInfo.name
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        try {

            context.startActivity(intent);

            return true;

        } catch (Exception e) {

            return false;
        }
    }

    private List<ResolveInfo>
    getLauncherApps() {

        Intent intent =
                new Intent(
                        Intent.ACTION_MAIN
                );

        intent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        return packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_ALL
        );
    }

    private String normalize(
            String text) {

        return text
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
    }

    private String findKnownPackage(
            String name) {

        if (name.contains("whatsapp")) {
            return "com.whatsapp";
        }

        if (name.contains("instagram")) {
            return "com.instagram.android";
        }

        if (name.contains("telegram")) {
            return "org.telegram.messenger";
        }

        if (name.contains("snapchat")) {
            return "com.snapchat.android";
        }

        if (name.contains("chrome")) {
            return "com.android.chrome";
        }

        if (name.contains("gmail")) {
            return "com.google.android.gm";
        }

        if (name.contains("youtube")) {
            return "com.google.android.youtube";
        }

        return null;
    }
}
