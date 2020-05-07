package org.tta.mobile.tta.ui.splash;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class SplashActivity extends BaseVMActivity {
    private static final int RANK = 0;

    private SplashViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AppSignatureHelper helper=new AppSignatureHelper(getApplicationContext());
//        showLongToast(helper.getAppSignatures().get(0));

        viewModel = new SplashViewModel(this);
        binding(R.layout.t_activity_splash, viewModel);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.appopen.name()));
        viewModel.startRouting();
    }
}
