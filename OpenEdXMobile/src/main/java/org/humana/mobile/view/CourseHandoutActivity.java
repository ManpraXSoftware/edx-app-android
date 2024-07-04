package org.humana.mobile.view;

import androidx.fragment.app.Fragment;

import org.humana.mobile.R;
import org.humana.mobile.base.BaseSingleFragmentActivity;

public class CourseHandoutActivity extends BaseSingleFragmentActivity {


    private Fragment fragment;

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_handouts));
    }

    @Override
    public Fragment getFirstFragment() {
        CourseHandoutFragment fragment = new CourseHandoutFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }
}
