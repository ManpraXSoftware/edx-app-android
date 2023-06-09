package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.deeplink.BranchLinkManager;
import org.edx.mobile.deeplink.PushLinkManager;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;

import androidx.annotation.NonNull;
import io.branch.referral.Branch;

// We are extending the normal Activity class here so that we can use Theme.NoDisplay, which does not support AppCompat activities
public class SplashActivity extends Activity {
    protected final Logger logger = new Logger(getClass().getName());
    private Config config = new Config(MainApplication.instance());

    @NonNull
    private com.google.firebase.analytics.FirebaseAnalytics tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Recommended solution to avoid opening of multiple tasks of our app's launcher activity.
        For more info:
        - https://issuetracker.google.com/issues/36907463
        - https://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ/
        - https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508#16447508
         */
       // tracker.getInstance(this).setAnalyticsCollectionEnabled(true);

        //if (BuildConfig.DEBUG) {
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
           /* tracker.getInstance(this).setSessionTimeoutDuration(1000);
            tracker.getInstance(this).setMinimumSessionDuration(500);*/
            Log.d("MainActivity", "Firebase Analytics Debug Mode Enabled");
        //}
        //tracker.getInstance(this).setAnalyticsCollectionEnabled(true);

     /*   FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build();
        FirebaseRemoteConfig.getInstance().setConfigSettingsAsync(configSettings);
        FirebaseRemoteConfig.getInstance().fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Debug mode enabled
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
            } else {
                // Debug mode not enabled
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false);
            }
        });
*/
        if (!isTaskRoot()) {
            final Intent intent = getIntent();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
                return;
            }
        }

        final IEdxEnvironment environment = MainApplication.getEnvironment(this);
        if (environment.getUserPrefs().getProfile() != null) {
            environment.getRouter().showMainDashboard(SplashActivity.this);
        } else if (!environment.getConfig().isRegistrationEnabled()) {
            startActivity(environment.getRouter().getLogInIntent());
        } else {
            environment.getRouter().showLaunchScreen(SplashActivity.this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Checking push notification case in onStart() to make sure it will call in all cases
        // when this launcher activity will be started. For more details study onCreate() function.
        PushLinkManager.INSTANCE.checkAndReactIfFCMNotificationReceived(this, getIntent().getExtras());
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            Branch.getInstance().initSession((referringParams, error) -> {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user
                    // clicked -> was re-directed to this app params will be empty if no data found
                    if (referringParams.optBoolean(BranchLinkManager.KEY_CLICKED_BRANCH_LINK)) {
                        try {
                            BranchLinkManager.INSTANCE.checkAndReactIfReceivedLink(this, referringParams);
                        } catch (Exception e) {
                            logger.error(e, true);
                        }
                    }
                } else {
                    // Ignore the logging of errors occurred due to lack of network connectivity
                    if (NetworkUtil.isConnected(getApplicationContext())) {
                        logger.error(new Exception("Branch not configured properly, error:\n"
                                + error.getMessage()), true);
                    }
                }
            }, this.getIntent().getData(), this);
        }
        finish();
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
