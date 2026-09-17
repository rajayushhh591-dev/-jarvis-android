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

public class VoiceEngine {

    public interface Listener {
        void onListening();
        void onResult(String text);
        void onError(String error);
    }

    private final Context context;
    private final Listener listener;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private SpeechRecognizer recognizer;

    private boolean running = false;
    private boolean speaking = false;
    private boolean listening = false;
    private boolean restartScheduled = false;

    public VoiceEngine(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start() {

        handler.post(() -> {

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                listener.onError(
                        "Speech recognition available nahi hai."
                );
                return;
            }

            running = true;
            speaking = false;

            startRecognition();
        });
    }

    private void startRecognition() {

        if (!running || speaking || listening) {
            return;
        }

        restartScheduled = false;

        destroyRecognizer();

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

                        if (!running || speaking) {
                            return;
                        }

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

                        scheduleRestart(700);
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

                        String text = null;

                        if (matches != null) {

                            for (String item : matches) {

                                if (item != null
                                        && !item.trim().isEmpty()) {

                                    text = item.trim();
                                    break;
                                }
                            }
                        }

                        if (text != null
                                && !text.isEmpty()) {

                            listener.onResult(text);

                        } else {

                            scheduleRestart(500);
                        }
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );

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
                    RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    context.getPackageName()
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            intent.putExtra(
                    RecognizerIntent
                            .EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2500
            );

            intent.putExtra(
                    RecognizerIntent
                            .EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2000
            );

            recognizer.startListening(intent);

        } catch (Exception e) {

            listening = false;
            scheduleRestart(1000);
        }
    }

    private void scheduleRestart(long delay) {

        if (!running
                || speaking
                || restartScheduled) {
            return;
        }

        restartScheduled = true;

        handler.postDelayed(() -> {

            restartScheduled = false;

            if (!running || speaking) {
                return;
            }

            startRecognition();

        }, delay);
    }

    public void setSpeaking(boolean value) {

        handler.post(() -> {

            speaking = value;

            if (speaking) {

                restartScheduled = false;

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

                    listening = false;
                    scheduleRestart(500);
                }
            }
        });
    }

    private void destroyRecognizer() {

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

        listening = false;
    }

    public void stop() {

        handler.post(() -> {

            running = false;
            speaking = false;
            listening = false;
            restartScheduled = false;

            handler.removeCallbacksAndMessages(null);

            destroyRecognizer();
        });
    }
}
