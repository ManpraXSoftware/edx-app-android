package org.tta.mobile.tta.task.profile;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.profile.UserAddressResponse;

public class GetUserAddressTask extends Task<UserAddressResponse> {

    private Bundle parameters;

    @Inject
    private LoginAPI loginAPI;

    public GetUserAddressTask(Context context, Bundle parameters) {
        super(context);
        this.parameters = parameters;
    }

    @Override
    public UserAddressResponse call() throws Exception {
        return loginAPI.getUserAddress(parameters);
    }
}
