package org.tta.mobile.tta.firebase;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.task.Task;

public class FirebaseTokenUpdateTask extends Task<FirebaseUpdateTokenResponse> {

    @NonNull
    private Bundle parameters;

    @Inject
    private LoginAPI loginAPI;

    public FirebaseTokenUpdateTask(@NonNull Context context, @NonNull Bundle mxparameters) {
        super(context);
        this.parameters = mxparameters;
    }

    @Override
    @NonNull
    public FirebaseUpdateTokenResponse call() throws Exception {
        return loginAPI.updateFireBaseTokenToServer(parameters);
    }

}
