package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.edx.mobile.R;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.programs.NotificationModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class NotificationActivity extends AppCompatActivity {
    static NotificationModel notificationModel;
    static String username;

    public static Intent newIntent(Context activity, NotificationModel notificationModelIntent,String usernameIntent) {
        final Intent intent = new Intent(activity, NotificationActivity.class);
        notificationModel=notificationModelIntent;
        username=usernameIntent;
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.notificationFragmentContainer, new NotificationFragment(notificationModel,username))
                .commit();
    }
}