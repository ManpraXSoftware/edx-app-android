package org.tta.mobile.tta.ui.logistration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.logistration.view_model.SigninViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class SigninFragment extends TaBaseFragment {
    private static final int RANK = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding(inflater, container, R.layout.t_fragment_signin, new SigninViewModel(getActivity(), this))
                .getRoot();
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.signin.name()));
    }
}
