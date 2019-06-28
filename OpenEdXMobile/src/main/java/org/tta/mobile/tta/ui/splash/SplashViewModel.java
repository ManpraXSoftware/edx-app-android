package org.tta.mobile.tta.ui.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Page;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.SurveyType;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.ui.launch.SwipeLaunchActivity;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.tta.mobile.tta.ui.logistration.UserInfoActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.wordpress_client.util.ConnectCookieHelper;

public class SplashViewModel extends BaseViewModel {

    private static final long DELAY = 2000;

    public SplashViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.onAppStartOrClose();
    }

    public void startRouting(){

        new Handler().postDelayed(() -> {
            mActivity.finish();
            if (mDataManager.getAppPref().isFirstLaunch()){
                ActivityUtil.gotoPage(mActivity, SwipeLaunchActivity.class);
                mDataManager.getAppPref().setFirstLaunch(false);
            } else {
                if (mDataManager.getLoginPrefs().getCurrentUserProfile() == null) {
                    ActivityUtil.gotoPage(mActivity, SigninRegisterActivity.class);
                } else {
                    performBackgroundTasks();
                    if (mDataManager.getLoginPrefs().getCurrentUserProfile().name == null ||
                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals("") ||
                            mDataManager.getLoginPrefs().getCurrentUserProfile().name.equals(mDataManager.getLoginPrefs().getUsername())
                    ) {
                        ActivityUtil.gotoPage(mActivity, UserInfoActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    } else {
                        ActivityUtil.gotoPage(mActivity, LandingActivity.class);
                    }

                    mActivity.analytic.addMxAnalytics_db("TA App open", Action.AppOpen,
                            Page.LoginPage.name(), Source.Mobile, null);

                }
            }
        }, DELAY);

    }

    private void performBackgroundTasks(){
        mDataManager.setCustomFieldAttributes(null);
        ConnectCookieHelper cHelper=new ConnectCookieHelper();
        if (cHelper.isCookieExpire()) {
            mDataManager.setConnectCookies();
        }
        mDataManager.checkSurvey(mActivity, SurveyType.Login);
        mDataManager.updateFirebaseToken(getActivity());
    }

}
