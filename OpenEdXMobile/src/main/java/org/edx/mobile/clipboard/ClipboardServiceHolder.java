package org.edx.mobile.clipboard;

import android.content.Context;

public class ClipboardServiceHolder {
    private static ClipboardService clipboardService;

    public static ClipboardService getClipboardService(Context context) {
        if (clipboardService == null) {
            clipboardService = new ClipboardHelper(context);
        }
        return clipboardService;
    }
}
