package org.tta.mobile.tta.ui.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.tta.utils.FirebaseUtil;

public class SplashActivity extends BaseVMActivity {
    private static final int RANK = 0;

    private SplashViewModel viewModel;
    private FirebaseUtil firebaseUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SplashViewModel(this);
        binding(R.layout.t_activity_splash, viewModel);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.appopen.name()));
        viewModel.startRouting(this);

        firebaseUtil=new FirebaseUtil(this);
        firebaseUtil.syncFirebaseToken();
    }
}
