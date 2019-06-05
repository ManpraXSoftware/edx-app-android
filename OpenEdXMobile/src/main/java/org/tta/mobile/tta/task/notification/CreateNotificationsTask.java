package org.tta.mobile.tta.task.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.data.remote.api.TaAPI;

import java.util.List;

public class CreateNotificationsTask extends Task<List<Notification>> {

    private List<Notification> notifications;

    @Inject
    private TaAPI taAPI;

    public CreateNotificationsTask(Context context, List<Notification> notifications) {
        super(context);
        this.notifications = notifications;
    }

    @Override
    public List<Notification> call() throws Exception {
        return taAPI.createNotifications(notifications).execute().body();
    }
}
