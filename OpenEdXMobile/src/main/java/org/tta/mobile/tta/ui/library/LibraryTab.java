package org.tta.mobile.tta.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tta.mobile.R;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.Analytic;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.tta.mobile.tta.ui.library.view_model.LibraryTabViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class LibraryTab extends TaBaseFragment {
    private static final int RANK = 3;

    private CollectionConfigResponse cr;

    private Category category;

    private SearchPageOpenedListener searchPageOpenedListener;

    private LibraryTabViewModel viewModel;

    public static LibraryTab newInstance(CollectionConfigResponse cr, Category category,
                                         SearchPageOpenedListener searchPageOpenedListener){
        LibraryTab fragment = new LibraryTab();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_CONFIGURATION, cr);
        args.putParcelable(Constants.KEY_CATEGORY, category);
        fragment.setArguments(args);
        fragment.searchPageOpenedListener = searchPageOpenedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            cr = savedInstanceState.getParcelable(Constants.KEY_CONFIGURATION);
            category = savedInstanceState.getParcelable(Constants.KEY_CATEGORY);
        } else if (getArguments() != null){
            cr = getArguments().getParcelable(Constants.KEY_CONFIGURATION);
            category = getArguments().getParcelable(Constants.KEY_CATEGORY);
        }

        viewModel = new LibraryTabViewModel(getActivity(), this, cr, category, searchPageOpenedListener);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_library_tab, viewModel)
                .getRoot();

        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        String nav = Nav.all.name();
        if (category != null && category.getSource_id() > 0 && cr != null){
            for (Source source: cr.getSource()){
                if (category.getSource_id() == source.getId()){
                    nav = source.getName();
                    break;
                }
            }
        }
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav));

        if (analytic != null) {
            analytic.addMxAnalytics_db(
                    category.getName() , Action.Nav, Nav.library.name(),
                    org.tta.mobile.tta.analytics.analytics_enums.Source.Mobile, null);
        }
    }

    /*@Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode){
            case PermissionsUtil.WRITE_STORAGE_PERMISSION_REQUEST:
                viewModel.showContentDashboard();
                break;
        }
    }*/

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (cr != null) {
            outState.putParcelable(Constants.KEY_CONFIGURATION, cr);
        }
        if (category != null) {
            outState.putParcelable(Constants.KEY_CATEGORY, category);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
