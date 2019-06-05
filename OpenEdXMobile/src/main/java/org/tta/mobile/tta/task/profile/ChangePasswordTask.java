package org.tta.mobile.tta.task.profile;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.profile.ChangePasswordResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class ChangePasswordTask extends Task<ChangePasswordResponse> {

    private Bundle parameters;

    @Inject
    private TaAPI taAPI;

    public ChangePasswordTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public ChangePasswordResponse call() throws Exception {
        return taAPI.changePassword(parameters).execute().body();
    }
}
