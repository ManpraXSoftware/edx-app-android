package org.tta.mobile.tta.ui.landing;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;


import org.tta.mobile.R;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.landing.view_model.LandingViewModel;
import org.tta.mobile.tta.ui.search.SearchFragment;

public class LandingActivity extends BaseVMActivity {

    private LandingViewModel viewModel;

    public static boolean isAlreadyOpened;
    private AppPref mAppPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new LandingViewModel(this);
        binding(R.layout.t_activity_landing, viewModel);

        mAppPref = new AppPref(this);
        BottomNavigationView view = findViewById(R.id.dashboard_bottom_nav);
        view.setItemIconTintList(null);

        viewModel.registerEventBus();



//        view.post(() -> {
//            if (!mAppPref.isProfileVisited()) {
//                ToolTipView.showToolTip(this, "यहाँ सभी सामग्री पाएँ ", view.getChildAt(0), Gravity.TOP);
//                mAppPref.setProfileVisited(true);
//            }
//        });

    }

    @Override
    public void onBackPressed() {

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.TAG);
        if (searchFragment != null && searchFragment.isVisible()){
            viewModel.selectLibrary();
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAlreadyOpened = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
        isAlreadyOpened = false;
        viewModel.getDataManager().onAppStartOrClose();
    }
}
