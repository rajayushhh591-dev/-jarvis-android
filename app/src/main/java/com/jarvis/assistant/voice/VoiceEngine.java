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

    private final Listener listener;
    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private SpeechRecognizer recognizer;
    private Intent recognizerIntent;

    private boolean active = false;

    public VoiceEngine(
            Context context,
            Listener listener) {

        this.listener = listener;

        recognizer =
                SpeechRecognizer.createSpeechRecognizer(
                        context
                );

        recognizerIntent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        // Indian English / Hinglish
        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "en-IN"
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        recognizer.setRecognitionListener(
                new RecognitionListener() {

            @Override
            public void onReadyForSpeech(
                    Bundle params) {

                if (active) {
                    listener.onListening();
                }
            }

            @Override
            public void onResults(
                    Bundle results) {

                if (!active) return;

                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer
                                        .RESULTS_RECOGNITION
                        );

                if (matches != null &&
                        !matches.isEmpty()) {

                    String text =
                            matches.get(0).trim();

                    if (!text.isEmpty()) {

                        listener.onResult(text);

                    } else {

                        restart();
                    }

                } else {

                    restart();
                }
            }

            @Override
            public void onError(int error) {

                if (!active) return;

                listener.onError(
                        String.valueOf(error)
                );

                restart();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(
                    float rmsdB) {}

            @Override
            public void onBufferReceived(
                    byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onPartialResults(
                    Bundle partialResults) {}

            @Override
            public void onEvent(
                    int eventType,
                    Bundle params) {}
        });
    }

    public void start() {

        active = true;

        handler.post(() -> {

            try {

                recognizer.cancel();

                recognizer.startListening(
                        recognizerIntent
                );

            } catch (Exception e) {

                listener.onError(
                        e.getMessage()
                );
            }
        });
    }

    public void restart() {

        if (!active) return;

        handler.postDelayed(() -> {

            if (!active) return;

            try {

                recognizer.cancel();

                recognizer.startListening(
                        recognizerIntent
                );

            } catch (Exception ignored) {}

        }, 700);
    }

    public void stop() {

        active = false;

        handler.removeCallbacksAndMessages(
                null
        );

        try {

            recognizer.stopListening();
            recognizer.cancel();

        } catch (Exception ignored) {}
    }

    public void destroy() {

        active = false;

        handler.removeCallbacksAndMessages(
                null
        );

        try {

            recognizer.destroy();

        } catch (Exception ignored) {}
    }
}
