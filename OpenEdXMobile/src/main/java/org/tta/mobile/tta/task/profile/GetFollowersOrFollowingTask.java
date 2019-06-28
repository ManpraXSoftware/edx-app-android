package org.tta.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.feed.SuggestedUser;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetFollowersOrFollowingTask extends Task<List<SuggestedUser>> {

    private int take, skip;
    private boolean follower;

    @Inject
    private TaAPI taAPI;

    public GetFollowersOrFollowingTask(Context context, boolean follower, int take, int skip) {
        super(context);
        this.follower = follower;
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<SuggestedUser> call() throws Exception {
        return taAPI.getFollowersOrFollowing(follower, take, skip).execute().body();
    }
}
