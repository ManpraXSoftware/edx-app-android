package org.edx.mobile.view;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.clipboard.ClipboardService;
import org.edx.mobile.clipboard.ClipboardServiceHolder;
import org.edx.mobile.databinding.ActivityLaunchBinding;
import org.edx.mobile.module.analytics.Analytics;

public class LaunchActivity extends BaseFragmentActivity {
    ClipboardService clipboardService;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clipboardService = ClipboardServiceHolder.getClipboardService(getApplicationContext());
        // finally change the color
        com.google.firebase.analytics.FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        final ActivityLaunchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_launch);
       /* String osVersionText = String.format("%s %s", getString(R.string.android_os_version), android.os.Build.VERSION.RELEASE);
        String appVersionText = String.format("%s %s", getString(R.string.app_version), org.edx.mobile.BuildConfig.VERSION_NAME);
        String appVersionText1 = String.format("%s %s", getString(R.string.app_version), org.edx.mobile.BuildConfig.VERSION_CODE);
        binding.signInTv.setText(appVersionText1);*/
        binding.signInTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAnalyticsCourseDetail();
                startActivity(environment.getRouter().getLogInIntent());
            }
        });
        /*binding.signInTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.signInTv.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        binding.yourAccessibleLearningPlatform.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String textToCopy = binding.yourAccessibleLearningPlatform.getText().toString();
                clipboardService.copyText(textToCopy);
                Toast.makeText(getApplicationContext(), getString(R.string.text_copied), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
/*        binding.signUpBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getAnalyticsRegistry().trackUserSignUpForAccount();
                startActivity(environment.getRouter().getRegisterIntent());
            }
        });*/
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.LAUNCH_SCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (environment.getLoginPrefs().getUsername() != null) {
            finish();
            environment.getRouter().showMainDashboard(this);
        }
    }

    void sendAnalyticsCourseDetail(){
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Events.VIEW_LOGIN_SCREEN_BUTTON_CLICK,null,null,null);
    }

}