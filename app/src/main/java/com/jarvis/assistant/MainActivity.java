package com.jarvis.assistant;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;

import com.jarvis.assistant.voice.VoiceEngine;

public class MainActivity extends Activity implements VoiceEngine.Listener {

    private TextView statusText;
    private VoiceEngine voiceEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);

        statusText = new TextView(this);
        statusText.setText("JARVIS\n\nInitializing...");
        statusText.setTextColor(Color.CYAN);
        statusText.setTextSize(24);
        statusText.setGravity(Gravity.CENTER);

        layout.addView(statusText);

        setContentView(layout);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 100
            );
        }

        voiceEngine = new VoiceEngine(this, this);
        voiceEngine.start();
    }

    @Override
    public void onListening() {
        runOnUiThread(() -> statusText.setText("JARVIS\n\nListening..."));
    }

    @Override
    public void onResult(String text) {
        runOnUiThread(() -> statusText.setText("JARVIS\n\n" + text));
        handleCommand(text.toLowerCase());
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> statusText.setText("JARVIS\n\n" + error));
    }

    private void handleCommand(String command) {
        try {
            if (command.contains("youtube")) {
                openApp("com.google.android.youtube");
            } else if (command.contains("whatsapp")) {
                openApp("com.whatsapp");
            } else if (command.contains("camera")) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(intent);
            } else if (command.contains("settings")) {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            }
        } catch (Exception e) {
            statusText.setText("App open nahi ho saka.");
        }
    }

    private void openApp(String packageName) {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage(packageName);

        if (intent != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        if (voiceEngine != null) {
            voiceEngine.stop();
        }
        super.onDestroy();
    }
}
