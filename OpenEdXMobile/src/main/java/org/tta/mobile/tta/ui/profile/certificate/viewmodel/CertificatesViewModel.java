package org.tta.mobile.tta.ui.profile.certificate.viewmodel;

import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.profile.certificate.GeneratedCertificatesFragment;
import org.tta.mobile.tta.ui.profile.certificate.PendingCertificatesFragment;
import org.tta.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.List;

public class CertificatesViewModel extends BaseViewModel {

    private List<Fragment> fragments;
    private List<String> titles;
    public CertificatesPagerAdapter adapter;
    private TaBaseFragment tab1, tab2;
    private int position;

    public ObservableInt initialPosition = new ObservableInt();
    public ObservableInt tabPosition = new ObservableInt();

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null){
                callback.onPageShow();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public CertificatesViewModel(BaseVMActivity activity, int tabPosition) {
        super(activity);
        position = tabPosition;
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        adapter = new CertificatesPagerAdapter(mActivity.getSupportFragmentManager());
        setTabs();
    }

    private void setTabs() {

        tab1 = GeneratedCertificatesFragment.newInstance();
        fragments.add(tab1);
        titles.add(mActivity.getString(R.string.generated_certificates));

        tab2 = PendingCertificatesFragment.newInstance();
        fragments.add(tab2);
        titles.add(mActivity.getString(R.string.pending_certificates));

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, CertificatesViewModel.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "setTabs");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
        tabPosition.set(position);
        PageViewStateCallback callback = (PageViewStateCallback) fragments.get(position);
        if (callback != null){
            callback.onPageShow();
        }
    }

    public class CertificatesPagerAdapter extends BasePagerAdapter {
        public CertificatesPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
