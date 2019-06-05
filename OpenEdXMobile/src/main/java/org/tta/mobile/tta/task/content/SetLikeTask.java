package org.tta.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.StatusResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class SetLikeTask extends Task<StatusResponse> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public SetLikeTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public StatusResponse call() throws Exception {
        return taAPI.setLike(contentId).execute().body();
    }
}
