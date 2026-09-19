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

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean active = false;
    private boolean processing = false;

    public VoiceEngine(Context context, Listener listener) {

        this.context = context;
        this.listener = listener;

        speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(context);

        recognizerIntent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                3
        );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                if (active) {
                    listener.onListening();
                }
            }

            @Override
            public void onResults(Bundle results) {

                if (!active) return;

                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                processing = false;

                if (matches != null && !matches.isEmpty()) {

                    String text = matches.get(0).trim();

                    if (!text.isEmpty()) {
                        listener.onResult(text);
                    }
                }
            }

            @Override
            public void onError(int error) {

                processing = false;

                if (!active) return;

                listener.onError(String.valueOf(error));

                restart();
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(
                    int eventType,
                    Bundle params) {}
        });
    }

    public void start() {

        active = true;
        processing = false;

        handler.post(() -> {

            try {
                speechRecognizer.startListening(
                        recognizerIntent
                );
            } catch (Exception e) {
                listener.onError(e.getMessage());
                restart();
            }
        });
    }

    private void restart() {

        if (!active) return;

        handler.postDelayed(() -> {

            if (!active) return;

            try {
                speechRecognizer.cancel();
                speechRecognizer.startListening(
                        recognizerIntent
                );
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }

        }, 800);
    }

    public void resumeListening() {

        if (!active) return;

        handler.postDelayed(() -> {

            if (!active) return;

            try {
                speechRecognizer.cancel();
                speechRecognizer.startListening(
                        recognizerIntent
                );
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }

        }, 700);
    }

    public void stop() {

        active = false;
        processing = false;

        handler.removeCallbacksAndMessages(null);

        try {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
        } catch (Exception ignored) {}
    }

    public void destroy() {

        active = false;

        handler.removeCallbacksAndMessages(null);

        try {
            speechRecognizer.destroy();
        } catch (Exception ignored) {}
    }
}
