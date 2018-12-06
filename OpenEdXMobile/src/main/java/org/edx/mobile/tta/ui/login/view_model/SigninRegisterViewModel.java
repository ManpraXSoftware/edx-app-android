package org.edx.mobile.tta.ui.login.view_model;

import android.databinding.ObservableField;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.login.RegisterFragment;
import org.edx.mobile.tta.ui.login.SigninFragment;

import java.util.ArrayList;
import java.util.List;

public class SigninRegisterViewModel extends BaseViewModel {

    public SigninRegisterAdapter adapter;

    public List<Fragment> fragments;

    public String[] titles;

    public ObservableField<Integer> initialPosition = new ObservableField<>();

    public SigninRegisterViewModel(BaseVMActivity activity) {
        super(activity);
        fragments = new ArrayList<>();
        fragments.add(new SigninFragment());
        fragments.add(new RegisterFragment());
        titles = new String[]{
                activity.getString(R.string.sign_in),
                activity.getString(R.string.register)
        };
        adapter = new SigninRegisterAdapter(mActivity.getSupportFragmentManager());
        initialPosition.set(mDataManager.getAppPref().isFirstLogin() ? 1 : 0);
    }

    public class SigninRegisterAdapter extends FragmentStatePagerAdapter{

        public SigninRegisterAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}
