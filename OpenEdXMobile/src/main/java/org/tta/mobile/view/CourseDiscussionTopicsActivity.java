package org.tta.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.tta.mobile.R;
import org.tta.mobile.base.BaseSingleFragmentActivity;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.module.analytics.Analytics;

import roboguice.inject.InjectExtra;

public class CourseDiscussionTopicsActivity extends BaseSingleFragmentActivity {

    @Inject
    private CourseDiscussionTopicsFragment discussionFragment;

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FORUM_VIEW_TOPICS,
                courseData.getCourse().getId(), null, null);
    }

    @Override
    public Fragment getFirstFragment() {
        if (courseData != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
            discussionFragment.setArguments(bundle);
        }
        discussionFragment.setRetainInstance(true);
        return discussionFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.discussion_topics_title));
    }
}
