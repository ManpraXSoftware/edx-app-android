package org.tta.mobile.tta.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.tutorials.MxTooltip;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.tta.mobile.tta.ui.library.view_model.LibraryViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.view.common.PageViewStateCallback;

public class LibraryFragment extends TaBaseFragment {
    public static final String TAG = LibraryFragment.class.getCanonicalName();
    private static final int RANK = 2;
    private LibraryViewModel viewModel;

    private SearchPageOpenedListener searchPageOpenedListener;
    private ViewPager viewPager;
    private AppPref mAppPref;

    public static LibraryFragment newInstance(SearchPageOpenedListener searchPageOpenedListener){
        LibraryFragment fragment = new LibraryFragment();
        fragment.searchPageOpenedListener = searchPageOpenedListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.library.name()));
        viewModel = new LibraryViewModel(getActivity(), this, searchPageOpenedListener);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_library, viewModel)
                .getRoot();

        TabLayout tabLayout = view.findViewById(R.id.listing_tab_layout);
        viewPager = view.findViewById(R.id.listing_view_pager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout.setupWithViewPager(viewPager);
        mAppPref = new AppPref(view.getContext());
//        viewModel.setToolTip();

        tabLayout.post(() -> {
            if (!mAppPref.isProfileVisited()) {
//                viewModel.setToolTip();
//                ToolTipView.showToolTip(getActivity(), getResources().getString(R.string.library_tabs_top),
//                        tabLayout, Gravity.BOTTOM);
                new MxTooltip.Builder(getActivity())
                        .anchorView(tabLayout)
                        .text(getResources().getString(R.string.library_tabs_top))
                        .gravity(Gravity.BOTTOM)
                        .animated(true)
                        .transparentOverlay(true)
                        .arrowDrawable(R.drawable.up_arrow)
                        .build()
                        .show();
                mAppPref.setProfileVisited(true);
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.library.name()));
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, LibraryFragment.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "onResume");
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
        });
    }
}
