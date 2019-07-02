package org.tta.mobile.tta.task.feed;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetFeedsTask extends Task<List<Feed>> {

    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetFeedsTask(Context context, int skip) {
        super(context);
//        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Feed> call() throws Exception {
        return taAPI.getFeeds(skip).execute().body();
    }
}
