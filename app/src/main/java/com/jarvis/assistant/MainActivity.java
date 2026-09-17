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
    private boolean microphoneReady = false;
    private boolean jarvisStarted = false;
    private boolean startupGreeting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);

        // -------------------------
        // UI
        // -------------------------

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);

        statusText = new TextView(this);

        statusText.setText(
                "JARVIS\n\nInitializing..."
        );

        statusText.setTextColor(Color.CYAN);
        statusText.setTextSize(24);
        statusText.setGravity(Gravity.CENTER);

        layout.addView(statusText);

        setContentView(layout);

        // -------------------------
        // VOICE ENGINE
        // -------------------------

        voiceEngine = new VoiceEngine(this, this);

        // -------------------------
        // MICROPHONE PERMISSION
        // -------------------------

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

        // -------------------------
        // TEXT TO SPEECH
        // -------------------------

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
                                                            .equals(utteranceId)) {

                                                        startupGreeting = false;

                                                        startListening();
                                                    }

                                                    else {

                                                        if (voiceEngine != null) {
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
                                                            .equals(utteranceId)) {

                                                        startupGreeting = false;

                                                        startListening();
                                                    }

                                                    else {

                                                        if (voiceEngine != null) {
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

                        // TTS fail ho tab bhi listening try karo.
                        startListening();
                    }
                }
        );
    }

    // =====================================================
    // START JARVIS
    // =====================================================

    private void startJarvis() {

        if (jarvisStarted) {
            return;
        }

        if (!microphoneReady) {
            return;
        }

        if (!ttsReady) {
            return;
        }

        jarvisStarted = true;
        startupGreeting = true;

        showStatus(
                "JARVIS\n\nHello."
        );

        speak(
                "Jarvis ready.",
                "STARTUP"
        );
    }

    // =====================================================
    // START LISTENING
    // =====================================================

    private void startListening() {

        if (!microphoneReady) {
            return;
        }

        if (voiceEngine == null) {
            return;
        }

        if (!voiceEngineIsRunning()) {

            voiceEngine.start();

        } else {

            voiceEngine.setSpeaking(false);
        }
    }

    /*
     * VoiceEngine ke andar direct running state expose nahi hai.
     * Isliye start() ko safely ek baar call karne ke liye
     * startup flag use kar rahe hain.
     */
    private boolean voiceEngineIsRunning() {
        return jarvisStarted && !startupGreeting;
    }

    // =====================================================
    // MICROPHONE PERMISSION RESULT
    // =====================================================

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

    // =====================================================
    // LISTENING CALLBACK
    // =====================================================

    @Override
    public void onListening() {

        runOnUiThread(() ->
                statusText.setText(
                        "JARVIS\n\nListening..."
                )
        );
    }

    // =====================================================
    // SPEECH RESULT
    // =====================================================

    @Override
    public void onResult(String text) {

        if (text == null) {
            return;
        }

        text = text.trim();

        if (text.isEmpty()) {
            return;
        }

        final String finalText = text;

        String command =
                text.toLowerCase(
                        Locale.ROOT
                ).trim();

        runOnUiThread(() ->
                statusText.setText(
                        "You:\n" + finalText
                )
        );

        handleCommand(command);
    }

    // =====================================================
    // RECOGNITION ERROR
    // =====================================================

    @Override
    public void onError(String error) {

        if (error == null) {
            return;
        }

        // Recognition ke normal errors ko screen par
        // repeatedly nahi dikhana.
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

        else if (command.contains("hello jarvis")
                || command.contains("hello")
                || command.contains("hi jarvis")
                || command.equals("hi")) {

            reply(
                    "Hello. How can I help you?"
            );
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
        // YOUTUBE
        // -------------------------

        else if (command.contains("youtube")
                || command.contains("you tube")) {

            reply(
                    "YouTube khol raha hoon."
            );

            openApp(
                    "com.google.android.youtube"
            );
        }

        // -------------------------
        // WHATSAPP
        // -------------------------

        else if (command.contains("whatsapp")
                || command.contains("what's app")) {

            reply(
                    "WhatsApp khol raha hoon."
            );

            openApp(
                    "com.whatsapp"
            );
        }

        // -------------------------
        // CAMERA
        // -------------------------

        else if (command.contains("camera")) {

            reply(
                    "Camera khol raha hoon."
            );

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

        // -------------------------
        // SETTINGS
        // -------------------------

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

        // -------------------------
        // STOP
        // -------------------------

        else if (command.contains("stop jarvis")
                || command.contains("close jarvis")) {

            reply(
                    "Okay. Main ruk raha hoon."
            );

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

        if (textToSpeech == null) {
            return;
        }

        if (!ttsReady) {
            return;
        }

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
    // OPEN APP
    // =====================================================

    private void openApp(String packageName) {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (intent != null) {

                startActivity(intent);

            } else {

                reply(
                        "Ye app phone mein nahi mili."
                );
            }

        } catch (Exception e) {

            reply(
                    "App open nahi ho saka."
            );
        }
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
