package org.tta.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.CountResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class UpdateNotificationsTask extends Task<CountResponse> {

    private List<Long> notificationIds;

    @Inject
    private TaAPI taAPI;

    public UpdateNotificationsTask(Context context, List<Long> notificationIds) {
        super(context);
        this.notificationIds = notificationIds;
    }

    @Override
    public CountResponse call() throws Exception {
        return taAPI.updateNotifications(notificationIds).execute().body();
    }
}
