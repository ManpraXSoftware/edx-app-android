package org.edx.mobile.Chatbot.TextToSpeechHelper;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import org.edx.mobile.interfaces.TalkBackListener;
import org.edx.mobile.util.LocaleManager;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechHelper extends UtteranceProgressListener {
    private TextToSpeech textToSpeech;

    public Activity activity;
    private AudioManager audioManager;

    TalkBackListener talkBackListener;
    public TextToSpeechHelper(Context context,Activity activity,TalkBackListener talkBackListener) {
        this.activity=activity;
        this.talkBackListener=talkBackListener;
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    String selectedLanguage = "en";
                    if (activity != null) {
                        if (!LocaleManager.getLanguagePref(activity).isEmpty()) {
                            selectedLanguage = LocaleManager.getLanguagePref(activity);
                        }
                    }
                    Locale locale = new Locale(selectedLanguage);
                    textToSpeech.setLanguage(locale);
                    int result = textToSpeech.setLanguage(locale);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "Text-to-speech not supported on your device", Toast.LENGTH_SHORT).show();
                    }
                    // textToSpeech.setOnUtteranceProgressListener(UtteranceProgressListener.this);
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener()   {
                        @Override
                        public void onStart(String utteranceId) {

                        }

                        @Override
                        public void onDone(String utteranceId) {
                            talkBackListener.onDoneTalkBackListener();
                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                } else {
                    Toast.makeText(context, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Use TextToSpeech.OnUtteranceCompletedListener here
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    public void speakText(String text) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId");
    }

    /*public void speakText(String text) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, text);
        //textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }*/

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    @Override
    public void onStart(String utteranceId) {

    }

    @Override
    public void onDone(String utteranceId) {

        talkBackListener.onDoneTalkBackListener();
    }

    @Override
    public void onError(String utteranceId) {

    }
}
