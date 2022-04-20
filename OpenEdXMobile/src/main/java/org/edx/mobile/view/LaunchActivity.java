package org.edx.mobile.view;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.BuildConfig;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.databinding.ActivityLaunchBinding;
import org.edx.mobile.module.analytics.Analytics;

public class LaunchActivity extends BaseFragmentActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // finally change the color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        final ActivityLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_launch);
        /*String osVersionText = String.format("%s %s", getString(R.string.android_os_version), android.os.Build.VERSION.RELEASE);
        String appVersionText = String.format("%s %s", getString(R.string.app_version), org.edx.mobile.BuildConfig.VERSION_NAME);
        String appVersionText1 = String.format("%s %s", getString(R.string.app_version), org.edx.mobile.BuildConfig.VERSION_CODE);*/
       // binding.signInTv.setText(appVersionText1);
        binding.signInTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(environment.getRouter().getLogInIntent());
            }
        });
/*        binding.signUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getAnalyticsRegistry().trackUserSignUpForAccount();
                startActivity(environment.getRouter().getRegisterIntent());
            }
        });*/
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LAUNCH_ACTIVITY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (environment.getLoginPrefs().getUsername() != null) {
            finish();
            environment.getRouter().showMainDashboard(this);
        }
    }
}