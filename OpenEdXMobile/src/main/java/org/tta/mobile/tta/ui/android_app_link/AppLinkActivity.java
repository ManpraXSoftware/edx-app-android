package org.tta.mobile.tta.ui.android_app_link;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.tta.mobile.tta.ui.logistration.UserInfoActivity;
import org.tta.mobile.tta.utils.ActivityUtil;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class AppLinkActivity extends BaseVMActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ATTENTION: This was auto-generated to handle app links.
        handleIntent(getIntent());

    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null)
            openApp();
    }

    private void openApp(){
        if (loginPrefs == null || !loginPrefs.isLoggedIn()) {
            ActivityUtil.gotoPage(this, SigninRegisterActivity.class);
            this.finish();
        } else if (loginPrefs.getDisplayName() == null || loginPrefs.getDisplayName().equals(loginPrefs.getUsername())){
            ActivityUtil.gotoPage(this, UserInfoActivity.class);
            this.finish();
        }
    }

}
