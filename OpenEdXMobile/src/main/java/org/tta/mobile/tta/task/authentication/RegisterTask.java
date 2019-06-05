package org.tta.mobile.tta.task.authentication;

import android.content.Context;
import android.os.Bundle;

import com.google.inject.Inject;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.social.SocialFactory;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.authentication.RegisterResponse;

public class RegisterTask extends Task<RegisterResponse> {
    private Bundle parameters;
    private SocialFactory.SOCIAL_SOURCE_TYPE backstoreType;
    private String accessToken;

    @Inject
    LoginAPI loginAPI;

    public RegisterTask(Context context, Bundle parameters, String accessToken, SocialFactory.SOCIAL_SOURCE_TYPE backstoreType) {
        super(context);
        this.parameters = parameters;
        this.accessToken = accessToken;
        this.backstoreType = backstoreType;
    }

    @Override
    public RegisterResponse call() throws Exception {
        return loginAPI.registerUsingMobileNumber(parameters);
    }
}
