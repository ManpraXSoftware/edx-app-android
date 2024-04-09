package org.edx.mobile.Chatbot.SpeechToTextHelper;

public interface SpeechToTextListener {
    void onSpeechResult(String text); // Called when a speech result is available
    void onSpeechError(String error); // Called when an error occurs
}
