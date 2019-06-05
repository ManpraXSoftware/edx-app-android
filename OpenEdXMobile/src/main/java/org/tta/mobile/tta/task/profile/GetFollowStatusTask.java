package org.tta.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.profile.FollowStatus;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetFollowStatusTask extends Task<FollowStatus> {

    private String username;

    @Inject
    private TaAPI taAPI;

    public GetFollowStatusTask(Context context, String username) {
        super(context);
        this.username = username;
    }

    @Override
    public FollowStatus call() throws Exception {
        return taAPI.getFollowStatus(username).execute().body();
    }
}
