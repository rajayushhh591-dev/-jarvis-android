
package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.Gravity;

import com.jarvis.assistant.voice.VoiceEngine;
import java.util.Locale;

public class MainActivity extends Activity {

    private VoiceEngine voiceEngine;
    private TextToSpeech tts;
    private TextView status, output;
    private static final int MIC_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(25, 25, 25, 25);

        status = new TextView(this);
        output = new TextView(this);

        status.setText("JARVIS Starting...");
        status.setTextSize(22);
        output.setTextSize(18);

        layout.addView(status);
        layout.addView(output);
        setContentView(layout);

        initializeTTS();
        initializeVoice();
        requestMicrophone();
    }

    private void initializeTTS() {
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                reply("Hello sir, I am Jarvis.");
            }
        });
    }

    private void initializeVoice() {
        voiceEngine = new VoiceEngine(this,
                new VoiceEngine.Listener() {

            @Override
            public void onListening() {
                runOnUiThread(() ->
                        status.setText("Listening..."));
            }

            @Override
            public void onResult(String text) {
                runOnUiThread(() -> {
                    output.setText("You: " + text);
                    handleCommand(text);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        status.setText("Error: " + error));
            }
        });
    }

    private void requestMicrophone() {
        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    }, MIC_CODE);

        } else {
            voiceEngine.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] results) {

        super.onRequestPermissionsResult(
                requestCode, permissions, results);

        if (requestCode == MIC_CODE &&
                results.length > 0 &&
                results[0] == PackageManager.PERMISSION_GRANTED) {

            voiceEngine.start();
        }
    }

    private void handleCommand(String text) {
        text = text.toLowerCase(Locale.ROOT);

        if (text.contains("hello") ||
                text.contains("hi")) {

            reply("Hello sir, how can I help you?");

        } else if (text.contains("your name")) {

            reply("My name is Jarvis.");

        } else if (text.contains("stop")) {

            voiceEngine.stop();
            reply("Okay sir, stopping.");

        } else {

            reply("Sorry sir, I don't understand.");
        }
    }

    private void reply(String message) {
        output.setText("Jarvis: " + message);

        if (tts != null) {
            tts.speak(message,
                    TextToSpeech.QUEUE_FLUSH,
                    null, "JARVIS");
        }
    }

    @Override
    protected void onDestroy() {
        if (voiceEngine != null) {
            voiceEngine.stop();
            voiceEngine.destroy();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
