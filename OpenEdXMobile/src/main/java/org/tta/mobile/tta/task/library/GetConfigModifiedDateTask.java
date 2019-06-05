package org.tta.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetConfigModifiedDateTask extends Task<ConfigModifiedDateResponse> {

    @Inject
    private TaAPI taAPI;

    public GetConfigModifiedDateTask(Context context) {
        super(context);
    }

    @Override
    public ConfigModifiedDateResponse call() throws Exception {
        return taAPI.getConfigModifiedDate().execute().body();
    }
}
