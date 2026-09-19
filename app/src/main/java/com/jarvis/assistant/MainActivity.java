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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jarvis.assistant.voice.VoiceEngine;

import java.util.Locale;

public class MainActivity extends Activity {

    private VoiceEngine voiceEngine;
    private TextToSpeech tts;

    private TextView status;
    private TextView output;

    private boolean jarvisActive = false;

    private static final int MIC_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createInterface();

        initializeTTS();
        initializeVoice();
        requestMicrophone();
    }

    private void createInterface() {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setGravity(
                Gravity.CENTER
        );

        layout.setPadding(
                25, 25, 25, 25
        );

        // JARVIS FACE
        ImageView face =
                new ImageView(this);

        /*
         * Add your JARVIS image as:
         *
         * app/src/main/res/drawable/jarvis_face.png
         *
         * Then this will display it.
         */

        int faceId =
                getResources().getIdentifier(
                        "jarvis_face",
                        "drawable",
                        getPackageName()
                );

        if (faceId != 0) {
            face.setImageResource(faceId);
            face.setAdjustViewBounds(true);
        }

        layout.addView(
                face,
                new LinearLayout.LayoutParams(
                        500,
                        500
                )
        );

        status = new TextView(this);

        status.setText(
                "JARVIS STARTING..."
        );

        status.setTextSize(22);

        status.setGravity(
                Gravity.CENTER
        );

        layout.addView(status);

        output = new TextView(this);

        output.setTextSize(18);

        output.setGravity(
                Gravity.CENTER
        );

        layout.addView(output);

        setContentView(layout);
    }

    private void initializeTTS() {

        tts = new TextToSpeech(
                this,
                result -> {

                    if (result ==
                            TextToSpeech.SUCCESS) {

                        tts.setLanguage(
                                Locale.US
                        );

                        tts.setSpeechRate(0.95f);

                        tts.setOnUtteranceProgressListener(
                                new UtteranceProgressListener() {

                            @Override
                            public void onStart(
                                    String utteranceId) {
                            }

                            @Override
                            public void onDone(
                                    String utteranceId) {

                                if (jarvisActive) {

                                    runOnUiThread(() ->
                                            voiceEngine
                                                    .resumeListening()
                                    );
                                }
                            }

                            @Override
                            public void onError(
                                    String utteranceId) {
                            }
                        });

                        jarvisActive = true;

                        reply(
                                "Hello sir, I am Jarvis."
                        );
                    }
                }
        );
    }

    private void initializeVoice() {

        voiceEngine =
                new VoiceEngine(
                        this,
                        new VoiceEngine.Listener() {

                    @Override
                    public void onListening() {

                        runOnUiThread(() -> {

                            status.setText(
                                    "🎤 LISTENING"
                            );
                        });
                    }

                    @Override
                    public void onResult(
                            String text) {

                        runOnUiThread(() -> {

                            status.setText(
                                    "🧠 PROCESSING"
                            );

                            output.setText(
                                    "You: " + text
                            );

                            handleCommand(text);
                        });
                    }

                    @Override
                    public void onError(
                            String error) {

                        runOnUiThread(() -> {

                            status.setText(
                                    "🎤 LISTENING"
                            );
                        });
                    }
                }
        );
    }

    private void requestMicrophone() {

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    MIC_CODE
            );

        } else {

            startJarvis();
        }
    }

    private void startJarvis() {

        jarvisActive = true;

        if (tts == null) {

            voiceEngine.start();

        } else {

            /*
             * VoiceEngine starts again automatically
             * after the welcome message finishes.
             */
        }
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

        if (requestCode == MIC_CODE &&
                results.length > 0 &&
                results[0] ==
                        PackageManager.PERMISSION_GRANTED) {

            startJarvis();

        } else {

            status.setText(
                    "Microphone permission required"
            );
        }
    }

    private void handleCommand(String command) {

        String text =
                command
                        .toLowerCase(Locale.ROOT)
                        .trim();

        if (text.contains("hello") ||
                text.contains("hi")) {

            reply(
                    "Hello sir, how can I help you?"
            );

        } else if (
                text.contains("your name") ||
                text.contains("who are you")
        ) {

            reply(
                    "My name is Jarvis."
            );

        } else if (
                text.contains("youtube")
        ) {

            openYouTube();

            reply(
                    "Opening YouTube, sir."
            );

        } else if (
                text.contains("google")
        ) {

            openGoogle();

            reply(
                    "Opening Google, sir."
            );

        } else if (
                text.contains("chrome")
        ) {

            openChrome();

            reply(
                    "Opening Chrome, sir."
            );

        } else if (
                text.contains("stop")
        ) {

            jarvisActive = false;

            voiceEngine.stop();

            reply(
                    "Okay sir, stopping."
            );

        } else {

            reply(
                    "I heard you say " +
                    command +
                    ", but I don't know that command yet."
            );
        }
    }

    private void openYouTube() {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    "com.google.android.youtube"
                            );

            if (intent != null) {

                startActivity(intent);

            } else {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.youtube.com"
                                )
                        )
                );
            }

        } catch (Exception e) {

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://www.youtube.com"
                            )
                    )
            );
        }
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

    private void openChrome() {

        try {

            Intent intent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    "com.android.chrome"
                            );

            if (intent != null) {
                startActivity(intent);
            } else {
                openGoogle();
            }

        } catch (Exception e) {
            openGoogle();
        }
    }

    private void reply(String message) {

        output.setText(
                "JARVIS: " + message
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

    @Override
    protected void onDestroy() {

        jarvisActive = false;

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
