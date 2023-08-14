package org.humana.mobile.view;

import androidx.fragment.app.Fragment;

import com.google.inject.Inject;

import org.humana.mobile.base.BaseSingleFragmentActivity;

public class DiscussionAddResponseActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddResponseFragment discussionAddResponseFragment;

    @Override
    public Fragment getFirstFragment() {
        discussionAddResponseFragment.setArguments(getIntent().getExtras());
        return discussionAddResponseFragment;
    }
}
