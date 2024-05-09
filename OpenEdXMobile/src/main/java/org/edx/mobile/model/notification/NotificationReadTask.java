

package org.edx.mobile.model.notification;

        import android.content.Context;

        import com.google.inject.Inject;

        import org.edx.mobile.authentication.LoginAPI;
        import org.edx.mobile.programs.NotificationModel;
        import org.edx.mobile.programs.NotificationReadModel;
        import org.edx.mobile.task.Task;

public class NotificationReadTask  extends Task<NotificationReadModel> {

    @Inject
    private LoginAPI loginAPI;
    int id;


    public NotificationReadTask(Context context,int id) {
        super(context);
        this.id=id;
    }

    @Override
    public NotificationReadModel call() throws Exception {
        return loginAPI.getNotificationReadTask(id);
    }
}

