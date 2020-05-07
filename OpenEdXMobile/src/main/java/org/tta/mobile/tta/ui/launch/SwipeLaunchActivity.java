package org.tta.mobile.tta.ui.launch;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.tta.mobile.R;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.launch.view_model.SwipeLaunchViewModel;

public class SwipeLaunchActivity extends BaseVMActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding(R.layout.t_activity_swipe_launch, new SwipeLaunchViewModel(this));

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        tabLayout.setupWithViewPager(viewPager);
    }
}
