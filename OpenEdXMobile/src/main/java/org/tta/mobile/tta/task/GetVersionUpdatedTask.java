package org.tta.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.http.callback.CallTrigger;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.UpdatedVersionResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetVersionUpdatedTask extends Task<UpdatedVersionResponse> {

    @Inject
    private TaAPI taAPI;

    public GetVersionUpdatedTask(Context context) {
        super(context);
    }

    public GetVersionUpdatedTask(Context context, CallTrigger callTrigger) {
        super(context, callTrigger);
    }

    @Override
    public UpdatedVersionResponse call() throws Exception {
        return null;
    }

//    @Override
//    public GetVersionUpdatedTask call() throws Exception {
//        return
//    }
//
//    @Override
//    public ProfileModel call() throws Exception {
//        return loginAPI.getProfile();
//    }
}