package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jarvis.assistant.voice.VoiceEngine;

import java.util.Locale;

public class MainActivity extends Activity
        implements VoiceEngine.Listener {

    private TextView statusText;

    private VoiceEngine voiceEngine;
    private TextToSpeech textToSpeech;

    private boolean ttsReady = false;
    private boolean permissionGranted = false;
    private boolean startupFinished = false;

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

        // -------------------------
        // MICROPHONE PERMISSION
        // -------------------------

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            permissionGranted = true;

        } else {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }

        // -------------------------
        // VOICE ENGINE
        // -------------------------

        voiceEngine = new VoiceEngine(this, this);

        // -------------------------
        // TEXT TO SPEECH
        // -------------------------

        textToSpeech = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                int result =
                        textToSpeech.setLanguage(Locale.getDefault());

                if (result != TextToSpeech.LANG_MISSING_DATA
                        && result != TextToSpeech.LANG_NOT_SUPPORTED) {

                    ttsReady = true;

                    textToSpeech.setOnUtteranceProgressListener(
                            new UtteranceProgressListener() {

                                @Override
                                public void onStart(String utteranceId) {
                                }

                                @Override
                                public void onDone(String utteranceId) {

                                    runOnUiThread(() -> {

                                        if (voiceEngine != null) {
                                            voiceEngine.setSpeaking(false);
                                        }

                                        if ("STARTUP".equals(utteranceId)) {
                                            startupFinished = true;
                                        }
                                    });
                                }

                                @Override
                                public void onError(String utteranceId) {

                                    runOnUiThread(() -> {

                                        if (voiceEngine != null) {
                                            voiceEngine.setSpeaking(false);
                                        }

                                        if ("STARTUP".equals(utteranceId)) {
                                            startupFinished = true;
                                        }
                                    });
                                }
                            }
                    );

                    startJarvis();

                } else {

                    showStatus("JARVIS\n\nTTS language unavailable.");
                }

            } else {

                showStatus("JARVIS\n\nTTS start nahi ho saka.");
            }
        });
    }

    // -------------------------
    // PERMISSION RESULT
    // -------------------------

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
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                permissionGranted = true;

                startJarvis();

            } else {

                showStatus(
                        "JARVIS\n\nMicrophone permission required."
                );
            }
        }
    }

    // -------------------------
    // START JARVIS
    // -------------------------

    private void startJarvis() {

        if (!permissionGranted || !ttsReady) {
            return;
        }

        if (startupFinished) {
            return;
        }

        if (voiceEngine != null) {
            voiceEngine.setSpeaking(true);
        }

        showStatus("JARVIS\n\nHello.");

        speak("Jarvis ready.", "STARTUP");
    }

    // -------------------------
    // LISTENING
    // -------------------------

    @Override
    public void onListening() {

        runOnUiThread(() ->
                statusText.setText("JARVIS\n\nListening...")
        );
    }

    // -------------------------
    // RESULT
    // -------------------------

    @Override
    public void onResult(String text) {

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String command =
                text.toLowerCase(Locale.ROOT).trim();

        runOnUiThread(() ->
                statusText.setText("You:\n" + text)
        );

        handleCommand(command);
    }

    // -------------------------
    // ERROR
    // -------------------------

    @Override
    public void onError(String error) {

        // Recognition ke repeated errors ko screen par spam nahi karna.
        // Sirf important startup/system error dikhayenge.

        if (error == null || error.trim().isEmpty()) {
            return;
        }

        if (error.contains("available")) {
            showStatus("JARVIS\n\n" + error);
        }
    }

    // -------------------------
    // COMMAND HANDLER
    // -------------------------

    private void handleCommand(String command) {

        // Greeting
        if (command.contains("good morning")
                || command.contains("goodmorning")) {

            reply("Good morning.");

        } else if (command.contains("hello jarvis")
                || command.contains("hello")
                || command.contains("hi jarvis")
                || command.equals("hi")) {

            reply("Hello. How can I help you?");

        } else if (command.contains("how are you")) {

            reply(
                    "I am doing great. I am ready to help you."
            );

        }

        // YouTube
        else if (command.contains("youtube")
                || command.contains("you tube")) {

            reply("YouTube khol raha hoon.");
            openApp("com.google.android.youtube");
        }

        // WhatsApp
        else if (command.contains("whatsapp")
                || command.contains("what's app")) {

            reply("WhatsApp khol raha hoon.");
            openApp("com.whatsapp");
        }

        // Camera
        else if (command.contains("camera")) {

            reply("Camera khol raha hoon.");

            try {

                Intent intent = new Intent(
                        "android.media.action.IMAGE_CAPTURE"
                );

                startActivity(intent);

            } catch (Exception e) {

                reply("Camera open nahi ho saka.");
            }
        }

        // Settings
        else if (command.contains("settings")
                || command.contains("setting")) {

            reply("Settings khol raha hoon.");

            try {

                Intent intent = new Intent(
                        android.provider.Settings.ACTION_SETTINGS
                );

                startActivity(intent);

            } catch (Exception e) {

                reply("Settings open nahi ho saka.");
            }
        }

        // Stop
        else if (command.contains("stop jarvis")
                || command.contains("close jarvis")) {

            reply("Okay. Main ruk raha hoon.");

            if (voiceEngine != null) {
                voiceEngine.stop();
            }
        }

        // Unknown
        else {

            reply(
                    "Sorry, main abhi is command ko nahi samajh paaya."
            );
        }
    }

    // -------------------------
    // REPLY
    // -------------------------

    private void reply(String message) {

        showStatus("JARVIS:\n" + message);

        speak(message, "REPLY");
    }

    // -------------------------
    // SPEAK
    // -------------------------

    private void speak(
            String message,
            String utteranceId) {

        if (textToSpeech == null || !ttsReady) {
            return;
        }

        if (voiceEngine != null) {
            voiceEngine.setSpeaking(true);
        }

        textToSpeech.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
        );
    }

    // -------------------------
    // OPEN APP
    // -------------------------

    private void openApp(String packageName) {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(packageName);

            if (intent != null) {

                startActivity(intent);

            } else {

                reply("Ye app phone mein nahi mili.");
            }

        } catch (Exception e) {

            reply("App open nahi ho saka.");
        }
    }

    // -------------------------
    // STATUS
    // -------------------------

    private void showStatus(String text) {

        runOnUiThread(() ->
                statusText.setText(text)
        );
    }

    // -------------------------
    // DESTROY
    // -------------------------

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
