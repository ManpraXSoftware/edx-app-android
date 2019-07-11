package org.tta.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.tta.mobile.R;
import org.tta.mobile.discussion.DiscussionComment;
import org.tta.mobile.discussion.DiscussionThread;
import org.tta.mobile.discussion.DiscussionTopic;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.course.discussion.view_model.DiscussionCommentViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class DiscussionCommentActivity extends BaseVMActivity {
    private int RANK;

    private DiscussionCommentViewModel viewModel;

    private EnrolledCoursesResponse course;
    private Content content;
    private DiscussionTopic topic;
    private DiscussionThread thread;
    private DiscussionComment comment;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        getExtras();
        viewModel = new DiscussionCommentViewModel(this, course, content, topic, thread, comment);
        binding(R.layout.t_activity_discussion_comment, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel.registerEventBus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null){
            course = (EnrolledCoursesResponse) parameters.getSerializable(Constants.KEY_ENROLLED_COURSE);
            topic = (DiscussionTopic) parameters.getSerializable(Constants.KEY_DISCUSSION_TOPIC);
            thread = (DiscussionThread) parameters.getSerializable(Constants.KEY_DISCUSSION_THREAD);
            comment = (DiscussionComment) parameters.getSerializable(Constants.KEY_DISCUSSION_COMMENT);
            content = parameters.getParcelable(Constants.KEY_CONTENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.comment.name()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
