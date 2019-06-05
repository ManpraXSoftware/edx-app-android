package org.tta.mobile.tta.task.feed;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.feed.SuggestedUser;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetSuggestedUsersTask extends Task<List<SuggestedUser>> {

    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetSuggestedUsersTask(Context context, int take, int skip) {
        super(context);
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<SuggestedUser> call() throws Exception {
        return taAPI.getSuggestedUsers(take, skip).execute().body();
    }
}
