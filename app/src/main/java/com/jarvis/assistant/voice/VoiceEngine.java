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
            listener.onError("Speech recognition service available nahi hai.");
            return;
        }

        running = true;
        startRecognition();
    }

    private void startRecognition() {

        if (!running || speaking) {
            return;
        }

        destroyRecognizer();

        try {

            recognizer =
                    SpeechRecognizer.createSpeechRecognizer(context);

            recognizer.setRecognitionListener(
                    new RecognitionListener() {

                        @Override
                        public void onReadyForSpeech(Bundle params) {

                            if (running && !speaking) {
                                listener.onListening();
                            }
                        }

                        @Override
                        public void onBeginningOfSpeech() {

                            listener.onListening();
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

                            listener.onError(
                                    "Recognition error code: " + error
                            );

                            restart(1000);
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

                            if (matches != null
                                    && !matches.isEmpty()) {

                                String text =
                                        matches.get(0).trim();

                                if (!text.isEmpty()) {

                                    listener.onResult(text);

                                    restart(800);
                                    return;
                                }
                            }

                            listener.onError(
                                    "Speech detect hui, lekin result empty hai."
                            );

                            restart(500);
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

            Intent intent = new Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            );

            /*
             * Indian English.
             * Hinglish testing ke liye pehle ye use kar rahe hain.
             */
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "en-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            /*
             * User ke rukne ke baad recognition complete.
             */
            intent.putExtra(
                    RecognizerIntent
                            .EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1800
            );

            intent.putExtra(
                    RecognizerIntent
                            .EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    2500
            );

            intent.putExtra(
                    RecognizerIntent
                            .EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    1000
            );

            recognizer.startListening(intent);

        } catch (Exception e) {

            listener.onError(
                    "Recognizer start error: " + e.getMessage()
            );

            restart(1500);
        }
    }

    private void restart(long delay) {

        handler.postDelayed(() -> {

            if (running && !speaking) {
                startRecognition();
            }

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

            if (running) {
                restart(500);
            }
        }
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
    }

    public void stop() {

        running = false;
        speaking = false;

        handler.removeCallbacksAndMessages(null);

        destroyRecognizer();
    }
}
