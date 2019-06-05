package org.tta.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.tta.mobile.R;
import org.tta.mobile.base.BaseSingleFragmentActivity;

public class SettingsActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.settings_txt));
    }

    @Override
    public Fragment getFirstFragment() {
        return new SettingsFragment();
    }

}
