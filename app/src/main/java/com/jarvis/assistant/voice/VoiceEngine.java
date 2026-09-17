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
    private boolean listening = false;
    private boolean restartScheduled = false;

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
        speaking = false;

        createRecognizer();
        startListening();
    }

    private void createRecognizer() {

        if (recognizer != null) {
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                listening = true;

                if (running && !speaking) {
                    listener.onListening();
                }
            }

            @Override
            public void onBeginningOfSpeech() {
                listening = true;
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                listening = false;
            }

            @Override
            public void onError(int error) {

                listening = false;

                if (!running || speaking) {
                    return;
                }

                // Normal "no match" / timeout errors ko screen par spam nahi karna.
                scheduleRestart(500);
            }

            @Override
            public void onResults(Bundle results) {

                listening = false;

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
                        listener.onResult(text);
                    }
                }

                scheduleRestart(500);
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

        if (!running || speaking || listening || recognizer == null) {
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
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    Locale.getDefault()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            // User ko naturally pause karne ka time.
            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2500
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2000
            );

            recognizer.startListening(intent);

        } catch (Exception e) {

            listening = false;
            scheduleRestart(1000);
        }
    }

    private void scheduleRestart(long delay) {

        if (!running || speaking || restartScheduled) {
            return;
        }

        restartScheduled = true;

        handler.postDelayed(() -> {

            restartScheduled = false;

            if (!running || speaking) {
                return;
            }

            startListening();

        }, delay);
    }

    public void setSpeaking(boolean value) {

        speaking = value;

        if (speaking) {

            handler.removeCallbacksAndMessages(null);
            restartScheduled = false;
            listening = false;

            if (recognizer != null) {
                try {
                    recognizer.cancel();
                } catch (Exception ignored) {
                }
            }

        } else {

            if (running) {
                scheduleRestart(400);
            }
        }
    }

    public void stop() {

        running = false;
        speaking = false;
        listening = false;
        restartScheduled = false;

        handler.removeCallbacksAndMessages(null);

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
}
