package org.tta.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tta.mobile.R;
import org.tta.mobile.discussion.DiscussionTopicDepth;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.course.discussion.view_model.DiscussionTopicViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class DiscussionTopicFragment extends TaBaseFragment {
    public static final String TAG = DiscussionTopicFragment.class.getCanonicalName();
    private int RANK;

    private DiscussionTopicViewModel viewModel;

    private DiscussionTopicDepth topicDepth;
    private EnrolledCoursesResponse course;
    private Content content;

    public static DiscussionTopicFragment newInstance(EnrolledCoursesResponse course, Content content, DiscussionTopicDepth topicDepth){
        DiscussionTopicFragment fragment = new DiscussionTopicFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_CONTENT, content);
        args.putSerializable(Constants.KEY_ENROLLED_COURSE, course);
        args.putSerializable(Constants.KEY_TOPIC_DEPTH, topicDepth);
        fragment.setArguments(args);
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            content = savedInstanceState.getParcelable(Constants.KEY_CONTENT);
            course = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Constants.KEY_ENROLLED_COURSE);
            topicDepth = (DiscussionTopicDepth) savedInstanceState.getSerializable(Constants.KEY_TOPIC_DEPTH);
        } else if (getArguments() != null){
            content = getArguments().getParcelable(Constants.KEY_CONTENT);
            course = (EnrolledCoursesResponse) getArguments().getSerializable(Constants.KEY_ENROLLED_COURSE);
            topicDepth = (DiscussionTopicDepth) getArguments().getSerializable(Constants.KEY_TOPIC_DEPTH);
        }

        viewModel = new DiscussionTopicViewModel(getActivity(), this, course, content, topicDepth);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_discussion_topic, viewModel).getRoot();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (content != null) {
            outState.putParcelable(Constants.KEY_CONTENT, content);
        }
        if (course != null) {
            outState.putSerializable(Constants.KEY_ENROLLED_COURSE, course);
        }
        if (topicDepth != null){
            outState.putSerializable(Constants.KEY_TOPIC_DEPTH, topicDepth);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
