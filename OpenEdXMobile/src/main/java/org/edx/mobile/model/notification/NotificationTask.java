package org.edx.mobile.model.notification;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.programs.NotificationModel;
import org.edx.mobile.task.Task;

public class NotificationTask  extends Task<NotificationModel> {

    @Inject
    private LoginAPI loginAPI;

    int pageIndex;


    public NotificationTask(Context context,int pageIndex) {
        super(context);
        this.pageIndex=pageIndex;
    }

    @Override
    public NotificationModel call() throws Exception {
        return loginAPI.getNotificationTask(pageIndex);
    }
}
