package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jarvis.assistant.voice.VoiceEngine;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private VoiceEngine voice;
    private TextToSpeech tts;
    private AppLauncher appLauncher;

    private JarvisFaceView face;
    private TextView status;
    private TextView output;

    private boolean running = false;
    private boolean ttsReady = false;

    private static final int MIC_PERMISSION = 100;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final ArrayList<Integer> avatarResources =
            new ArrayList<>();

    private int selectedAvatar = 0;

    private SharedPreferences preferences;

    private final Runnable longPressRunnable =
            this::showAvatarPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(
                "JARVIS_SETTINGS",
                MODE_PRIVATE
        );

        appLauncher = new AppLauncher(this);

        loadAvatarResources();
        buildUI();
        initializeVoice();
        initializeTTS();
        requestMicrophone();
    }

    // ---------------------------------------------------------
    // AVATAR RESOURCES
    // ---------------------------------------------------------

    private void loadAvatarResources() {

        avatarResources.clear();

        String[] names = {
                "jarvis_base_face",
                "jarvis_body_1",
                "jarvis_body_2",
                "jarvis_body_3",
                "jarvis_body_4",
                "jarvis_body_5"
        };

        for (String name : names) {

            int id = getResources().getIdentifier(
                    name,
                    "drawable",
                    getPackageName()
            );

            if (id != 0) {
                avatarResources.add(id);
            }
        }

        if (avatarResources.isEmpty()) {
            avatarResources.add(android.R.drawable.ic_menu_gallery);
        }

        selectedAvatar = preferences.getInt(
                "selected_avatar",
                0
        );

        if (selectedAvatar >= avatarResources.size()) {
            selectedAvatar = 0;
        }
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------

    private void buildUI() {

        LinearLayout root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setGravity(
                Gravity.CENTER
        );

        root.setPadding(
                20,
                30,
                20,
                20
        );

        root.setBackgroundColor(
                Color.BLACK
        );

        face = new JarvisFaceView(this);

        face.setAvatarResource(
                avatarResources.get(selectedAvatar)
        );

        LinearLayout.LayoutParams faceParams =
                new LinearLayout.LayoutParams(
                       -1,
                        0,
                        1f
                );

        faceParams.gravity =
                Gravity.CENTER;

        root.addView(
                face,
                faceParams
        );

        // 6-second long press
        face.setOnTouchListener(
                new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            handler.postDelayed(
                                    longPressRunnable,
                                    6000
                            );

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_UP ||
                                event.getAction() ==
                                        MotionEvent.ACTION_CANCEL) {

                            handler.removeCallbacks(
                                    longPressRunnable
                            );

                            return true;
                        }

                        return true;
                    }
                }
        );

        status = new TextView(this);

        status.setText(
                "JARVIS STARTING..."
        );

        status.setTextSize(20);

        status.setGravity(
                Gravity.CENTER
        );

        status.setTextColor(
                Color.WHITE
        );

        root.addView(
                status,
                new LinearLayout.LayoutParams(
                        -1,
                        55
                )
        );

        output = new TextView(this);

        output.setTextSize(17);

        output.setGravity(
                Gravity.CENTER
        );

        output.setTextColor(
                Color.LTGRAY
        );

        output.setMaxLines(3);

        root.addView(
                output,
                new LinearLayout.LayoutParams(
                        -1,
                        90
                )
        );

        setContentView(root);
    }

    // ---------------------------------------------------------
    // AVATAR PICKER
    // ---------------------------------------------------------

    private void showAvatarPicker() {

        handler.removeCallbacks(
                longPressRunnable
        );

        face.setState(
                JarvisFaceView.ACTION
        );

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                20,
                20,
                20,
                20
        );

        TextView title =
                new TextView(this);

        title.setText(
                "SELECT JARVIS AVATAR"
        );

        title.setTextSize(20);

        title.setTextColor(
                Color.WHITE
        );

        title.setGravity(
                Gravity.CENTER
        );

        container.addView(title);

        HorizontalScrollView scroll =
                new HorizontalScrollView(this);

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        for (int i = 0;
             i < avatarResources.size();
             i++) {

            final int index = i;

            ImageButton button =
                    new ImageButton(this);

            button.setImageResource(
                    avatarResources.get(i)
            );

            button.setScaleType(
                    android.widget.ImageView.ScaleType.CENTER_CROP
            );

            button.setBackgroundColor(
                    Color.TRANSPARENT
            );

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            180,
                            180
                    );

            params.setMargins(
                    10,
                    20,
                    10,
                    20
            );

            row.addView(
                    button,
                    params
            );

            button.setOnClickListener(
                    v -> {

                        selectedAvatar = index;

                        preferences.edit()
                                .putInt(
                                        "selected_avatar",
                                        index
                                )
                                .apply();

                        face.setAvatarResource(
                                avatarResources.get(index)
                        );

                        face.setState(
                                JarvisFaceView.IDLE
                        );

                        Toast.makeText(
                                this,
                                "Avatar changed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );
        }

        scroll.addView(row);

        container.addView(scroll);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setView(container)
                        .setNegativeButton(
                                "CLOSE",
                                null
                        )
                        .create();

        dialog.show();

        dialog.getWindow()
                .setDimAmount(0.85f);
    }

    // ---------------------------------------------------------
    // VOICE
    // ---------------------------------------------------------

    private void initializeVoice() {

        voice = new VoiceEngine(
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

                        if (!running) {
                            return;
                        }

                        runOnUiThread(() -> {

                            status.setText(
                                    "🎤 LISTENING"
                            );

                            face.setState(
                                    JarvisFaceView.LISTENING
                            );
                        });
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // TTS
    // ---------------------------------------------------------

    private void initializeTTS() {

        tts = new TextToSpeech(
                this,
                result -> {

                    if (result ==
                            TextToSpeech.SUCCESS) {

                        ttsReady = true;

                        tts.setSpeechRate(
                                0.95f
                        );

                        tts.setPitch(
                                1.0f
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

                                        if (!running) {
                                            return;
                                        }

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

                                    @Override
                                    public void onError(
                                            String id) {

                                        if (running) {
                                            runOnUiThread(() ->
                                                    voice.start()
                                            );
                                        }
                                    }
                                }
                        );

                        running = true;

                        speak(
                                "Hello sir, I am Jarvis."
                        );
                    }
                }
        );
    }

    // ---------------------------------------------------------
    // MICROPHONE
    // ---------------------------------------------------------

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

        if (ttsReady) {
            voice.start();
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

    // ---------------------------------------------------------
    // COMMAND PROCESSING
    // ---------------------------------------------------------

    private void handleCommand(
            String original) {

        String text =
                original
                        .toLowerCase(Locale.ROOT)
                        .trim();

        // HELLO
        if (text.contains("hello") ||
                text.contains("hi jarvis") ||
                text.equals("hi") ||
                text.contains("नमस्ते") ||
                text.contains("हेलो")) {

            speak(
                    "Hello sir, how can I help you?"
            );

            return;
        }

        // NAME
        if (text.contains("your name") ||
                text.contains("who are you") ||
                text.contains("tumhara naam") ||
                text.contains("आपका नाम")) {

            speak(
                    "I am Jarvis, your personal assistant."
            );

            return;
        }

        // STOP
        if (text.equals("stop") ||
                text.contains("stop listening") ||
                text.contains("रुक जाओ") ||
                text.contains("बंद हो जाओ")) {

            running = false;

            voice.stop();

            face.setState(
                    JarvisFaceView.IDLE
            );

            status.setText(
                    "JARVIS STOPPED"
            );

            return;
        }

        // YOUTUBE
        if (text.contains("youtube") ||
                text.contains("youtube par") ||
                text.contains("youtube pe")) {

            String query =
                    extractYouTubeQuery(text);

            openYouTube(query);

            speak(
                    "Opening YouTube, sir."
            );

            return;
        }

        // DIALER
        if (text.contains("call") ||
                text.contains("dial") ||
                text.contains("phone")) {

            String number =
                    extractPhoneNumber(original);

            if (!number.isEmpty()) {

                openDialer(number);

            } else {

                speak(
                        "Which number should I call, sir?"
                );
            }

            return;
        }

        // CAMERA
        if (text.contains("camera") ||
                text.contains("कैमरा")) {

            openCamera();

            return;
        }

        // SETTINGS
        if (text.contains("settings") ||
                text.contains("setting") ||
                text.contains("सेटिंग")) {

            openSettings();

            return;
        }

        // GOOGLE
        if (text.equals("open google") ||
                text.contains("google kholo") ||
                text.contains("google खोलो")) {

            openGoogle();

            speak(
                    "Opening Google, sir."
            );

            return;
        }

        // EXPLICIT OPEN COMMAND
        if (text.startsWith("open ")) {

            String appName =
                    original.substring(5).trim();

            if (!appName.isEmpty()) {

                openAppByName(appName);
            }

            return;
        }

        // COMMON APPS
        if (text.contains("whatsapp")) {

            openAppByName("WhatsApp");

            return;
        }

        if (text.contains("instagram")) {

            openAppByName("Instagram");

            return;
        }

        if (text.contains("snapchat")) {

            openAppByName("Snapchat");

            return;
        }

        if (text.contains("telegram")) {

            openAppByName("Telegram");

            return;
        }

        if (text.contains("gmail")) {

            openAppByName("Gmail");

            return;
        }

        if (text.contains("chrome")) {

            openAppByName("Chrome");

            return;
        }

        // SEARCH COMMAND
        if (text.startsWith("search ") ||
                text.contains("google par search") ||
                text.contains("search karo")) {

            String query =
                    text.replace(
                            "google par search",
                            ""
                    )
                    .replace(
                            "search karo",
                            ""
                    )
                    .replace(
                            "search",
                            ""
                    )
                    .trim();

            if (!query.isEmpty()) {

                openGoogleSearch(query);

                speak(
                        "Searching for " +
                                query +
                                ", sir."
                );

            } else {

                speak(
                        "What should I search for, sir?"
                );
            }

            return;
        }

        speak(
                "Sorry sir, I did not understand that command."
        );
    }

    // ---------------------------------------------------------
    // APP OPENING
    // ---------------------------------------------------------

    private void openAppByName(
            String appName) {

        face.setState(
                JarvisFaceView.ACTION
        );

        status.setText(
                "⚡ ACTION"
        );

        boolean opened =
                appLauncher.openApp(
                        appName
                );

        if (opened) {

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
    }

    // ---------------------------------------------------------
    // DIALER
    // ---------------------------------------------------------

    private void openDialer(
            String phoneNumber) {

        face.setState(
                JarvisFaceView.ACTION
        );

        status.setText(
                "⚡ CALL"
        );

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse(
                                    "tel:" +
                                            phoneNumber
                            )
                    );

            startActivity(intent);

            speak(
                    "Opening dialer, sir."
            );

        } catch (Exception e) {

            speak(
                    "Sorry sir, I could not open the dialer."
            );
        }
    }

    private String extractPhoneNumber(
            String text) {

        StringBuilder number =
                new StringBuilder();

        for (int i = 0;
             i < text.length();
             i++) {

            char c =
                    text.charAt(i);

            if (Character.isDigit(c) ||
                    (c == '+' &&
                            number.length() == 0)) {

                number.append(c);
            }
        }

        return number.toString();
    }

    // ---------------------------------------------------------
    // YOUTUBE
    // ---------------------------------------------------------

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
                "gाना",
                "chalao",
                "chala",
                "bajao",
                "baja do",
                "open",
                "please"
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
                    "https://www.youtube.com/results?search_query=" +
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

            openGoogleSearch(query);
        }
    }

    // ---------------------------------------------------------
    // GOOGLE
    // ---------------------------------------------------------

    private void openGoogleSearch(
            String query) {

        try {

            String url =
                    "https://www.google.com/search?q=" +
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

        try {

            startActivity(
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://www.google.com"
                            )
                    )
            );

        } catch (Exception e) {

            speak(
                    "Google could not be opened."
            );
        }
    }

    // ---------------------------------------------------------
    // CAMERA
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // SETTINGS
    // ---------------------------------------------------------

    private void openSettings() {

        try {

            startActivity(
                    new Intent(
                            Settings.ACTION_SETTINGS
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

    // ---------------------------------------------------------
    // SPEAK
    // ---------------------------------------------------------

    private void speak(
            String message) {

        output.setText(
                "JARVIS: " +
                        message
        );

        face.setState(
                JarvisFaceView.SPEAKING
        );

        status.setText(
                "🔊 SPEAKING"
        );

        if (tts == null ||
                !ttsReady) {

            return;
        }

        boolean hindi =
                message.matches(
                        ".*[\\u0900-\\u097F].*"
                );

        try {

            if (hindi) {

                tts.setLanguage(
                        new Locale(
                                "hi",
                                "IN"
                        )
                );

            } else {

                tts.setLanguage(
                        Locale.US
                );
            }

        } catch (Exception ignored) {}

        tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "JARVIS_" +
                        System.currentTimeMillis()
        );
    }

    // ---------------------------------------------------------
    // DESTROY
    // ---------------------------------------------------------

    @Override
    protected void onDestroy() {

        running = false;

        handler.removeCallbacksAndMessages(
                null
        );

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
