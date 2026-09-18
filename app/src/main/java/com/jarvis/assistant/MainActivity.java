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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jarvis.assistant.voice.VoiceEngine;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity
        implements VoiceEngine.Listener {

    private static final int CAMERA_REQUEST = 200;

    private TextView statusText;

    private VoiceEngine voiceEngine;
    private TextToSpeech textToSpeech;

    private boolean ttsReady = false;
    private boolean microphoneReady = false;
    private boolean jarvisStarted = false;

    private boolean photoMode = false;

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

        voiceEngine = new VoiceEngine(this, this);

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

        textToSpeech = new TextToSpeech(
                this,
                status -> {

                    if (status == TextToSpeech.SUCCESS) {

                        int languageResult =
                                textToSpeech.setLanguage(
                                        Locale.getDefault()
                                );

                        if (languageResult ==
                                TextToSpeech.LANG_MISSING_DATA
                                ||
                                languageResult ==
                                TextToSpeech.LANG_NOT_SUPPORTED) {

                            textToSpeech.setLanguage(Locale.US);
                        }

                        ttsReady = true;

                        textToSpeech.setOnUtteranceProgressListener(
                                new UtteranceProgressListener() {

                                    @Override
                                    public void onStart(
                                            String utteranceId) {
                                    }

                                    @Override
                                    public void onDone(
                                            String utteranceId) {

                                        runOnUiThread(() -> {

                                            if ("STARTUP".equals(
                                                    utteranceId)) {

                                                startListening();

                                            } else {

                                                if (voiceEngine != null) {
                                                    voiceEngine.setSpeaking(false);
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(
                                            String utteranceId) {

                                        runOnUiThread(() -> {

                                            if ("STARTUP".equals(
                                                    utteranceId)) {

                                                startListening();

                                            } else {

                                                if (voiceEngine != null) {
                                                    voiceEngine.setSpeaking(false);
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

    private void startJarvis() {

        if (jarvisStarted) return;
        if (!microphoneReady) return;
        if (!ttsReady) return;

        jarvisStarted = true;

        showStatus("JARVIS\n\nHello.");

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

        runOnUiThread(() ->
                statusText.setText(
                        "JARVIS\n\nListening..."
                )
        );
    }

    @Override
    public void onResult(String text) {

        if (text == null) return;

        text = text.trim();

        if (text.isEmpty()) return;

        final String finalText = text;

        String command =
                text.toLowerCase(Locale.ROOT).trim();

        runOnUiThread(() ->
                statusText.setText(
                        "You:\n" + finalText
                )
        );

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

    // =====================================================
    // COMMAND HANDLER
    // =====================================================

    private void handleCommand(String command) {

        // -------------------------
        // GREETING
        // -------------------------

        if (command.contains("good morning")
                || command.contains("goodmorning")) {

            reply("Good morning.");
        }

        // -------------------------
        // HELLO
        // -------------------------

        else if (command.contains("hello jarvis")
                || command.contains("hello")
                || command.contains("hi jarvis")
                || command.equals("hi")) {

            reply("Hello. How can I help you?");
        }

        // -------------------------
        // HOW ARE YOU
        // -------------------------

        else if (command.contains("how are you")) {

            reply(
                    "I am doing great. I am ready to help you."
            );
        }

        // -------------------------
        // TAKE PHOTO
        // -------------------------

        else if (command.contains("take a photo")
                || command.contains("take photo")
                || command.contains("capture photo")
                || command.contains("click a photo")
                || command.contains("take picture")
                || command.contains("take a picture")) {

            openCameraForPhoto();
        }

        // -------------------------
        // ONE MORE PHOTO
        // -------------------------

        else if (command.contains("one more photo")
                || command.contains("another photo")
                || command.contains("one more picture")
                || command.contains("another picture")
                || command.contains("ek aur photo")
                || command.contains("ek aur picture")) {

            openCameraForPhoto();
        }

        // -------------------------
        // YOUTUBE
        // -------------------------

        else if (command.contains("youtube")
                || command.contains("you tube")) {

            reply("YouTube khol raha hoon.");

            openAppByName("youtube");
        }

        // -------------------------
        // WHATSAPP
        // -------------------------

        else if (command.contains("whatsapp")
                || command.contains("what's app")) {

            reply("WhatsApp khol raha hoon.");

            openAppByName("whatsapp");
        }

        // -------------------------
        // CAMERA
        // -------------------------

        else if (command.contains("camera")) {

            reply("Camera khol raha hoon.");

            openCamera();
        }

        // -------------------------
        // SETTINGS
        // -------------------------

        else if (command.contains("settings")
                || command.contains("setting")) {

            reply("Settings khol raha hoon.");

            try {

                Intent intent =
                        new Intent(
                                android.provider.Settings
                                        .ACTION_SETTINGS
                        );

                startActivity(intent);

            } catch (Exception e) {

                reply("Settings open nahi ho saka.");
            }
        }

        // -------------------------
        // GENERIC OPEN APP
        // -------------------------

        else if (command.startsWith("open ")
                || command.startsWith("launch ")
                || command.startsWith("start ")) {

            String appName = command;

            if (appName.startsWith("open ")) {
                appName = appName.substring(5);
            } else if (appName.startsWith("launch ")) {
                appName = appName.substring(7);
            } else if (appName.startsWith("start ")) {
                appName = appName.substring(6);
            }

            appName = appName.trim();

            if (!appName.isEmpty()) {

                openAppByName(appName);

            } else {

                reply("Kaunsa app kholna hai?");
            }
        }

        // -------------------------
        // STOP
        // -------------------------

        else if (command.contains("stop jarvis")
                || command.contains("close jarvis")) {

            reply("Okay. Main ruk raha hoon.");

            if (voiceEngine != null) {
                voiceEngine.stop();
            }
        }

        // -------------------------
        // UNKNOWN
        // -------------------------

        else {

            reply(
                    "Sorry, main abhi is command ko nahi samajh paaya."
            );
        }
    }

    // =====================================================
    // OPEN CAMERA FOR PHOTO
    // =====================================================

    private void openCameraForPhoto() {

        photoMode = true;

        reply("Camera khol raha hoon. Photo lene ke baad wapas aa jana.");

        try {

            Intent intent =
                    new Intent(
                            "android.media.action.IMAGE_CAPTURE"
                    );

            startActivityForResult(
                    intent,
                    CAMERA_REQUEST
            );

        } catch (Exception e) {

            photoMode = false;

            reply("Camera open nahi ho saka.");
        }
    }

    // =====================================================
    // OPEN CAMERA
    // =====================================================

    private void openCamera() {

        photoMode = false;

        try {

            Intent intent =
                    new Intent(
                            "android.media.action.IMAGE_CAPTURE"
                    );

            startActivity(intent);

        } catch (Exception e) {

            reply("Camera open nahi ho saka.");
        }
    }

    // =====================================================
    // CAMERA RESULT
    // =====================================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == CAMERA_REQUEST) {

            photoMode = false;

            if (resultCode == RESULT_OK) {

                reply(
                        "Photo ho gayi. Ek aur photo?"
                );

            } else {

                reply(
                        "Theek hai."
                );
            }
        }
    }

    // =====================================================
    // OPEN APP BY NAME
    // =====================================================

    private void openAppByName(String requestedName) {

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
                    packageManager.queryIntentActivities(
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
                        info.loadLabel(packageManager);

                if (label == null) {
                    continue;
                }

                String appLabel =
                        label.toString()
                                .toLowerCase(Locale.ROOT)
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

                    startActivity(launchIntent);

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

    // =====================================================
    // REPLY
    // =====================================================

    private void reply(String message) {

        showStatus(
                "JARVIS:\n" + message
        );

        speak(
                message,
                "REPLY"
        );
    }

    // =====================================================
    // SPEAK
    // =====================================================

    private void speak(
            String message,
            String utteranceId) {

        if (textToSpeech == null) return;
        if (!ttsReady) return;

        if (!"STARTUP".equals(utteranceId)) {

            if (voiceEngine != null) {
                voiceEngine.setSpeaking(true);
            }
        }

        textToSpeech.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
        );
    }

    // =====================================================
    // STATUS
    // =====================================================

    private void showStatus(String text) {

        runOnUiThread(() ->
                statusText.setText(text)
        );
    }

    // =====================================================
    // DESTROY
    // =====================================================

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
