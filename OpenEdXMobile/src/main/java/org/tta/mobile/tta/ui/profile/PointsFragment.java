package org.tta.mobile.tta.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.tta.mobile.R;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.profile.view_model.PointsViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class PointsFragment extends TaBaseFragment {
    public static final String TAG = PointsFragment.class.getCanonicalName();
    private static final int RANK = 3;

    private PointsViewModel viewModel;
    private Toolbar toolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new PointsViewModel(getActivity(), this);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.points_table.name()));

        analytic.addMxAnalytics_db(null, Action.ViewPoints, Nav.points_table.name(), Source.Mobile, null);
    }

    public static PointsFragment newInstance() {
        PointsFragment pointsFragment = new PointsFragment();
        return pointsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_points, viewModel).getRoot();
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.points_table.name()));
    }
}
