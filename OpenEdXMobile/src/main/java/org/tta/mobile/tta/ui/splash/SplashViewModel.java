package org.tta.mobile.tta.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.tta.mobile.tta.ui.logistration.UserInfoActivity;
import org.tta.mobile.tta.utils.ActivityUtil;

public class SplashViewModel extends BaseViewModel {

    private static final long DELAY = 2000;

    public SplashViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.onAppStart();
    }

    public void startRouting(Activity activity){

        new Handler().postDelayed(() -> {
            activity.finish();
            if (mDataManager.getAppPref().isFirstLaunch()){
                ActivityUtil.gotoPage(activity, SwipeLaunchActivity.class);
                mDataManager.getAppPref().setFirstLaunch(false);
            } else {
                if (mDataManager.getLoginPrefs().getCurrentUserProfile() == null) {
                    ActivityUtil.gotoPage(activity, SigninRegisterActivity.class);
                } else {
                    mDataManager.setCustomFieldAttributes(null);
                    if (mDataManager.getLoginPrefs().getCurrentUserProfile().name == null ||
                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals("") ||
                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals(mDataManager.getLoginPrefs().getUsername())
                    ) {
                        ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    } else {
                        ActivityUtil.gotoPage(activity, LandingActivity.class);
                    }
                }
            }
        }, DELAY);

    }

}
