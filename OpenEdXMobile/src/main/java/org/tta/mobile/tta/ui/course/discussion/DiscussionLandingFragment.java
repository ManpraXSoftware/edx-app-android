package org.tta.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tta.mobile.R;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.course.discussion.view_model.DiscussionLandingViewModel;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class DiscussionLandingFragment extends TaBaseFragment {
    public static final String TAG = DiscussionLandingFragment.class.getCanonicalName();
    private int RANK;

    private DiscussionLandingViewModel viewModel;

    private EnrolledCoursesResponse course;
    private Content content;

    public static DiscussionLandingFragment newInstance(EnrolledCoursesResponse course, Content content) {
        DiscussionLandingFragment discussionLandingFragment = new DiscussionLandingFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_CONTENT, content);
        args.putSerializable(Constants.KEY_ENROLLED_COURSE, course);
        discussionLandingFragment.setArguments(args);
        discussionLandingFragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return discussionLandingFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            content = savedInstanceState.getParcelable(Constants.KEY_CONTENT);
            course = (EnrolledCoursesResponse) savedInstanceState.getSerializable(Constants.KEY_ENROLLED_COURSE);
        } else if (getArguments() != null){
            content = getArguments().getParcelable(Constants.KEY_CONTENT);
            course = (EnrolledCoursesResponse) getArguments().getSerializable(Constants.KEY_ENROLLED_COURSE);
        }

        viewModel = new DiscussionLandingViewModel(getActivity(), this, course, content);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_discussion_landing, viewModel).getRoot();

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

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
