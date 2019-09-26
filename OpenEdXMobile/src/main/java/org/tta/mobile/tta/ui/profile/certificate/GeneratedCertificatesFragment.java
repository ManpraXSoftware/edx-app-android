package org.tta.mobile.tta.ui.profile.certificate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.profile.certificate.viewmodel.GeneratedCertificatesViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class GeneratedCertificatesFragment extends TaBaseFragment {
    public static final String TAG = GeneratedCertificatesFragment.class.getCanonicalName();
    private int RANK;

    private GeneratedCertificatesViewModel viewModel;

    public static GeneratedCertificatesFragment newInstance(){
        GeneratedCertificatesFragment fragment = new GeneratedCertificatesFragment();
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new GeneratedCertificatesViewModel(getActivity(), this);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_generated_certificates, viewModel).getRoot();

        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.generated_certificates.name()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
