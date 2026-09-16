package com.jarvis.assistant.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    public VoiceEngine(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Speech recognition available nahi hai.");
            return;
        }

        if (recognizer != null) {
            recognizer.destroy();
        }

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
                } else {
                    listener.onError("Voice recognition error: " + error);
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
                true
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        recognizer.startListening(intent);
    }

    private void restartListening() {

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).postDelayed(() -> {

            if (running) {
                startRecognition();
            }

        }, 300);
    }

    public void stop() {

        running = false;

        if (recognizer != null) {
            recognizer.stopListening();
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
    }
}
