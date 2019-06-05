package org.tta.mobile.tta.task.library;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetCollectionConfigTask extends Task<CollectionConfigResponse> {

    @Inject
    private TaAPI taAPI;

    public GetCollectionConfigTask(Context context) {
        super(context);
    }

    @Override
    public CollectionConfigResponse call() throws Exception {
        return taAPI.getCollectionConfig().execute().body();
    }
}
