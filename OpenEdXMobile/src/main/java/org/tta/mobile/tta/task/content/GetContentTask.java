package org.tta.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetContentTask extends Task<Content> {

    private long contentId;

    @Inject
    private TaAPI taAPI;

    public GetContentTask(Context context, long contentId) {
        super(context);
        this.contentId = contentId;
    }

    @Override
    public Content call() throws Exception {
        return taAPI.getContent(contentId).execute().body();
    }
}
