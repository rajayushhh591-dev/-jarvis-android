
package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView statusText;
    private ImageView jarvisFace;

    private VoiceEngine voiceEngine;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(android.graphics.Color.BLACK);
        layout.setPadding(25, 25, 25, 25);

        jarvisFace = new ImageView(this);
        jarvisFace.setImageResource(R.drawable.jarvis_base_face);
        jarvisFace.setAdjustViewBounds(true);

        LinearLayout.LayoutParams faceParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        layout.addView(jarvisFace, faceParams);

        statusText = new TextView(this);
        statusText.setText("Initializing JARVIS...");
        statusText.setTextColor(android.graphics.Color.CYAN);
        statusText.setTextSize(18);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(10, 20, 10, 20);

        layout.addView(statusText);

        setContentView(layout);

        startFaceIdleAnimation();

        voiceEngine = new VoiceEngine(this, new VoiceEngine.Callback() {

            @Override
            public void onListening() {
                showStatus("Listening...");
            }

            @Override
            public void onResult(String result) {
                handleCommand(result);
            }

            @Override
            public void onError(String error) {
                showStatus("Error: " + error);
            }
        });

        requestMicrophonePermission();

        textToSpeech = new TextToSpeech(this, status -> {

            if (status == TextToSpeech.SUCCESS) {

                textToSpeech.setLanguage(Locale.US);

                textToSpeech.setOnUtteranceProgressListener(
                        new UtteranceProgressListener() {

                            @Override
                            public void onStart(String utteranceId) {
                                runOnUiThread(() -> {
                                    startSpeakingAnimation();
                                });
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                runOnUiThread(() -> {
                                    stopSpeakingAnimation();
                                });
                            }

                            @Override
                            public void onError(String utteranceId) {
                                runOnUiThread(() -> {
                                    stopSpeakingAnimation();
                                });
                            }
                        }
                );

                startJarvis();

            } else {
                showStatus("Text-to-speech failed");
            }
        });
    }

    private void requestMicrophonePermission() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    100
            );
        }
    }

    private void startJarvis() {

        reply("Hello sir. JARVIS is online.");
    }

    private void startListening() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            showStatus("Microphone permission required");
            requestMicrophonePermission();
            return;
        }

        showStatus("Listening...");
        voiceEngine.startListening();
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
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                showStatus("Microphone permission granted");

            } else {
                showStatus("Microphone permission denied");
            }
        }
    }

    private void handleCommand(String command) {

        if (command == null || command.trim().isEmpty()) {
            reply("I did not hear anything, sir.");
            return;
        }

        String text = command.toLowerCase(Locale.ROOT).trim();

        if (text.contains("good morning")) {

            reply("Good morning, sir.");

        } else if (text.contains("hello")
                || text.contains("hi jarvis")
                || text.equals("hi")) {

            reply("Hello sir. How can I help you?");

        } else if (text.contains("how are you")) {

            reply("I am functioning perfectly, sir.");

        } else if (text.contains("youtube")) {

            reply("Opening YouTube.");
            openAppByName("youtube");

        } else if (text.contains("whatsapp")) {

            reply("Opening WhatsApp.");
            openAppByName("whatsapp");

        } else if (text.contains("camera")) {

            reply("Opening camera.");
            openCamera();

        } else if (text.contains("settings")) {

            reply("Opening settings.");

            try {
                Intent intent = new Intent(
                        android.provider.Settings.ACTION_SETTINGS
                );

                startActivity(intent);

            } catch (Exception e) {
                reply("Sorry sir, I cannot open settings.");
            }

        } else if (text.startsWith("open ")
                || text.startsWith("launch ")
                || text.startsWith("start ")) {

            String appName = text;

            if (text.startsWith("open ")) {
                appName = text.substring(5).trim();

            } else if (text.startsWith("launch ")) {
                appName = text.substring(7).trim();

            } else if (text.startsWith("start ")) {
                appName = text.substring(6).trim();
            }

            reply("Trying to open " + appName + ".");
            openAppByName(appName);

        } else if (text.contains("stop jarvis")
                || text.contains("close jarvis")
                || text.contains("exit jarvis")) {

            reply("Goodbye, sir.");

        } else {

            reply("Sorry sir, I do not understand that command yet.");
        }
    }

    private void openCamera() {

        try {

            Intent intent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE
            );

            startActivity(intent);

        } catch (Exception e) {
            reply("Camera is not available, sir.");
        }
    }

    private void openAppByName(String appName) {

        PackageManager packageManager = getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps =
                packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo app : apps) {

            String label = app.loadLabel(packageManager)
                    .toString()
                    .toLowerCase(Locale.ROOT);

            if (label.contains(appName.toLowerCase(Locale.ROOT))) {

                Intent launchIntent =
                        packageManager.getLaunchIntentForPackage(
                                app.activityInfo.packageName
                        );

                if (launchIntent != null) {
                    startActivity(launchIntent);
                    return;
                }
            }
        }

        reply("Sorry sir, I could not find that application.");
    }

    private void reply(String message) {

        showStatus(message);
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
                "JARVIS_" + System.currentTimeMillis()
        );
    }

    private void showStatus(String message) {

        runOnUiThread(() -> {

            if (statusText != null) {
                statusText.setText(message);
            }
        });
    }

    private void startFaceIdleAnimation() {

        AnimationSet animationSet = new AnimationSet(true);

        AlphaAnimation alpha =
                new AlphaAnimation(0.82f, 1.0f);

        alpha.setDuration(1400);

        ScaleAnimation scale = new ScaleAnimation(
                1.0f,
                1.02f,
                1.0f,
                1.02f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        scale.setDuration(1400);

        TranslateAnimation move = new TranslateAnimation(
                0,
                0,
                0,
                -4
        );

        move.setDuration(1400);

        animationSet.addAnimation(alpha);
        animationSet.addAnimation(scale);
        animationSet.addAnimation(move);

        animationSet.setRepeatMode(Animation.REVERSE);
        animationSet.setRepeatCount(Animation.INFINITE);

        jarvisFace.startAnimation(animationSet);
    }

    private void startSpeakingAnimation() {

        jarvisFace.clearAnimation();

        AnimationSet animationSet = new AnimationSet(true);

        AlphaAnimation alpha =
                new AlphaAnimation(0.85f, 1.0f);

        alpha.setDuration(300);

        ScaleAnimation scale = new ScaleAnimation(
                1.0f,
                1.045f,
                1.0f,
                1.045f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );

        scale.setDuration(300);

        TranslateAnimation move = new TranslateAnimation(
                0,
                0,
                0,
                -7
        );

        move.setDuration(300);

        animationSet.addAnimation(alpha);
        animationSet.addAnimation(scale);
        animationSet.addAnimation(move);

        animationSet.setRepeatMode(Animation.REVERSE);
        animationSet.setRepeatCount(Animation.INFINITE);

        jarvisFace.startAnimation(animationSet);
    }

    private void stopSpeakingAnimation() {

        jarvisFace.clearAnimation();

        startFaceIdleAnimation();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (voiceEngine != null) {
            voiceEngine.stopListening();
        }
    }
}
