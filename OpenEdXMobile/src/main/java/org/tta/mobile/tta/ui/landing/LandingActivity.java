package org.tta.mobile.tta.ui.landing;

import android.databinding.ObservableInt;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;

import org.tta.mobile.R;
import org.tta.mobile.tta.data.pref.AppPref;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.landing.view_model.LandingViewModel;
import org.tta.mobile.tta.ui.search.SearchFragment;
import org.tta.mobile.tta.utils.ToolTipView;

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

        view.post(() -> {
            if (!mAppPref.isProfileVisited()) {
                ToolTipView.showToolTip(this, "यहाँ सभी सामग्री पाए", findViewById(R.id.action_library), Gravity.TOP);
                mAppPref.setProfileVisited(true);
            }
        });
        viewModel.registerEventBus();
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
    }
}
