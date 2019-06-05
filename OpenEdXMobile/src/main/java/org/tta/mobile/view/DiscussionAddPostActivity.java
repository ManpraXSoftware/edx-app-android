package org.tta.mobile.view;

import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.tta.mobile.base.BaseSingleFragmentActivity;

public class DiscussionAddPostActivity extends BaseSingleFragmentActivity {
    @Inject
    DiscussionAddPostFragment discussionAddPostFragment;

    @Override
    public Fragment getFirstFragment() {
        discussionAddPostFragment.setArguments(getIntent().getExtras());
        return discussionAddPostFragment;
    }
}
