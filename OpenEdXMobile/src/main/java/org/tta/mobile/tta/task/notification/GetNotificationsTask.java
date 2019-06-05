package org.tta.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class GetNotificationsTask extends Task<List<Notification>> {

    private int take, skip;

    @Inject
    private TaAPI taAPI;

    public GetNotificationsTask(Context context, int take, int skip) {
        super(context);
        this.take = take;
        this.skip = skip;
    }

    @Override
    public List<Notification> call() throws Exception {
        return taAPI.getNotifications(take, skip).execute().body();
    }
}
