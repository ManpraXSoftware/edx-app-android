package org.tta.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetUserContentStatusTask extends Task<List<ContentStatus>> {

    private List<Long> contentIds;

    @Inject
    private TaAPI taAPI;

    public GetUserContentStatusTask(Context context, List<Long> contentIds) {
        super(context);
        this.contentIds = contentIds;
    }

    @Override
    public List<ContentStatus> call() throws Exception {
        return taAPI.getUserContentStatus(contentIds).execute().body();
    }
}
