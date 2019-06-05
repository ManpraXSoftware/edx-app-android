package org.tta.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.StatusResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class SetLikeUsingSourceIdentityTask extends Task<StatusResponse> {

    private String sourceIdentity;

    @Inject
    private TaAPI taAPI;

    public SetLikeUsingSourceIdentityTask(Context context, String sourceIdentity) {
        super(context);
        this.sourceIdentity = sourceIdentity;
    }

    @Override
    public StatusResponse call() throws Exception {
        return taAPI.setLikeUsingSourceIdentity(sourceIdentity).execute().body();
    }

}
