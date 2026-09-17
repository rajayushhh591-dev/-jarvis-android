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
    private boolean restarting = false;

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

        startNewRecognition();
    }

    private void startNewRecognition() {

        if (!running || speaking || restarting) {
            return;
        }

        restarting = true;

        handler.postDelayed(() -> {

            restarting = false;

            if (!running || speaking) {
                return;
            }

            createRecognizer();
            beginListening();

        }, 300);
    }

    private void createRecognizer() {

        destroyRecognizer();

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
                // User ne bolna start kar diya.
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Voice level available hai.
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                // User ruk gaya.
                // Android result process karega.
            }

            @Override
            public void onError(int error) {

                if (!running || speaking) {
                    return;
                }

                // Temporary recognition errors ko automatically recover karo.
                restartListening(500);
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

                        listener.onResult(text);

                        // Result process hone ke baad thoda pause.
                        restartListening(800);

                        return;
                    }
                }

                restartListening(400);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void beginListening() {

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

            /*
             * Indian English / Hinglish ke liye English-India.
             * Baad mein language detection aur better banayenge.
             */
            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    "en-IN"
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                    "en-IN"
            );

            /*
             * Partial results abhi off rakhe hain.
             * Pehle stable final-result system banayenge.
             */
            intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_MAX_RESULTS,
                    5
            );

            /*
             * User ke rukne ke baad result finalize.
             */
            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1800
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    1200
            );

            intent.putExtra(
                    RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    1000
            );

            recognizer.startListening(intent);

        } catch (Exception e) {

            listener.onError("Microphone listening start nahi ho payi.");

            restartListening(1000);
        }
    }

    private void restartListening(long delay) {

        if (!running || speaking) {
            return;
        }

        handler.postDelayed(() -> {

            if (!running || speaking) {
                return;
            }

            startNewRecognition();

        }, delay);
    }

    /*
     * JARVIS jab bolega tab listening temporarily stop hogi.
     */
    public void setSpeaking(boolean value) {

        speaking = value;

        if (speaking) {

            handler.removeCallbacksAndMessages(null);
            restarting = false;

            if (recognizer != null) {

                try {
                    recognizer.cancel();
                } catch (Exception ignored) {
                }
            }

        } else {

            if (running) {
                restartListening(300);
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
        restarting = false;

        handler.removeCallbacksAndMessages(null);

        destroyRecognizer();
    }
}
