package org.tta.mobile.tta.ui.course.discussion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.tta.mobile.R;
import org.tta.mobile.discussion.DiscussionComment;
import org.tta.mobile.discussion.DiscussionThread;
import org.tta.mobile.discussion.DiscussionTopic;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.data.enums.SortType;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.course.discussion.view_model.DiscussionCommentsTabViewModel;
import org.tta.mobile.tta.ui.interfaces.DiscussionCommentClickListener;
import org.tta.mobile.tta.utils.BreadcrumbUtil;

import java.util.List;

public class DiscussionCommentsTab extends TaBaseFragment {
    public static final String TAG = DiscussionCommentsTab.class.getCanonicalName();
    private int RANK;

    private DiscussionCommentsTabViewModel viewModel;

    private EnrolledCoursesResponse course;
    private Content content;
    private DiscussionTopic topic;
    private DiscussionThread thread;
    private DiscussionCommentClickListener listener;
    private List<DiscussionComment> comments;
    private SortType sortType;

    public static DiscussionCommentsTab newInstance(EnrolledCoursesResponse course, Content content, DiscussionTopic topic,
                                                    DiscussionThread thread, DiscussionCommentClickListener listener,
                                                    List<DiscussionComment> comments, SortType sortType){
        DiscussionCommentsTab fragment = new DiscussionCommentsTab();
        fragment.course = course;
        fragment.content = content;
        fragment.topic = topic;
        fragment.thread = thread;
        fragment.listener = listener;
        fragment.sortType = sortType;
        fragment.comments = comments;
        fragment.RANK = BreadcrumbUtil.getCurrentRank() + 1;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new DiscussionCommentsTabViewModel(getActivity(), this, course, content, topic, thread, listener, comments, sortType);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_discussion_comments_tab, viewModel).getRoot();

        return view;
    }

    public void refreshList() {
        if (viewModel != null) {
            viewModel.refreshList();
        }
    }

    public void setLoaded(){
        if (viewModel != null){
            viewModel.setLoaded();
        }
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        Nav nav;
        switch (sortType){
            case all:
                nav = Nav.all;
                break;
            case recent:
                nav = Nav.recently_added;
                break;
            default:
                nav = Nav.most_relevant;
        }
        logger.debug("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, nav.name()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
