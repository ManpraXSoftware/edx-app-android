package org.edx.mobile.clipboard;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardHelper implements ClipboardService {
    private ClipboardManager clipboardManager;

    public ClipboardHelper(Context context) {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void copyText(String text) {
        // Create a clip data object
        ClipData clipData = ClipData.newPlainText("Copied Text", text);

        // Copy the text to the clipboard
        clipboardManager.setPrimaryClip(clipData);
    }
}
