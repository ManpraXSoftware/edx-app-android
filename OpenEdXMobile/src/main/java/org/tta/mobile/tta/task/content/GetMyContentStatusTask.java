package org.tta.mobile.tta.task.content;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetMyContentStatusTask extends Task<List<ContentStatus>> {

    @Inject
    private TaAPI taAPI;

    public GetMyContentStatusTask(Context context) {
        super(context);
    }

    @Override
    public List<ContentStatus> call() throws Exception {
        return taAPI.getMyContentStatus().execute().body();
    }
}
