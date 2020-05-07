package org.tta.mobile.tta.ui.profile.certificate;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.ui.profile.certificate.viewmodel.CertificatesViewModel;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.view.common.PageViewStateCallback;

public class CertificatesActivity extends BaseVMActivity {
    private int RANK;

    private CertificatesViewModel viewModel;

    private boolean isPush = false;
    private int tabPosition;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.all_certificates.name()));
        getExtras();
        viewModel = new CertificatesViewModel(this, tabPosition);
        binding(R.layout.t_activity_certificates, viewModel);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.all_certificates.name()));
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, CertificatesActivity.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "onResume");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
        });
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey(Constants.KEY_IS_PUSH)){
                isPush = parameters.getBoolean(Constants.KEY_IS_PUSH);
            }
            if (parameters.containsKey(Constants.KEY_TAB_POSITION)){
                tabPosition = parameters.getInt(Constants.KEY_TAB_POSITION);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isPush){
            super.onBackPressed();
        } else {
            if (!LandingActivity.isAlreadyOpened) {
                ActivityUtil.gotoPage(this, LandingActivity.class);
            }
            finish();
        }
    }
}
