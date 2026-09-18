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
    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean running = false;
    private boolean speaking = false;
    private boolean listening = false;

    public VoiceEngine(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void start() {

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError(
                    "Speech recognition service available nahi hai."
            );
            return;
        }

        running = true;
        speaking = false;

        createRecognizer();
        startListening();
    }

    private void createRecognizer() {

        if (recognizer != null) {
            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }
        }

        recognizer =
                SpeechRecognizer.createSpeechRecognizer(context);

        recognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(
                            Bundle params) {

                        if (!running || speaking) {
                            return;
                        }

                        listening = true;
                        listener.onListening();
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        listening = true;
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer) {
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

                        listener.onError(
                                "Recognition error: " + error
                        );

                        restart(800);
                    }

                    @Override
                    public void onResults(
                            Bundle results) {

                        listening = false;

                        if (!running || speaking) {
                            return;
                        }

                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null
                                && !matches.isEmpty()) {

                            String text =
                                    matches.get(0);

                            if (text != null) {
                                text = text.trim();
                            }

                            if (text != null
                                    && !text.isEmpty()) {

                                listener.onResult(text);
                            }
                        }

                        restart(500);
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {

                        if (!running || speaking) {
                            return;
                        }

                        ArrayList<String> matches =
                                partialResults
                                        .getStringArrayList(
                                                SpeechRecognizer
                                                        .RESULTS_RECOGNITION
                                        );

                        if (matches != null
                                && !matches.isEmpty()) {

                            String text =
                                    matches.get(0);

                            if (text != null
                                    && !text.trim().isEmpty()) {

                                // Partial result intentionally
                                // not sent as final command.
                            }
                        }
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );
    }

    private void startListening() {

        if (!running
                || speaking
                || listening
                || recognizer == null) {
            return;
        }

        try {

            Intent intent =
                    new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                    );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "en-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    true
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    context.getPackageName()
            );

            recognizer.startListening(intent);

        } catch (Exception e) {

            listening = false;

            listener.onError(
                    "Start listening error: "
                            + e.getClass().getSimpleName()
            );

            restart(1000);
        }
    }

    private void restart(long delay) {

        handler.postDelayed(
                () -> {

                    if (!running || speaking) {
                        return;
                    }

                    listening = false;

                    if (recognizer != null) {
                        try {
                            recognizer.cancel();
                        } catch (Exception ignored) {
                        }
                    }

                    startListening();

                },
                delay
        );
    }

    public void setSpeaking(boolean value) {

        speaking = value;

        if (speaking) {

            handler.removeCallbacksAndMessages(null);
            listening = false;

            if (recognizer != null) {
                try {
                    recognizer.cancel();
                } catch (Exception ignored) {
                }
            }

        } else {

            if (running) {
                restart(400);
            }
        }
    }

    public void stop() {

        running = false;
        speaking = false;
        listening = false;

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
