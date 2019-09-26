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
import org.tta.mobile.tta.ui.profile.certificate.viewmodel.PendingCertificatesViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class PendingCertificatesFragment extends TaBaseFragment {
    public static final String TAG = PendingCertificatesFragment.class.getCanonicalName();
    private int RANK;

    private PendingCertificatesViewModel viewModel;

    public static PendingCertificatesFragment newInstance(){
        PendingCertificatesFragment fragment = new PendingCertificatesFragment();
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new PendingCertificatesViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_pending_certificates, viewModel).getRoot();

        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.pending_certificates.name()));
    }
}
