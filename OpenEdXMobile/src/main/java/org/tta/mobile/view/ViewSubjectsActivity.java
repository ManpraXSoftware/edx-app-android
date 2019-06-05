package org.tta.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.tta.mobile.base.BaseSingleFragmentActivity;
import org.tta.mobile.logger.Logger;

public class ViewSubjectsActivity extends BaseSingleFragmentActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());

    public static Intent newIntent(Activity activity) {
        return new Intent(activity, ViewSubjectsActivity.class);
    }

    @Override
    public Fragment getFirstFragment() {
        return new ViewSubjectsFragment();
    }
}
