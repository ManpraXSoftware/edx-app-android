package org.tta.mobile.view.common;

import androidx.annotation.NonNull;

public interface TaskMessageCallback {
    void onMessage(@NonNull MessageType messageType, @NonNull String message);
}
