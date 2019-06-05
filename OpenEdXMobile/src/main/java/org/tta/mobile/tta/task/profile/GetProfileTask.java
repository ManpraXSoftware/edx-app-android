package org.tta.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.task.Task;

public class GetProfileTask extends Task<ProfileModel> {

    @Inject
    private LoginAPI loginAPI;

    public GetProfileTask(Context context) {
        super(context);
    }

    @Override
    public ProfileModel call() throws Exception {
        return loginAPI.getProfile();
    }
}
