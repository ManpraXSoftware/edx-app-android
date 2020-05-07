package org.tta.mobile.tta.ui.agenda_items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tta.mobile.R;

import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.model.agenda.AgendaItem;
import org.tta.mobile.tta.data.model.agenda.AgendaList;
import org.tta.mobile.tta.ui.agenda_items.view_model.AgendaItemViewModel;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.utils.BreadcrumbUtil;


public class AgendaItemTab extends TaBaseFragment {
    private static final int RANK = 4;

    private AgendaItemViewModel viewModel;
    public AgendaItem item;
    private String toolbarData;
    private AgendaList agendaList;

    public static AgendaItemTab newInstance(AgendaItem item, String toolbarData, AgendaList agendaList) {
        AgendaItemTab fragment = new AgendaItemTab();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_AGENDA_ITEM, item);
        args.putParcelable(Constants.KEY_AGENDA_LIST, agendaList);
        args.putString(Constants.KEY_TOOLBAR_DATA, toolbarData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            agendaList = savedInstanceState.getParcelable(Constants.KEY_AGENDA_LIST);
            item = savedInstanceState.getParcelable(Constants.KEY_AGENDA_ITEM);
            toolbarData = savedInstanceState.getString(Constants.KEY_TOOLBAR_DATA);
        } else if (getArguments() != null){
            agendaList = getArguments().getParcelable(Constants.KEY_AGENDA_LIST);
            item = getArguments().getParcelable(Constants.KEY_AGENDA_ITEM);
            toolbarData = getArguments().getString(Constants.KEY_TOOLBAR_DATA);
        }

        viewModel = new AgendaItemViewModel(getActivity(), this, item, toolbarData, agendaList);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_agenda_item_tab, viewModel)
                .getRoot();
        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, item.getSource_name()));

        Nav nav;
        if (toolbarData.equalsIgnoreCase(getString(R.string.state_wise_list))){
            nav = Nav.state_agenda;
        } else if (toolbarData.equalsIgnoreCase(getString(R.string.my_agenda))) {
            nav = Nav.my_agenda;
        } else {
            nav = Nav.download_agenda;
        }

        analytic.addMxAnalytics_db(item.getSource_title() , Action.Nav, nav.name(),
                org.tta.mobile.tta.analytics.analytics_enums.Source.Mobile, null);
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
        if (item != null) {
            outState.putParcelable(Constants.KEY_AGENDA_ITEM, item);
        }
        if (agendaList != null) {
            outState.putParcelable(Constants.KEY_AGENDA_LIST, agendaList);
        }
        if (toolbarData != null) {
            outState.putString(Constants.KEY_TOOLBAR_DATA, toolbarData);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
