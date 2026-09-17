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
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean running = false;
    private boolean speaking = false;

    public VoiceEngine(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError("Speech recognition available nahi hai.");
            return;
        }

        running = true;
        createRecognizer();
        startListening();
    }

    private void createRecognizer() {

        stopRecognizer();

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                if (running && !speaking) {
                    listener.onListening();
                }
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

                if (!running || speaking) {
                    return;
                }

                restartAfterDelay(700);
            }

            @Override
            public void onResults(Bundle results) {

                if (!running || speaking) {
                    return;
                }

                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (matches != null && !matches.isEmpty()) {

                    String text = matches.get(0).trim();

                    if (!text.isEmpty()) {

                        // Pehle result MainActivity ko denge.
                        listener.onResult(text);

                        // Phir nayi listening start hogi.
                        restartAfterDelay(1200);

                        return;
                    }
                }

                restartAfterDelay(700);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startListening() {

        if (!running || speaking || recognizer == null) {
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

            // Hindi + English ke liye phone ki default language use hogi.
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    Locale.getDefault()
            );

            // Speech complete hone ka wait.
            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            // Silence ke baad result finalize hoga.
            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1500
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1000
            );

            recognizer.startListening(intent);

        } catch (Exception e) {

            listener.onError("Listening start nahi ho payi.");

            restartAfterDelay(1000);
        }
    }

    private void restartAfterDelay(long delay) {

        handler.postDelayed(() -> {

            if (!running || speaking) {
                return;
            }

            createRecognizer();
            startListening();

        }, delay);
    }

    public void setSpeaking(boolean value) {

        speaking = value;

        if (speaking) {

            if (recognizer != null) {
                try {
                    recognizer.cancel();
                } catch (Exception ignored) {
                }
            }

        } else {

            restartAfterDelay(500);
        }
    }

    private void stopRecognizer() {

        if (recognizer != null) {

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
        speaking = false;

        handler.removeCallbacksAndMessages(null);

        stopRecognizer();
    }
}
