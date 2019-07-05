package org.tta.mobile.tta.appupdate;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.user.Account;

public class InAppUpdateManager {

private Context ctx;
private Activity activity;
private AppUpdateManager appUpdateManager;
private int MY_REQUEST_CODE=314;
private // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask;

    public InAppUpdateManager(Context mCtx)
    {
        this.ctx=mCtx;
        this.appUpdateManager=AppUpdateManagerFactory.create(ctx);
        this.appUpdateInfoTask=appUpdateManager.getAppUpdateInfo();
    }

    private void getAppUpdate()
    {
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfoTask.getResult().updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
            }
        });
    }

    private void StartUpdate()
    {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfoTask.getResult(),
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    activity,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }


}
