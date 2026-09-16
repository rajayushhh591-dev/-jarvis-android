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
import android.speech.tts.TextToSpeech;

import com.jarvis.assistant.voice.VoiceEngine;

import java.util.Locale;

public class MainActivity extends Activity implements VoiceEngine.Listener {

    private TextView statusText;
    private VoiceEngine voiceEngine;
    private TextToSpeech textToSpeech;

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

        // Text To Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                speak("Jarvis ready.");
            }
        });

        // Microphone permission
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }

        voiceEngine = new VoiceEngine(this, this);
        voiceEngine.start();
    }

    @Override
    public void onListening() {
        runOnUiThread(() ->
                statusText.setText("JARVIS\n\nListening...")
        );
    }

    @Override
    public void onResult(String text) {

        String command = text.toLowerCase(Locale.ROOT).trim();

        runOnUiThread(() ->
                statusText.setText("You:\n" + text)
        );

        handleCommand(command);
    }

    @Override
    public void onError(String error) {

        runOnUiThread(() ->
                statusText.setText("JARVIS\n\n" + error)
        );
    }

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

            reply("I am doing great. I am ready to help you.");

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

        // Exit
        else if (command.contains("stop jarvis")
                || command.contains("close jarvis")) {

            reply("Okay. Main ruk raha hoon.");

            if (voiceEngine != null) {
                voiceEngine.stop();
            }
        }

        // Unknown command
        else {

            reply("Sorry, main abhi is command ko nahi samajh paaya.");
        }
    }

    private void reply(String message) {

        runOnUiThread(() ->
                statusText.setText("JARVIS:\n" + message)
        );

        speak(message);
    }

    private void speak(String message) {

        if (textToSpeech == null) {
            return;
        }

        textToSpeech.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_REPLY"
        );
    }

    private void openApp(String packageName) {

        try {

            Intent intent = getPackageManager()
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
