package com.jarvis.assistant.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceEngine {

    public interface Listener {
        void onListening();
        void onResult(String text);
        void onError(String error);
    }

    private final Context context;
    private final Listener listener;

    private SpeechRecognizer recognizer;
    private boolean running = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public VoiceEngine(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Speech recognition available nahi hai.");
            return;
        }

        stopRecognizerOnly();

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                listener.onListening();
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                if (running) {
                    restartListening();
                }
            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (matches != null && !matches.isEmpty()) {

                    String text = matches.get(0).trim();

                    if (!text.isEmpty()) {
                        listener.onResult(text);
                    }
                }

                // Result ke turant baad restart nahi karna.
                // Thoda delay rakhenge.
                if (running) {
                    restartListening();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        running = true;
        startRecognition();
    }

    private void startRecognition() {

        if (!running || recognizer == null) {
            return;
        }

        try {

            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    3
            );

            recognizer.startListening(intent);

        } catch (Exception e) {
            listener.onError("Voice start nahi ho payi.");
        }
    }

    private void restartListening() {

        handler.postDelayed(() -> {

            if (running && recognizer != null) {
                startRecognition();
            }

        }, 1000);
    }

    private void stopRecognizerOnly() {

        if (recognizer != null) {
            try {
                recognizer.stopListening();
            } catch (Exception ignored) {
            }

            try {
                recognizer.cancel();
            } catch (Exception ignored) {
            }

            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }

            recognizer = null;
        }
    }

    public void stop() {

        running = false;

        handler.removeCallbacksAndMessages(null);

        stopRecognizerOnly();
    }
}
