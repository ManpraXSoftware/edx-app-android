package org.tta.mobile.tta.ui.agenda_items;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.model.agenda.AgendaItem;
import org.tta.mobile.tta.data.model.agenda.AgendaList;
import org.tta.mobile.tta.ui.agenda_items.view_model.AgendaListViewModel;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AgendaListFragment extends TaBaseFragment {
    public static final String TAG = AgendaListFragment.class.getCanonicalName();
    private static final int RANK = 3;

    private AgendaListViewModel viewModel;
    private String toolbarData;
    private AgendaItem tabSelected;
    private Toolbar toolbar;
    private  List<AgendaItem> items;
    private AgendaList agendaList;

    ViewPager viewPager;
    TabLayout tabLayout;

    public static AgendaListFragment newInstance(String toolbarData, List<AgendaItem> items, AgendaItem tabSelected, AgendaList agendaList){
        AgendaListFragment fragment = new AgendaListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(Constants.KEY_AGENDA_ITEMS, (ArrayList<? extends Parcelable>) items);
        args.putParcelable(Constants.KEY_AGENDA_ITEM, tabSelected);
        args.putParcelable(Constants.KEY_AGENDA_LIST, agendaList);
        args.putString(Constants.KEY_TOOLBAR_DATA, toolbarData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            items = savedInstanceState.getParcelableArrayList(Constants.KEY_AGENDA_ITEMS);
            agendaList = savedInstanceState.getParcelable(Constants.KEY_AGENDA_LIST);
            tabSelected = savedInstanceState.getParcelable(Constants.KEY_AGENDA_ITEM);
            toolbarData = savedInstanceState.getString(Constants.KEY_TOOLBAR_DATA);
        } else if (getArguments() != null){
            items = getArguments().getParcelableArrayList(Constants.KEY_AGENDA_ITEMS);
            agendaList = getArguments().getParcelable(Constants.KEY_AGENDA_LIST);
            tabSelected = getArguments().getParcelable(Constants.KEY_AGENDA_ITEM);
            toolbarData = getArguments().getString(Constants.KEY_TOOLBAR_DATA);
        }

        setBreadcrumb();
        viewModel =  new AgendaListViewModel(getActivity(),this, toolbarData,items,tabSelected, agendaList);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_agenda_list, viewModel)
                .getRoot();
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        tabLayout = view.findViewById(R.id.listing_tab_layout);
        viewPager  = view.findViewById(R.id.listing_view_pager);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.post(() -> {
            tabLayout.getTabAt(items.indexOf(tabSelected)).select();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setBreadcrumb();

        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, AgendaListFragment.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "onResume");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
        });
    }

    private void setBreadcrumb() {
        Nav nav;
        if (toolbarData.equalsIgnoreCase(getString(R.string.state_wise_list))){
            nav = Nav.state_agenda;
        } else if (toolbarData.equalsIgnoreCase(getString(R.string.my_agenda))) {
            nav = Nav.my_agenda;
        } else {
            nav = Nav.download_agenda;
        }
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav.name()));

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (items != null) {
            outState.putParcelableArrayList(Constants.KEY_AGENDA_ITEMS, (ArrayList<? extends Parcelable>) items);
        }
        if (tabSelected != null) {
            outState.putParcelable(Constants.KEY_AGENDA_ITEM, tabSelected);
        }
        if (agendaList != null) {
            outState.putParcelable(Constants.KEY_AGENDA_LIST, agendaList);
        }
        if (toolbarData != null) {
            outState.putString(Constants.KEY_TOOLBAR_DATA, toolbarData);
        }
    }
}
