
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
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;

    public VoiceEngine(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        recognizerIntent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        speechRecognizer.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        listener.onListening();
                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> matches =
                                results.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (matches != null && !matches.isEmpty()) {
                            listener.onResult(matches.get(0));
                        }
                    }

                    @Override
                    public void onError(int error) {
                        listener.onError(String.valueOf(error));
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
                    public void onEvent(int eventType, Bundle params) {}
                }
        );
    }

    public void start() {
        speechRecognizer.startListening(recognizerIntent);
    }

    public void stop() {
        speechRecognizer.stopListening();
    }

    public void destroy() {
        speechRecognizer.destroy();
    }
}
