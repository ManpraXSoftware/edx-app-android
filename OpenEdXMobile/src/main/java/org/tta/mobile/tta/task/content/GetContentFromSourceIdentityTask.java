package org.tta.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetContentFromSourceIdentityTask extends Task<Content> {

    private String sourceIdentity;

    @Inject
    private TaAPI taAPI;

    public GetContentFromSourceIdentityTask(Context context, String sourceIdentity) {
        super(context);
        this.sourceIdentity = sourceIdentity;
    }

    @Override
    public Content call() throws Exception {
        return taAPI.getContentFromSourceIdentity(sourceIdentity).execute().body();
    }

}
