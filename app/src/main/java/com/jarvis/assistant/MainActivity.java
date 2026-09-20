package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

    private boolean menuVisible = false;

    private static final int MIC_PERMISSION = 100;

    private static final int CALL_PERMISSION = 101;

    private final Handler handler =
            new Handler(
                    Looper.getMainLooper()
            );

    private final ArrayList<Integer>
            avatarResources =
            new ArrayList<>();

    private int selectedAvatar = 0;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        preferences =
                getSharedPreferences(
                        "JARVIS_SETTINGS",
                        MODE_PRIVATE
                );

        appLauncher =
                new AppLauncher(this);

        loadAvatarResources();

        buildUI();

        initializeVoice();

        initializeTTS();

        requestMicrophone();
    }

    // =========================================================
    // AVATAR
    // =========================================================

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

            int id =
                    getResources()
                            .getIdentifier(
                                    name,
                                    "drawable",
                                    getPackageName()
                            );

            if (id != 0) {

                avatarResources.add(id);
            }
        }

        if (avatarResources.isEmpty()) {

            avatarResources.add(
                    android.R.drawable
                            .ic_menu_gallery
            );
        }

        selectedAvatar =
                preferences.getInt(
                        "selected_avatar",
                        0
                );

        if (selectedAvatar < 0 ||
                selectedAvatar >=
                        avatarResources.size()) {

            selectedAvatar = 0;
        }
    }

    // =========================================================
    // UI
    // =========================================================

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

        face =
                new JarvisFaceView(this);

        face.setAvatarResource(
                avatarResources.get(
                        selectedAvatar
                )
        );

        LinearLayout.LayoutParams
                faceParams =
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1f
                );

        root.addView(
                face,
                faceParams
        );

        // -------------------------------------
        // FACE TAP
        // -------------------------------------

        face.setOnTouchListener(
                new View.OnTouchListener() {

                    private float downX;

                    private float downY;

                    @Override
                    public boolean onTouch(
                            View v,
                            MotionEvent event) {

                        if (event.getAction() ==
                                MotionEvent.ACTION_DOWN) {

                            downX =
                                    event.getX();

                            downY =
                                    event.getY();

                            return true;
                        }

                        if (event.getAction() ==
                                MotionEvent.ACTION_UP) {

                            float dx =
                                    Math.abs(
                                            event.getX()
                                                    - downX
                                    );

                            float dy =
                                    Math.abs(
                                            event.getY()
                                                    - downY
                                    );

                            if (dx < 30 &&
                                    dy < 30) {

                                toggleJarvisMenu();
                            }

                            return true;
                        }

                        return true;
                    }
                }
        );

        // -------------------------------------
        // STATUS
        // -------------------------------------

        status =
                new TextView(this);

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

        // -------------------------------------
        // OUTPUT
        // -------------------------------------

        output =
                new TextView(this);

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

    // =========================================================
    // JARVIS MENU
    // =========================================================

    private void toggleJarvisMenu() {

        if (menuVisible) {

            menuVisible = false;

            face.setState(
                    JarvisFaceView.IDLE
            );

            return;
        }

        menuVisible = true;

        face.setState(
                JarvisFaceView.ACTION
        );

        showJarvisMenu();
    }

    private void showJarvisMenu() {

        String[] items = {

                "Home",

                "Chat",

                "Voice",

                "Face Selector",

                "Settings",

                "About"
        };

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(
                                "JARVIS"
                        )
                        .setItems(
                                items,
                                (d, which) -> {

                                    menuVisible =
                                            false;

                                    switch (which) {

                                        case 0:

                                            showHome();

                                            break;

                                        case 1:

                                            showChat();

                                            break;

                                        case 2:

                                            startVoiceMode();

                                            break;

                                        case 3:

                                            showAvatarPicker();

                                            break;

                                        case 4:

                                            openSettings();

                                            break;

                                        case 5:

                                            showAbout();

                                            break;
                                    }
                                }
                        )
                        .create();

        dialog.setOnDismissListener(
                d -> {

                    menuVisible = false;

                    face.setState(
                            JarvisFaceView.IDLE
                    );
                }
        );

        dialog.show();
    }

    // =========================================================
    // FACE SELECTOR
    // =========================================================

    private void showAvatarPicker() {

        menuVisible = false;

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
                "SELECT JARVIS FACE"
        );

        title.setTextSize(21);

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

            LinearLayout card =
                    new LinearLayout(this);

            card.setOrientation(
                    LinearLayout.VERTICAL
            );

            card.setGravity(
                    Gravity.CENTER
            );

            ImageButton button =
                    new ImageButton(this);

            button.setImageResource(
                    avatarResources.get(i)
            );

            button.setScaleType(
                    android.widget.ImageView
                            .ScaleType.CENTER_CROP
            );

            button.setBackgroundColor(
                    Color.TRANSPARENT
            );

            LinearLayout.LayoutParams
                    imageParams =
                    new LinearLayout.LayoutParams(
                            180,
                            180
                    );

            button.setLayoutParams(
                    imageParams
            );

            TextView label =
                    new TextView(this);

            label.setText(
                    "Face " + (index + 1)
            );

            label.setTextColor(
                    Color.WHITE
            );

            label.setGravity(
                    Gravity.CENTER
            );

            card.addView(button);

            card.addView(label);

            LinearLayout.LayoutParams
                    cardParams =
                    new LinearLayout.LayoutParams(
                            200,
                            230
                    );

            cardParams.setMargins(
                    10,
                    10,
                    10,
                    10
            );

            row.addView(
                    card,
                    cardParams
            );

            button.setOnClickListener(
                    v -> {

                        selectedAvatar =
                                index;

                        preferences.edit()
                                .putInt(
                                        "selected_avatar",
                                        selectedAvatar
                                )
                                .apply();

                        face.setAvatarResource(
                                avatarResources.get(
                                        selectedAvatar
                                )
                        );

                        face.setState(
                                JarvisFaceView.IDLE
                        );

                        Toast.makeText(
                                this,
                                "Face " +
                                        (index + 1) +
                                        " selected",
                                Toast.LENGTH_SHORT
                        ).show();

                        showHome();
                    }
            );
        }

        // ADD MORE FACES

        LinearLayout addCard =
                new LinearLayout(this);

        addCard.setOrientation(
                LinearLayout.VERTICAL
        );

        addCard.setGravity(
                Gravity.CENTER
        );

        TextView addButton =
                new TextView(this);

        addButton.setText(
                "+\nAdd More Faces"
        );

        addButton.setTextSize(18);

        addButton.setTextColor(
                Color.WHITE
        );

        addButton.setGravity(
                Gravity.CENTER
        );

        addButton.setPadding(
                20,
                20,
                20,
                20
        );

        addCard.addView(
                addButton
        );

        LinearLayout.LayoutParams
                addParams =
                new LinearLayout.LayoutParams(
                        200,
                        180
                );

        addParams.setMargins(
                10,
                10,
                10,
                10
        );

        row.addView(
                addCard,
                addParams
        );

        addButton.setOnClickListener(
                v -> {

                    Toast.makeText(
                            this,
                            "Add your new face image to res/drawable, then add its name in loadAvatarResources().",
                            Toast.LENGTH_LONG
                    ).show();
                }
        );

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

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setDimAmount(0.85f);
        }
    }

    // =========================================================
    // HOME / CHAT / VOICE / ABOUT
    // =========================================================

    private void showHome() {

        output.setText(
                "JARVIS HOME"
        );

        status.setText(
                "READY"
        );

        face.setState(
                JarvisFaceView.IDLE
        );
    }

    private void showChat() {

        output.setText(
                "JARVIS CHAT"
        );

        status.setText(
                "CHAT MODE"
        );

        face.setState(
                JarvisFaceView.IDLE
        );
    }

    private void startVoiceMode() {

        running = true;

        status.setText(
                "🎤 LISTENING"
        );

        face.setState(
                JarvisFaceView.LISTENING
        );

        voice.start();
    }

    private void showAbout() {

        new AlertDialog.Builder(this)
                .setTitle(
                        "JARVIS"
                )
                .setMessage(
                        "JARVIS Personal Assistant\n\n" +
                        "Custom voice assistant with " +
                        "selectable JARVIS faces."
                )
                .setPositiveButton(
                        "CLOSE",
                        null
                )
                .show();
    }

    // =========================================================
    // VOICE
    // =========================================================

    private void initializeVoice() {

        voice =
                new VoiceEngine(
                        this,
                        new VoiceEngine.Listener() {

                            @Override
                            public void onListening() {

                                runOnUiThread(
                                        () -> {

                                            face.setState(
                                                    JarvisFaceView
                                                            .LISTENING
                                            );

                                            status.setText(
                                                    "🎤 LISTENING"
                                            );
                                        }
                                );
                            }

                            @Override
                            public void onResult(
                                    String text) {

                                runOnUiThread(
                                        () -> {

                                            face.setState(
                                                    JarvisFaceView
                                                            .THINKING
                                            );

                                            status.setText(
                                                    "🧠 PROCESSING"
                                            );

                                            output.setText(
                                                    "YOU: " +
                                                            text
                                            );

                                            handleCommand(
                                                    text
                                            );
                                        }
                                );
                            }

                            @Override
                            public void onError(
                                    String error) {

                                if (!running) {
                                    return;
                                }

                                runOnUiThread(
                                        () -> {

                                            status.setText(
                                                    "🎤 LISTENING"
                                            );

                                            face.setState(
                                                    JarvisFaceView
                                                            .LISTENING
                                            );
                                        }
                                );
                            }
                        }
                );
    }

    // =========================================================
    // TTS
    // =========================================================

    private void initializeTTS() {

        tts =
                new TextToSpeech(
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

                                                runOnUiThread(
                                                        () -> {

                                                            face.setState(
                                                                    JarvisFaceView
                                                                            .SPEAKING
                                                            );

                                                            status.setText(
                                                                    "🔊 SPEAKING"
                                                            );
                                                        }
                                                );
                                            }

                                            @Override
                                            public void onDone(
                                                    String id) {

                                                if (!running) {
                                                    return;
                                                }

                                                runOnUiThread(
                                                        () -> {

                                                            face.setState(
                                                                    JarvisFaceView
                                                                            .LISTENING
                                                            );

                                                            status.setText(
                                                                    "🎤 LISTENING"
                                                            );

                                                            voice.start();
                                                        }
                                                );
                                            }

                                            @Override
                                            public void onError(
                                                    String id) {

                                                if (running) {

                                                    runOnUiThread(
                                                            () ->
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

    // =========================================================
    // MICROPHONE
    // =========================================================

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
                            PackageManager
                                    .PERMISSION_GRANTED) {

                startJarvis();

            } else {

                status.setText(
                        "Microphone permission required"
                );
            }

            return;
        }

        if (requestCode ==
                CALL_PERMISSION) {

            if (results.length > 0 &&
                    results[0] ==
                            PackageManager
                                    .PERMISSION_GRANTED) {

                String number =
                        preferences.getString(
                                "pending_call_number",
                                ""
                        );

                if (!number.isEmpty()) {

                    preferences.edit()
                            .remove(
                                    "pending_call_number"
                            )
                            .apply();

                    makePhoneCall(number);
                }

            } else {

                speak(
                        "Phone permission was not allowed, sir."
                );
            }
        }
    }

    // =========================================================
    // COMMANDS
    // =========================================================

    private void handleCommand(
            String original) {

        if (original == null) {
            return;
        }

        String text =
                original
                        .toLowerCase(Locale.ROOT)
                        .trim();

        if (text.isEmpty()) {
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

        // HOW ARE YOU

        if (text.contains("how are you") ||
                text.contains("kaise ho") ||
                text.contains("कैसे हो")) {

            speak(
                    "I am doing great, sir. I am ready to help you."
            );

            return;
        }

        // WHAT ARE YOU DOING

        if (text.contains("what are you doing") ||
                text.contains("kya kar rahe ho") ||
                text.contains("क्या कर रहे हो")) {

            speak(
                    "I am listening and waiting for your command, sir."
            );

            return;
        }

        // CALL

        if (text.contains("call") ||
                text.contains("dial") ||
                text.contains("phone")) {

            String number =
                    extractPhoneNumber(
                            original
                    );

            if (!number.isEmpty()) {

                makePhoneCall(number);

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

        // YOUTUBE

        if (text.contains("youtube")) {

            String query =
                    extractYouTubeQuery(
                            text
                    );

            openYouTube(query);

            return;
        }

        // GOOGLE

        if (text.equals("open google") ||
                text.contains("google kholo") ||
                text.contains("google खोलो")) {

            openGoogle();

            return;
        }

        // EXPLICIT OPEN

        if (text.startsWith("open ")) {

            String appName =
                    original
                            .substring(5)
                            .trim();

            if (!appName.isEmpty()) {

                openAppByName(
                        appName
                );
            }

            return;
        }

        // COMMON APPS

        if (text.contains("whatsapp")) {

            openAppByName(
                    "WhatsApp"
            );

            return;
        }

        if (text.contains("instagram")) {

            openAppByName(
                    "Instagram"
            );

            return;
        }

        if (text.contains("snapchat")) {

            openAppByName(
                    "Snapchat"
            );

            return;
        }

        if (text.contains("telegram")) {

            openAppByName(
                    "Telegram"
            );

            return;
        }

        if (text.contains("gmail")) {

            openAppByName(
                    "Gmail"
            );

            return;
        }

        // UNKNOWN COMMAND

        speak(
                "I heard you, sir, but I do not have an action for that yet."
        );
    }

    // =========================================================
    // PHONE NUMBER
    // =========================================================

    private String extractPhoneNumber(
            String text) {

        if (text == null) {
            return "";
        }

        String number =
                text.replaceAll(
                        "[^0-9+]",
                        ""
                );

        if (number.startsWith("91") &&
                number.length() == 12) {

            number =
                    "+" + number;
        }

        if (number.startsWith("0") &&
                number.length() == 11) {

            number =
                    "+91" +
                            number.substring(1);
        }

        if (number.length() >= 10) {

            return number;
        }

        return "";
    }

    // =========================================================
    // DIRECT CALL
    // =========================================================

    private void makePhoneCall(
            String number) {

        if (number == null ||
                number.trim().isEmpty()) {

            speak(
                    "I could not understand the number, sir."
            );

            return;
        }

        if (checkSelfPermission(
                Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED) {

            preferences.edit()
                    .putString(
                            "pending_call_number",
                            number
                    )
                    .apply();

            requestPermissions(
                    new String[]{
                            Manifest.permission.CALL_PHONE
                    },
                    CALL_PERMISSION
            );

            return;
        }

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_CALL
                    );

            intent.setData(
                    Uri.parse(
                            "tel:" + number
                    )
            );

            startActivity(intent);

        } catch (Exception e) {

            speak(
                    "Sorry sir, I could not make the call."
            );
        }
    }

    // =========================================================
    // APP
    // =========================================================

    private void openAppByName(
            String appName) {

        boolean opened =
                appLauncher.openApp(
                        appName
                );

        if (opened) {

            status.setText(
                    "OPENED"
            );

            face.setState(
                    JarvisFaceView.ACTION
            );

        } else {

            speak(
                    "I could not find " +
                            appName +
                            " on this phone."
            );
        }
    }

    // =========================================================
    // CAMERA
    // =========================================================

    private void openCamera() {

        try {

            Intent intent =
                    new Intent(
                            "android.media.action.IMAGE_CAPTURE"
                    );

            startActivity(intent);

            speak(
                    "Opening camera, sir."
            );

        } catch (Exception e) {

            speak(
                    "I could not open the camera, sir."
            );
        }
    }

    // =========================================================
    // SETTINGS
    // =========================================================

    private void openSettings() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            startActivity(intent);

            speak(
                    "Opening settings, sir."
            );

        } catch (Exception e) {

            speak(
                    "I could not open settings."
            );
        }
    }

    // =========================================================
    // GOOGLE
    // =========================================================

    private void openGoogle() {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    "https://www.google.com"
                            )
                    );

            startActivity(intent);

            speak(
                    "Opening Google, sir."
            );

        } catch (Exception e) {

            speak(
                    "I could not open Google."
            );
        }
    }

    // =========================================================
    // YOUTUBE
    // =========================================================

    private String extractYouTubeQuery(
            String text) {

        if (text == null) {
            return "";
        }

        String query = text;

        query =
                query.replace(
                        "open youtube",
                        ""
                );

        query =
                query.replace(
                        "youtube par",
                        ""
                );

        query =
                query.replace(
                        "youtube pe",
                        ""
                );

        query =
                query.replace(
                        "youtube",
                        ""
                );

        query =
                query.trim();

        return query;
    }

    private void openYouTube(
            String query) {

        try {

            if (query == null ||
                    query.trim().isEmpty()) {

                Intent intent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.youtube.com"
                                )
                        );

                startActivity(intent);

            } else {

                String encoded =
                        URLEncoder.encode(
                                query,
                                "UTF-8"
                        );

                Intent intent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.youtube.com/results?search_query=" +
                                                encoded
                                )
                        );

                startActivity(intent);
            }

            speak(
                    "Opening YouTube, sir."
            );

        } catch (Exception e) {

            speak(
                    "I could not open YouTube."
            );
        }
    }

    // =========================================================
    // SPEAK
    // =========================================================

    private void speak(
            String text) {

        if (text == null ||
                text.trim().isEmpty()) {

            return;
        }

        output.setText(
                "JARVIS: " + text
        );

        if (!ttsReady) {

            status.setText(
                    "JARVIS"
            );

            return;
        }

        face.setState(
                JarvisFaceView.SPEAKING
        );

        status.setText(
                "🔊 SPEAKING"
        );

        String utteranceId =
                "JARVIS_" +
                        System.currentTimeMillis();

        tts.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
        );
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        running = false;

        if (voice != null) {

            voice.destroy();
        }

        if (tts != null) {

            tts.stop();

            tts.shutdown();
        }

        handler.removeCallbacksAndMessages(
                null
        );

        super.onDestroy();
    }
}
