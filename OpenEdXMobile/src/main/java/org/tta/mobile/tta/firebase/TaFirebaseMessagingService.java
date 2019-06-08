package org.tta.mobile.tta.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.DataManager;
import org.tta.mobile.tta.data.enums.NotificationType;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.ui.deep_link.DeepLinkActivity;
import org.tta.mobile.tta.ui.splash.SplashActivity;
import org.tta.mobile.tta.utils.NotificationUtil;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class TaFirebaseMessagingService extends FirebaseMessagingService {

    public static final String COURSE = "course";
    public static final String CONNECT = "connect";
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_CONNECT_URL = "connect_url";

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_ISPUSH = "isPush";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("onNewToken", "Refreshed token: " + s);

        // TODO: Implement this method to send any registration to your app's servers.
        Toast.makeText(getApplicationContext(),"Got firebase id",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("Manprax","Notification Message Body");
        //sendNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("body"),"","");

        if (remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("body") &&
                remoteMessage.getData().containsKey("path") && remoteMessage.getData().containsKey("type"))
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"),
                    remoteMessage.getData().get("path"), remoteMessage.getData().get("type"));
        else if(remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("body"))
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"),
                    "","");
        else
            return;
    }

    private void sendNotification(String messageTitle,String messageBody,String path,String type) {
        //**add this line**
        int requestID = (int) System.currentTimeMillis();

        Intent notificationIntent;

        //get Intent for notification landing page
        //remove this for generlisation && path!=null && !path.equals("")
        if(type!=null && !type.equals(""))
        {
            notificationIntent=getNavigationIntent(type,path);
        }
        else
        {
            notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        }

        if (loginPrefs.getUsername() != null) {
            Notification notification = new Notification();
            notification.setCreated_time(System.currentTimeMillis());
            notification.setDescription(messageBody);
            notification.setTitle(messageTitle);
            notification.setUsername(loginPrefs.getUsername());
            notification.setType(NotificationType.content.name());
            notification.setRef_id(path);

            DataManager dataManager = DataManager.getInstance(this);
            dataManager.addNotification(notification);

            notificationIntent.putExtra(Constants.EXTRA_NOTIFICATION, notification);
        }

        //**add this line**
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //**edit this line to put requestID as requestCode**
        PendingIntent contentIntent = PendingIntent.getActivity(this, requestID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int notificationId = 1;
        String channelId = "TheTeacherapp-Channel-01";
        String channelName = "TheTeacherapp Channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        new NotificationUtil(this, notificationId, channelId, channelName, channelName, importance)
                .setTitle(messageTitle)
                .setMessage(messageBody)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .show();

    }

    private Intent getNavigationIntent(String type,String path)
    {
        Intent navigationIntent=new Intent();

        //if user is not logged in navigate to splash screen
        //removed this check "|| (path==null && path.equals(""))"
        if(type==null || type.equals("") || loginPrefs==null || !loginPrefs.isLoggedIn())
        {
            navigationIntent = new Intent(getApplicationContext(), SplashActivity.class);
            //return here default intent for dashboard
            return navigationIntent;
        }

        if(type.equals(COURSE)|| type.equals(CONNECT))
        {
            if(path==null || path.equals(""))
                navigationIntent = new Intent(getApplicationContext(), SplashActivity.class);
            else
            {
                navigationIntent=new Intent(getApplicationContext(), DeepLinkActivity.class);
                navigationIntent.putExtra(EXTRA_PATH,path);
                navigationIntent.putExtra(EXTRA_TYPE,type);
                navigationIntent.putExtra(EXTRA_ISPUSH,true);
            }
        }
        else
        {
            navigationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        }
        return navigationIntent;
    }
}
