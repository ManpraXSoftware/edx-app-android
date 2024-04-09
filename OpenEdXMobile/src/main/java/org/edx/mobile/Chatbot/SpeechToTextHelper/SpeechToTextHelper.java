package org.edx.mobile.Chatbot.SpeechToTextHelper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import org.edx.mobile.Chatbot.SpeechToTextHelper.SpeechToTextListener;
import org.edx.mobile.R;
import org.edx.mobile.util.LocaleManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;

import static android.app.Activity.RESULT_OK;

public class SpeechToTextHelper implements RecognitionListener {

    private Activity activity;
    private SpeechRecognizer speechRecognizer;
    private SpeechToTextListener speechToTextListener;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;

    private boolean isSpeechStarted = false;
    private Handler handler = new Handler();
    //private Runnable stopListeningRunnable, restartListeningRunnable;


    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;


    public SpeechToTextHelper(Context context, SpeechToTextListener listener, Activity activity) {
        this.activity = activity;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechToTextListener = listener;
        speechRecognizer.setRecognitionListener(this);
      //  soundId = SoundPool().load(this, R.raw.beep_sound_2, 1);;
        mediaPlayer = MediaPlayer.create(activity, R.raw.beep_sound_2);
        //initializeRunnables();
    }



    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String recognizedText = matches.get(0);

            speechToTextListener.onSpeechResult(recognizedText);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String recognizedText = result.get(0);
                    // Pass the recognized text to your SpeechToTextHelper's onSpeechResult() method

                    speechToTextListener.onSpeechResult(recognizedText);
                }
            }
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onReadyForSpeech(Bundle params) {

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
        String errorMessage = getErrorText(error);
        speechToTextListener.onSpeechError(errorMessage);
    }

    private void startTimer(Runnable runnable, long delayMillis) {

    }



    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public void startSpeechRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            Toast.makeText(activity, "Speech recognition is not supported on this device", Toast.LENGTH_LONG).show();
            return; // Exit the function if speech recognition is unavailable
        }

        // Check microphone permission
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_SPEECH_INPUT);
            return; // Exit the function if permission is not granted yet
        }

        // Determine supported languages
        String selectedLanguage = LocaleManager.getLanguagePref(activity).isEmpty() ? "en" : LocaleManager.getLanguagePref(activity);

        // Check if selected language is supported
        List<Locale> supportedLocales = Arrays.asList(Locale.getAvailableLocales());
        if (!supportedLocales.contains(new Locale(selectedLanguage))) {
            Toast.makeText(activity, String.format("Speech recognition not supported for language: %s", selectedLanguage), Toast.LENGTH_LONG).show();
            return; // Consider exiting or offering alternative language options
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage);
        speechRecognizer.startListening(intent);
    }



    public void restartListeningAfterDelay() {
        if (!isSpeechStarted) {
            startSpeechRecognition();
        }
    }

    private void playBeepSoundAndVibrator(Activity activity) {
        // Get instance of Vibrator from the current context
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate with a soft pattern
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate pattern: {duration of vibration, duration of pause, duration of vibration, duration of pause, ...}
            long[] pattern = {50, 100, 50, 100}; // Soft vibration pattern: vibrate for 50ms, pause for 100ms, repeat
            vibrator.vibrate(pattern, -1); // -1 means don't repeat the pattern
        }
        if (mediaPlayer != null) {
          //  mediaPlayer.start();
        }
    }


    public void stopSpeechRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
        }
    }
}
