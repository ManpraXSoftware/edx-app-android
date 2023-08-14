package org.humana.mobile.exception;

import androidx.annotation.NonNull;

public class AuthException extends Exception {
    public AuthException(@NonNull String message) {
        super(message);
    }
}
