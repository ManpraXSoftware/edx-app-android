package org.tta.mobile.view;

import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.tta.mobile.R;
import org.tta.mobile.base.BaseSingleFragmentActivity;

public class CourseDiscussionCommentsActivity extends BaseSingleFragmentActivity {

    @Inject
    private CourseDiscussionCommentsFragment commentsFragment;

    @Override
    public Fragment getFirstFragment() {
        commentsFragment.setArguments(getIntent().getExtras());
        commentsFragment.setRetainInstance(true);
        return commentsFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.discussion_comments));
    }

}
