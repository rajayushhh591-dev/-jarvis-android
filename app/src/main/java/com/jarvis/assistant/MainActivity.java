package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jarvis.assistant.voice.VoiceEngine;

import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends Activity {

    private VoiceEngine voice;
    private TextToSpeech tts;

    private JarvisFaceView face;
    private TextView status;
    private TextView output;

    private boolean running = false;

    private static final int MIC_PERMISSION = 100;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        buildUI();

        initializeVoice();

        initializeTTS();

        requestMicrophone();
    }

    // ==============================
    // USER INTERFACE
    // ==============================

    private void buildUI() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setBackgroundColor(
                0xFF000000
        );

        face =
                new JarvisFaceView(this);

        LinearLayout.LayoutParams faceParams =
                new LinearLayout.LayoutParams(
                        650,
                        650
                );

        faceParams.gravity =
                Gravity.CENTER;

        root.addView(
                face,
                faceParams
        );

        status =
                new TextView(this);

        status.setText(
                "JARVIS STARTING..."
        );

        status.setTextSize(22);

        status.setGravity(
                Gravity.CENTER
        );

        status.setTextColor(
                0xFFFFFFFF
        );

        root.addView(status);

        output =
                new TextView(this);

        output.setTextSize(18);

        output.setGravity(
                Gravity.CENTER
        );

        output.setTextColor(
                0xFFFFFFFF
        );

        root.addView(output);

        setContentView(root);
    }

    // ==============================
    // VOICE
    // ==============================

    private void initializeVoice() {

        voice =
                new VoiceEngine(
                        this,
                        new VoiceEngine.Listener() {

                    @Override
                    public void onListening() {

                        runOnUiThread(() -> {

                            face.setState(
                                    JarvisFaceView.LISTENING
                            );

                            status.setText(
                                    "🎤 LISTENING"
                            );
                        });
                    }

                    @Override
                    public void onResult(
                            String text) {

                        runOnUiThread(() -> {

                            face.setState(
                                    JarvisFaceView.THINKING
                            );

                            status.setText(
                                    "🧠 PROCESSING"
                            );

                            output.setText(
                                    "YOU: " + text
                            );

                            handleCommand(text);
                        });
                    }

                    @Override
                    public void onError(
                            String error) {

                        if (running) {

                            runOnUiThread(() -> {

                                face.setState(
                                        JarvisFaceView.LISTENING
                                );

                                status.setText(
                                        "🎤 LISTENING"
                                );
                            });
                        }
                    }
                }
        );
    }

    // ==============================
    // TEXT TO SPEECH
    // ==============================

    private void initializeTTS() {

        tts =
                new TextToSpeech(
                        this,
                        result -> {

                    if (result ==
                            TextToSpeech.SUCCESS) {

                        tts.setLanguage(
                                Locale.US
                        );

                        tts.setSpeechRate(
                                0.95f
                        );

                        tts.setOnUtteranceProgressListener(
                                new UtteranceProgressListener() {

                            @Override
                            public void onStart(
                                    String id) {

                                runOnUiThread(() -> {

                                    face.setState(
                                            JarvisFaceView.SPEAKING
                                    );

                                    status.setText(
                                            "🔊 SPEAKING"
                                    );
                                });
                            }

                            @Override
                            public void onDone(
                                    String id) {

                                if (running) {

                                    runOnUiThread(() -> {

                                        face.setState(
                                                JarvisFaceView.LISTENING
                                        );

                                        status.setText(
                                                "🎤 LISTENING"
                                        );

                                        voice.start();
                                    });
                                }
                            }

                            @Override
                            public void onError(
                                    String id) {}
                        });

                        running = true;

                        speak(
                                "Hello sir, I am Jarvis."
                        );
                    }
                }
        );
    }

    // ==============================
    // MICROPHONE
    // ==============================

    private void requestMicrophone() {

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    MIC_PERMISSION
            );

        } else {

            startJarvis();
        }
    }

    private void startJarvis() {

        running = true;

        // TTS welcome message finishes,
        // then VoiceEngine starts automatically.
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] results) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                results
        );

        if (requestCode ==
                MIC_PERMISSION) {

            if (results.length > 0 &&
                    results[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                startJarvis();

            } else {

                status.setText(
                        "Microphone permission required"
                );
            }
        }
    }

    // ==============================
    // COMMAND ROUTER
    // ==============================

    private void handleCommand(
            String original) {

        String text =
                original
                        .toLowerCase(Locale.ROOT)
                        .trim();

        // HELLO

        if (text.contains("hello") ||
                text.contains("hi jarvis") ||
                text.equals("hi")) {

            speak(
                    "Hello sir, how can I help you?"
            );

            return;
        }

        // NAME

        if (text.contains("your name") ||
                text.contains("who are you")) {

            speak(
                    "I am Jarvis, your personal assistant."
            );

            return;
        }

        // YOUTUBE

        if (text.contains("youtube")) {

            String query =
                    extractYouTubeQuery(text);

            openYouTube(query);

            speak(
                    "Opening YouTube, sir."
            );

            return;
        }

        // WHATSAPP

        if (text.contains("whatsapp")) {

            openApp(
                    "com.whatsapp",
                    "WhatsApp"
            );

            return;
        }

        // INSTAGRAM

        if (text.contains("instagram")) {

            openApp(
                    "com.instagram.android",
                    "Instagram"
            );

            return;
        }

        // GMAIL

        if (text.contains("gmail")) {

            openApp(
                    "com.google.android.gm",
                    "Gmail"
            );

            return;
        }

        // CAMERA

        if (text.contains("camera")) {

            openCamera();

            return;
        }

        // SETTINGS

        if (text.contains("settings") ||
                text.contains("setting")) {

            openSettings();

            return;
        }

        // CHROME

        if (text.contains("chrome")) {

            openApp(
                    "com.android.chrome",
                    "Chrome"
            );

            return;
        }

        // GOOGLE SEARCH

        if (text.contains("search")) {

            String query =
                    text
                            .replace(
                                    "search",
                                    ""
                            )
                            .replace(
                                    "google",
                                    ""
                            )
                            .trim();

            if (query.isEmpty()) {

                query = "Google";
            }

            openGoogleSearch(query);

            speak(
                    "Searching Google, sir."
            );

            return;
        }

        // GOOGLE

        if (text.contains("open google") ||
                text.contains("google kholo")) {

            openGoogle();

            speak(
                    "Opening Google, sir."
            );

            return;
        }

        // STOP

        if (text.equals("stop") ||
                text.contains("stop listening")) {

            running = false;

            voice.stop();

            face.setState(
                    JarvisFaceView.IDLE
            );

            status.setText(
                    "JARVIS STOPPED"
            );

            speak(
                    "Okay sir. I am stopping."
            );

            return;
        }

        // UNKNOWN

        speak(
                "I heard you say " +
                        original +
                        ". I don't know that command yet."
        );
    }

    // ==============================
    // OPEN APP
    // ==============================

    private void openApp(
            String packageName,
            String appName) {

        face.setState(
                JarvisFaceView.ACTION
        );

        status.setText(
                "⚡ ACTION"
        );

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (intent != null) {

                startActivity(intent);

                speak(
                        "Opening " +
                                appName +
                                ", sir."
                );

            } else {

                speak(
                        appName +
                                " is not installed."
                );
            }

        } catch (Exception e) {

            speak(
                    "Sorry sir, I could not open " +
                            appName
            );
        }
    }

    // ==============================
    // YOUTUBE
    // ==============================

    private String extractYouTubeQuery(
            String text) {

        String query = text;

        String[] words = {

                "play",
                "youtube",
                "on youtube",
                "youtube par",
                "youtube pe",
                "song",
                "gana",
                "chalao",
                "chala",
                "bajao",
                "baja do",
                "open"
        };

        for (String word : words) {

            query =
                    query.replace(
                            word,
                            ""
                    );
        }

        query = query.trim();

        if (query.isEmpty()) {

            query =
                    "Arijit Singh songs";
        }

        return query;
    }

    private void openYouTube(
            String query) {

        try {

            String url =
                    "https://www.youtube.com/results?search_query="
                            +
                            URLEncoder.encode(
                                    query,
                                    "UTF-8"
                            );

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    )
            );

        } catch (Exception e) {

            openGoogleSearch(
                    query
            );
        }
    }

    // ==============================
    // GOOGLE SEARCH
    // ==============================

    private void openGoogleSearch(
            String query) {

        try {

            String url =
                    "https://www.google.com/search?q="
                            +
                            URLEncoder.encode(
                                    query,
                                    "UTF-8"
                            );

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    )
            );

        } catch (Exception ignored) {}
    }

    private void openGoogle() {

        startActivity(
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                                "https://www.google.com"
                        )
                )
        );
    }

    // ==============================
    // CAMERA
    // ==============================

    private void openCamera() {

        try {

            startActivity(
                    new Intent(
                            "android.media.action.IMAGE_CAPTURE"
                    )
            );

            speak(
                    "Opening camera, sir."
            );

        } catch (Exception e) {

            speak(
                    "Camera could not be opened."
            );
        }
    }

    // ==============================
    // SETTINGS
    // ==============================

    private void openSettings() {

        try {

            startActivity(
                    new Intent(
                            android.provider.Settings
                                    .ACTION_SETTINGS
                    )
            );

            speak(
                    "Opening settings, sir."
            );

        } catch (Exception e) {

            speak(
                    "I could not open settings."
            );
        }
    }

    // ==============================
    // SPEAK
    // ==============================

    private void speak(
            String message) {

        output.setText(
                "JARVIS: " + message
        );

        face.setState(
                JarvisFaceView.SPEAKING
        );

        status.setText(
                "🔊 SPEAKING"
        );

        if (tts != null) {

            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "JARVIS_" +
                            System.currentTimeMillis()
            );
        }
    }

    // ==============================
    // DESTROY
    // ==============================

    @Override
    protected void onDestroy() {

        running = false;

        if (voice != null) {

            voice.stop();
            voice.destroy();
        }

        if (tts != null) {

            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
}
