package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jarvis.assistant.voice.VoiceEngine;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity
        implements VoiceEngine.Listener {

    private TextView statusText;
    private ImageView jarvisFace;

    private VoiceEngine voiceEngine;
    private TextToSpeech textToSpeech;

    private boolean ttsReady = false;
    private boolean microphoneReady = false;
    private boolean jarvisStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);

        jarvisFace = new ImageView(this);

        jarvisFace.setImageResource(
                R.drawable.jarvis_base_face
        );

        jarvisFace.setScaleType(
                ImageView.ScaleType.FIT_CENTER
        );

        LinearLayout.LayoutParams faceParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        faceParams.setMargins(
                20,
                30,
                20,
                10
        );

        layout.addView(
                jarvisFace,
                faceParams
        );

        statusText = new TextView(this);

        statusText.setText(
                "JARVIS\n\nInitializing..."
        );

        statusText.setTextColor(Color.CYAN);
        statusText.setTextSize(22);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(
                10,
                10,
                10,
                40
        );

        layout.addView(
                statusText,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(layout);

        startFaceIdleAnimation();

        voiceEngine =
                new VoiceEngine(this, this);

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED) {

            microphoneReady = true;

        } else {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    100
            );
        }

        textToSpeech =
                new TextToSpeech(
                        this,
                        status -> {

                            if (status ==
                                    TextToSpeech.SUCCESS) {

                                int languageResult =
                                        textToSpeech.setLanguage(
                                                Locale.getDefault()
                                        );

                                if (languageResult ==
                                        TextToSpeech.LANG_MISSING_DATA
                                        ||
                                        languageResult ==
                                        TextToSpeech.LANG_NOT_SUPPORTED) {

                                    textToSpeech.setLanguage(
                                            Locale.US
                                    );
                                }

                                ttsReady = true;

                                textToSpeech
                                        .setOnUtteranceProgressListener(
                                                new UtteranceProgressListener() {

                                                    @Override
                                                    public void onStart(
                                                            String utteranceId) {
                                                    }

                                                    @Override
                                                    public void onDone(
                                                            String utteranceId) {

                                                        runOnUiThread(() -> {

                                                            if ("STARTUP"
                                                                    .equals(
                                                                            utteranceId)) {

                                                                startListening();

                                                            } else {

                                                                if (voiceEngine
                                                                        != null) {

                                                                    voiceEngine
                                                                            .setSpeaking(
                                                                                    false
                                                                            );
                                                                }
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onError(
                                                            String utteranceId) {

                                                        runOnUiThread(() -> {

                                                            if ("STARTUP"
                                                                    .equals(
                                                                            utteranceId)) {

                                                                startListening();

                                                            } else {

                                                                if (voiceEngine
                                                                        != null) {

                                                                    voiceEngine
                                                                            .setSpeaking(
                                                                                    false
                                                                            );
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                        );

                                startJarvis();

                            } else {

                                showStatus(
                                        "JARVIS\n\nTTS start nahi ho saka."
                                );

                                startListening();
                            }
                        }
                );
    }

    private void startFaceIdleAnimation() {

        AlphaAnimation animation =
                new AlphaAnimation(
                        0.82f,
                        1.0f
                );

        animation.setDuration(1400);
        animation.setRepeatMode(
                AlphaAnimation.REVERSE
        );
        animation.setRepeatCount(
                AlphaAnimation.INFINITE
        );

        jarvisFace.startAnimation(animation);
    }

    private void startJarvis() {

        if (jarvisStarted) return;
        if (!microphoneReady) return;
        if (!ttsReady) return;

        jarvisStarted = true;

        showStatus(
                "JARVIS\n\nHello."
        );

        speak(
                "Jarvis ready.",
                "STARTUP"
        );
    }

    private void startListening() {

        if (!microphoneReady) return;
        if (voiceEngine == null) return;

        voiceEngine.start();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 100) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                microphoneReady = true;

                startJarvis();

            } else {

                showStatus(
                        "JARVIS\n\nMicrophone permission required."
                );
            }
        }
    }

    @Override
    public void onListening() {

        runOnUiThread(() -> {

            statusText.setText(
                    "JARVIS\n\nListening..."
            );

            jarvisFace.animate()
                    .scaleX(1.04f)
                    .scaleY(1.04f)
                    .setDuration(250)
                    .start();
        });
    }

    @Override
    public void onResult(String text) {

        if (text == null) return;

        text = text.trim();

        if (text.isEmpty()) return;

        final String finalText = text;

        String command =
                text.toLowerCase(Locale.ROOT).trim();

        runOnUiThread(() -> {

            statusText.setText(
                    "You:\n" + finalText
            );

            jarvisFace.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
        });

        handleCommand(command);
    }

    @Override
    public void onError(String error) {

        if (error == null) return;

        if (error.contains("available")) {

            showStatus(
                    "JARVIS\n\n" + error
            );
        }
    }

    private void handleCommand(String command) {

        if (command.contains("good morning")
                || command.contains("goodmorning")) {

            reply("Good morning.");
        }

        else if (command.contains("hello jarvis")
                || command.contains("hello")
                || command.contains("hi jarvis")
                || command.equals("hi")) {

            reply(
                    "Hello. How can I help you?"
            );
        }

        else if (command.contains("how are you")) {

            reply(
                    "I am doing great. I am ready to help you."
            );
        }

        else if (command.contains("youtube")
                || command.contains("you tube")) {

            reply(
                    "YouTube khol raha hoon."
            );

            openAppByName("youtube");
        }

        else if (command.contains("whatsapp")
                || command.contains("what's app")) {

            reply(
                    "WhatsApp khol raha hoon."
            );

            openAppByName("whatsapp");
        }

        else if (command.contains("camera")) {

            reply(
                    "Camera khol raha hoon."
            );

            openCamera();
        }

        else if (command.contains("settings")
                || command.contains("setting")) {

            reply(
                    "Settings khol raha hoon."
            );

            try {

                Intent intent =
                        new Intent(
                                android.provider.Settings
                                        .ACTION_SETTINGS
                        );

                startActivity(intent);

            } catch (Exception e) {

                reply(
                        "Settings open nahi ho saka."
                );
            }
        }

        else if (command.startsWith("open ")
                || command.startsWith("launch ")
                || command.startsWith("start ")) {

            String appName = command;

            if (appName.startsWith("open ")) {

                appName =
                        appName.substring(5);

            } else if (appName.startsWith("launch ")) {

                appName =
                        appName.substring(7);

            } else if (appName.startsWith("start ")) {

                appName =
                        appName.substring(6);
            }

            appName = appName.trim();

            if (!appName.isEmpty()) {

                openAppByName(appName);

            } else {

                reply(
                        "Kaunsa app kholna hai?"
                );
            }
        }

        else if (command.contains("stop jarvis")
                || command.contains("close jarvis")) {

            reply(
                    "Okay. Main ruk raha hoon."
            );

            if (voiceEngine != null) {
                voiceEngine.stop();
            }
        }

        else {

            reply(
                    "Sorry, main abhi is command ko nahi samajh paaya."
            );
        }
    }

    private void openCamera() {

        try {

            Intent intent =
                    new Intent(
                            "android.media.action.IMAGE_CAPTURE"
                    );

            startActivity(intent);

        } catch (Exception e) {

            reply(
                    "Camera open nahi ho saka."
            );
        }
    }

    private void openAppByName(
            String requestedName) {

        try {

            PackageManager packageManager =
                    getPackageManager();

            Intent launcherIntent =
                    new Intent(
                            Intent.ACTION_MAIN,
                            null
                    );

            launcherIntent.addCategory(
                    Intent.CATEGORY_LAUNCHER
            );

            List<ResolveInfo> apps =
                    packageManager
                            .queryIntentActivities(
                                    launcherIntent,
                                    0
                            );

            String wanted =
                    requestedName
                            .toLowerCase(Locale.ROOT)
                            .trim();

            for (ResolveInfo info : apps) {

                if (info.activityInfo == null) {
                    continue;
                }

                CharSequence label =
                        info.loadLabel(
                                packageManager
                        );

                if (label == null) {
                    continue;
                }

                String appLabel =
                        label.toString()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .trim();

                if (appLabel.equals(wanted)
                        || appLabel.contains(wanted)
                        || wanted.contains(appLabel)) {

                    Intent launchIntent =
                            new Intent();

                    launchIntent.setClassName(
                            info.activityInfo.packageName,
                            info.activityInfo.name
                    );

                    launchIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    reply(
                            label.toString()
                                    + " khol raha hoon."
                    );

                    startActivity(
                            launchIntent
                    );

                    return;
                }
            }

            reply(
                    requestedName
                            + " phone mein nahi mila."
            );

        } catch (Exception e) {

            reply(
                    requestedName
                            + " open nahi ho saka."
            );
        }
    }

    private void reply(String message) {

        showStatus(
                "JARVIS:\n" + message
        );

        speak(
                message,
                "REPLY"
        );
    }

    private void speak(
            String message,
            String utteranceId) {

        if (textToSpeech == null) return;
        if (!ttsReady) return;

        if (!"STARTUP".equals(utteranceId)) {

            if (voiceEngine != null) {

                voiceEngine.setSpeaking(
                        true
                );
            }
        }

        textToSpeech.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
        );
    }

    private void showStatus(String text) {

        runOnUiThread(() ->
                statusText.setText(text)
        );
    }

    @Override
    protected void onDestroy() {

        if (voiceEngine != null) {
            voiceEngine.stop();
        }

        if (textToSpeech != null) {

            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }
}
