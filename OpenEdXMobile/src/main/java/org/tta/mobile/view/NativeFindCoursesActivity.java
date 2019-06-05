package org.tta.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.tta.mobile.base.BaseSingleFragmentActivity;
import org.tta.mobile.module.analytics.Analytics;
import org.tta.mobile.view.dialog.NativeFindCoursesFragment;

public class NativeFindCoursesActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, NativeFindCoursesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FIND_COURSES);
    }

    @Override
    public Fragment getFirstFragment() {
        return new NativeFindCoursesFragment();
    }
}
