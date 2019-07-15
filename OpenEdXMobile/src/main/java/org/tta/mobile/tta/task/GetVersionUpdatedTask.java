package org.tta.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.http.callback.CallTrigger;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.UpdateResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetVersionUpdatedTask extends Task<UpdateResponse> {

    @Inject
    private TaAPI taAPI;
    String v_name;
    Long v_code;

    public GetVersionUpdatedTask(Context context,String version_name ,Long version_code) {
        super(context);
        this.v_name=version_name;
        this.v_code=version_code;
    }

    @Override
    public UpdateResponse call() throws Exception {
        return taAPI.getVersionUpdate(v_name,v_code).execute().body();
    }
}