package org.tta.mobile.tta.ui.launch.view_model;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Page;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.SurveyType;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.ui.launch.LaunchFragment;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;
import org.tta.mobile.tta.ui.logistration.UserInfoActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.wordpress_client.util.ConnectCookieHelper;

import java.util.ArrayList;
import java.util.List;

public class SwipeLaunchViewModel extends BaseViewModel {

    public SectionsPagerAdapter adapter;
    public List<Fragment> fragments;
    public ObservableBoolean fabVisible = new ObservableBoolean(false);

    public ObservableField<ViewPager.OnPageChangeListener> listener = new ObservableField<>(
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {

                }

                @Override
                public void onPageSelected(int i) {
                    if (i == fragments.size()-1){
                        fabVisible.set(true);
                    } else {
                        fabVisible.set(false);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            }
    );

    public SwipeLaunchViewModel(BaseVMActivity activity) {
        super(activity);
        fragments = new ArrayList<>();
        fragments.add(LaunchFragment.newInstance(R.drawable.tta_onboarding_01, activity.getString(R.string.launch_text_1)));
        fragments.add(LaunchFragment.newInstance(R.drawable.tta_onboarding_02, activity.getString(R.string.launch_text_2)));
        fragments.add(LaunchFragment.newInstance(R.drawable.tta_onboarding_03, activity.getString(R.string.launch_text_3)));
        adapter = new SectionsPagerAdapter(mActivity.getSupportFragmentManager());
    }

    public void next() {
        mActivity.finish();
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

    private void performBackgroundTasks(){
        mDataManager.setCustomFieldAttributes(null);
        ConnectCookieHelper cHelper=new ConnectCookieHelper();
        if (cHelper.isCookieExpire()) {
            mDataManager.setConnectCookies();
        }
        mDataManager.checkSurvey(mActivity, SurveyType.Login);
        mDataManager.updateFirebaseToken(getActivity());
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
