package org.tta.mobile.tta.ui.landing;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.ActivityResult;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.landing.view_model.LandingViewModel;
import org.tta.mobile.tta.ui.search.SearchFragment;

public class LandingActivity extends BaseVMActivity{

    private static final int REQUEST_IN_APP_UPDATE = 414;

    private LandingViewModel viewModel;

    private LinearLayout layoutUpdate;
    private TextView textUpdate;
    private ProgressBar progressUpdate;

    public static boolean isAlreadyOpened;
    private AppPref mAppPref;
    AppUpdateManager appUpdateManager;
    private boolean isUpdateListenerRegistered = false;
    private int MY_REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LandingViewModel(this);
        binding(R.layout.t_activity_landing, viewModel);

        layoutUpdate = findViewById(R.id.layout_update);
        textUpdate = findViewById(R.id.text_update);
        progressUpdate = findViewById(R.id.progress_update);

        mAppPref = new AppPref(this);
        BottomNavigationView view = findViewById(R.id.dashboard_bottom_nav);
        view.setItemIconTintList(null);

//        appUpdateManager = AppUpdateManagerFactory.create(this);
//        checkForAppUpdates();

        viewModel.registerEventBus();

        checkInAppUpdate(true);
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

        checkInAppUpdate(false);

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
            Log.d("Update", "state of the app update : " + state);
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
                    Log.d("Update", "An update is available!!!");
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
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_CLASS_NAME, LandingActivity.class.getName());
                    parameters.putString(Constants.KEY_FUNCTION_NAME, "checkForAppUpdates");
                    Logger.logCrashlytics(e, parameters);
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
                Log.d("Update", "Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }

        } else if (requestCode == REQUEST_IN_APP_UPDATE) {
            if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                layoutUpdate.setVisibility(View.GONE);
                unRegisterUpdateListener();
                showIndefiniteSnack(getString(R.string.error_inapp_update));
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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


    //In app update start

    private InstallStateUpdatedListener updatedListener = state -> {

        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            showUpdateProgress(state.bytesDownloaded(), state.totalBytesToDownload());
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            layoutUpdate.setVisibility(View.GONE);
            requestInstallUpdate();
            unRegisterUpdateListener();
        }

    };

    private void checkInAppUpdate(boolean requestIfAvailable) {

        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(this);
        }

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                requestInstallUpdate();
            } if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADING) {
                if (!isUpdateListenerRegistered) {
                    appUpdateManager.registerListener(updatedListener);
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    requestIfAvailable) {
                requestFlexibleUpdate(appUpdateInfo);
            }
        });

    }

    private void requestFlexibleUpdate(AppUpdateInfo appUpdateInfo) {

        try {
            isUpdateListenerRegistered = true;
            appUpdateManager.registerListener(updatedListener);
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE,
                    this, REQUEST_IN_APP_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            unRegisterUpdateListener();

            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, LandingActivity.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "requestFlexibleUpdate");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }

    }

    private void requestInstallUpdate() {
        showIndefiniteSnack(getString(R.string.update_downloaded_restart),
                getString(R.string.restart), v -> appUpdateManager.completeUpdate());
    }

    private void unRegisterUpdateListener() {
        if (isUpdateListenerRegistered && appUpdateManager != null) {
            appUpdateManager.unregisterListener(updatedListener);
        }
        isUpdateListenerRegistered = false;
    }

    private void showUpdateProgress(long bytesDownloaded, long totalBytesToDownload) {
        long percent = bytesDownloaded * 100 / totalBytesToDownload;

        layoutUpdate.setVisibility(View.VISIBLE);
        textUpdate.setText(String.format(getString(R.string.downloading_update), percent));
        progressUpdate.setProgress((int) percent);
    }

    //In app update end

}
