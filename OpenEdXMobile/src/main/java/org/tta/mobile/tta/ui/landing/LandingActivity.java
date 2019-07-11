package org.tta.mobile.tta.ui.landing;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.tta.mobile.R;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.landing.view_model.LandingViewModel;
import org.tta.mobile.tta.ui.search.SearchFragment;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import static com.crashlytics.android.Crashlytics.log;

public class LandingActivity extends BaseVMActivity{

    private LandingViewModel viewModel;

    public static boolean isAlreadyOpened;
    private AppPref mAppPref;
    AppUpdateManager appUpdateManager;
    private int MY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LandingViewModel(this);
        binding(R.layout.t_activity_landing, viewModel);

        mAppPref = new AppPref(this);
        BottomNavigationView view = findViewById(R.id.dashboard_bottom_nav);
        view.setItemIconTintList(null);

//        appUpdateManager = AppUpdateManagerFactory.create(this);
//        checkForAppUpdates();

        viewModel.registerEventBus();

    }

    @Override
    public void onBackPressed() {

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.TAG);
        if (searchFragment != null && searchFragment.isVisible()) {
            viewModel.selectLibrary();
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAlreadyOpened = true;
//        checkForAppUpdates();
//        appUpdateManager
//                .getAppUpdateInfo()
//                .addOnSuccessListener(appUpdateInfo -> {
//                    // If the update is downloaded but not installed,
//                    // notify the user to complete the update.
//                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
//                        popupSnackbarForCompleteUpdate();
//                    }
//                });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
        isAlreadyOpened = false;
        viewModel.getDataManager().onAppStartOrClose();
    }

    private void checkForAppUpdates() {
        InstallStateUpdatedListener listener = state -> {
            // Show module progress, log state, or install the update.
            log("state of the app update : " + state);
        };


// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateManager.registerListener(listener);
// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
//                 Request the update.

                try {
                    log("An update is available!!!");
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfoTask.getResult(),
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            this,
                            // Include a request code to later monitor this update request.
                            MY_REQUEST_CODE);
                    appUpdateManager.unregisterListener(listener);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_CODE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme);
            builder.setTitle("An Update is Available..");
            builder.setPositiveButton("Update", (dialog, which) -> {
                //Click button action
                startActivity(data);
                dialog.dismiss();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                //Cancel button action
            });

            builder.setCancelable(false);
            builder.show();

            if (resultCode != RESULT_OK) {
                log("Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }

        }
    }


    /* Displays the snackbar notification and call to action. */
    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.cl_main),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RESTART", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(R.color.colorPrimaryDark));
        snackbar.show();
    }

//    private void downloadUpdate(){
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().addNetworkInterceptor(
//        )
//    }

}
