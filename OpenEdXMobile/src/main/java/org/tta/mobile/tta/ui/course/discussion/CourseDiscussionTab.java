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
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.course.discussion.view_model.CourseDiscussionViewModel;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

public class CourseDiscussionTab extends TaBaseFragment {
    private int RANK;

    private CourseDiscussionViewModel viewModel;

    private EnrolledCoursesResponse course;
    private Content content;

    public static CourseDiscussionTab newInstance(EnrolledCoursesResponse course, Content content){
        CourseDiscussionTab fragment = new CourseDiscussionTab();
        Bundle args = new Bundle();
        args.putParcelable(Constants.KEY_CONTENT, content);
        args.putSerializable(Constants.KEY_ENROLLED_COURSE, course);
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
        } else if (getArguments() != null){
            content = getArguments().getParcelable(Constants.KEY_CONTENT);
            course = (EnrolledCoursesResponse) getArguments().getSerializable(Constants.KEY_ENROLLED_COURSE);
        }

        viewModel = new CourseDiscussionViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_course_discussion, viewModel).getRoot();

        if (isAdded()) {
            ActivityUtil.replaceFragmentInActivity(
                    getActivity().getSupportFragmentManager(),
                    DiscussionLandingFragment.newInstance(course, content),
                    R.id.discussion_tab,
                    DiscussionLandingFragment.TAG,
                    false, null
            );
        }

        return view;
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.discussion.name()));
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
}
