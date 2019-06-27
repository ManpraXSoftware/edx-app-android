package org.tta.mobile.tta.ui.course.discussion.view_model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import org.tta.mobile.R;
import org.tta.mobile.discussion.DiscussionComment;
import org.tta.mobile.discussion.DiscussionRequestFields;
import org.tta.mobile.discussion.DiscussionThread;
import org.tta.mobile.discussion.DiscussionTopic;
import org.tta.mobile.event.NetworkConnectivityChangeEvent;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.DiscussionTopicType;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.SortType;
import org.tta.mobile.tta.event.DiscussionThreadUpdateEvent;
import org.tta.mobile.tta.event.LoadMoreDiscussionCommentsEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.course.discussion.DiscussionCommentActivity;
import org.tta.mobile.tta.ui.course.discussion.DiscussionCommentsTab;
import org.tta.mobile.tta.ui.interfaces.DiscussionCommentClickListener;
import org.tta.mobile.tta.ui.profile.OtherProfileActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.JsonUtil;
import org.tta.mobile.util.DateUtil;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.ResourceUtil;
import org.tta.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class DiscussionThreadViewModel extends BaseViewModel
    implements DiscussionCommentClickListener {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_PAGE = 1;

    public EnrolledCoursesResponse course;
    public DiscussionTopic topic;
    public DiscussionThread thread;
    private List<Fragment> fragments;
    private List<String> titles;
    private String commentParentId = null;
    private DiscussionComment selectedComment;
    private List<DiscussionComment> comments;

    public ObservableField<String> userImage = new ObservableField<>();
    public ObservableInt userPlaceholder = new ObservableInt(R.drawable.profile_photo_placeholder);
    public ObservableField<String> threadDate = new ObservableField<>();
    public ObservableField<String> likeCount = new ObservableField<>("");
    public ObservableField<String> commentsCount = new ObservableField<>("");
    public ObservableInt likeIcon = new ObservableInt(R.drawable.t_icon_like);
    public ObservableBoolean replyingToVisible = new ObservableBoolean();
    public ObservableBoolean commentFocus = new ObservableBoolean();
    public ObservableField<String> replyingToText = new ObservableField<>();
    public ObservableField<String> comment = new ObservableField<>();
    public ObservableBoolean offlineVisible = new ObservableBoolean();
    public ObservableInt initialPosition = new ObservableInt();

    private DiscussionCommentsTab tab1;
    private DiscussionCommentsTab tab2;
    private DiscussionCommentsTab tab3;

    private int take, page;
    private boolean allLoaded;

    public CommentsPagerAdapter adapter;

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null){
                callback.onPageShow();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public DiscussionThreadViewModel(BaseVMActivity activity, EnrolledCoursesResponse course, DiscussionTopic topic, DiscussionThread thread) {
        super(activity);
        this.course = course;
        this.topic = topic;
        this.thread = thread;
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        comments = new ArrayList<>();
        take = DEFAULT_TAKE;
        page = DEFAULT_PAGE;

        userImage.set(thread.getProfileImage() == null ? "" : thread.getProfileImage().getImageUrlMedium());
        threadDate.set(DateUtil.getDisplayTime(thread.getUpdatedAt()));
        likeIcon.set(thread.isVoted() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);

        adapter = new CommentsPagerAdapter(mActivity.getSupportFragmentManager());

        mActivity.showLoading();
        setTabs();
        fetchThread();
        fetchComments();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    private void fetchThread(){

        mDataManager.getDiscussionThread(thread.getIdentifier(), new OnResponseCallback<DiscussionThread>() {
            @Override
            public void onSuccess(DiscussionThread data) {
                thread.setResponseCount(data.getResponseCount());
                thread.setVoteCount(data.getVoteCount());
                commentsCount.set(String.valueOf(thread.getResponseCount()));
                likeCount.set(String.valueOf(thread.getVoteCount()));
            }

            @Override
            public void onFailure(Exception e) {
                commentsCount.set("");
            }
        });

    }

    private void fetchComments() {

        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());

        mDataManager.getThreadComments(thread.getIdentifier(), take, page, requestedFields,
                thread.getType() == DiscussionThread.ThreadType.QUESTION,
                new OnResponseCallback<List<DiscussionComment>>() {
                    @Override
                    public void onSuccess(List<DiscussionComment> data) {
                        mActivity.hideLoading();
                        if (data.size() < take){
                            setLoaded();
                        }
                        populateComments(data);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        setLoaded();
                    }
                });

    }

    private void populateComments(List<DiscussionComment> data) {
        boolean newItemsAdded = false;
        for (DiscussionComment comment: data){
            if (!comments.contains(comment)){
                comments.add(comment);
                newItemsAdded = true;
            }
        }
        if (newItemsAdded) {
            refreshComments();
        }
    }

    private void setTabs() {

        tab1 = DiscussionCommentsTab.newInstance(course, topic, thread, this, comments, SortType.all);
        fragments.add(tab1);
        titles.add(mActivity.getString(R.string.all));

        tab2 = DiscussionCommentsTab.newInstance(course, topic, thread, this, comments, SortType.recent);
        fragments.add(tab2);
        titles.add(mActivity.getString(R.string.recently_added_list));

        tab3 = DiscussionCommentsTab.newInstance(course, topic, thread, this, comments, SortType.relevant);
        fragments.add(tab3);
        titles.add(mActivity.getString(R.string.most_relevant_list));

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            mActivity.showLongSnack(e.getLocalizedMessage());
        }

        initialPosition.set(0);
        tab1.onPageShow();

    }

    public void shareThread(){

    }

    public void likeThread(){
        mActivity.showLoading();
        mDataManager.likeDiscussionThread(thread.getIdentifier(), !thread.isVoted(),
                new OnResponseCallback<DiscussionThread>() {
                    @Override
                    public void onSuccess(DiscussionThread data) {
                        mActivity.hideLoading();
                        thread.setVoted(data.isVoted());
                        thread.setVoteCount(data.getVoteCount());
                        likeCount.set(String.valueOf(thread.getVoteCount()));
                        likeIcon.set(data.isVoted() ? R.drawable.t_icon_like_filled : R.drawable.t_icon_like);

                        mActivity.analytic.addMxAnalytics_db(
                                topic.getName().contains("लेखक") ?
                                        DiscussionTopicType.Postname_AD.name() :
                                        DiscussionTopicType.Postname_CD.name(),
                                data.isVoted() ? Action.DBLike : Action.DBUnlike,
                                course.getCourse().getName(),
                                Source.Mobile, thread.getIdentifier());

                        postThreadUpdated();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    public void onClickThreadUser(){
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_USERNAME, thread.getAuthor());
        ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
    }

    public void addComment(){
        resetReplyToComment();

        if (commentFocus.get()) {
            commentFocus.set(false);
        }
        commentFocus.set(true);
    }

    public void resetReplyToComment(){
        replyingToVisible.set(false);
        commentParentId = null;
        comment.set("");
    }

    public void addReplyToComment(){
        String comment = this.comment.get();
        if (comment == null || comment.trim().equals("")){
            mActivity.showShortToast("Comment cannot be empty");
            return;
        }

        mActivity.showLoading();
        mDataManager.createDiscussionComment(thread.getIdentifier(), comment.trim(), commentParentId,
                new OnResponseCallback<DiscussionComment>() {
                    @Override
                    public void onSuccess(DiscussionComment data) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(mActivity.getString(R.string.comment_added_successfully));

                        if (data.isAuthorAnonymous()){
                            data.setAuthor(loginPrefs.getUsername());
                        }
                        if (data.getDisplayName() == null){
                            data.setDisplayName(loginPrefs.getDisplayName());
                        }
                        if (data.getProfileImage() == null){
                            data.setProfileImage(loginPrefs.getProfileImage());
                        }

                        thread.incrementCommentCount();
                        if (commentParentId == null){
                            thread.incrementResponseCount();
                            commentsCount.set(String.valueOf(thread.getResponseCount()));
                            //TODO: add this new comment to list
                            comments.add(0, data);

                            mActivity.analytic.addMxAnalytics_db(
                                    topic.getName().contains("लेखक") ?
                                            DiscussionTopicType.Postname_AD.name() :
                                            DiscussionTopicType.Postname_CD.name(),
                                    Action.DBComment, course.getCourse().getName(),
                                    Source.Mobile, thread.getIdentifier());

                        } else {
                            selectedComment.incrementChildCount();

                            mActivity.analytic.addMxAnalytics_db(thread.getIdentifier(),
                                    Action.DBCommentReply, course.getCourse().getName(),
                                    Source.Mobile, selectedComment.getIdentifier());

                        }
                        refreshComments();
                        resetReplyToComment();

                        postThreadUpdated();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void onClickLike(DiscussionComment comment) {
        mActivity.showLoading();
        mDataManager.likeDiscussionComment(comment.getIdentifier(), !comment.isVoted(),
                new OnResponseCallback<DiscussionComment>() {
                    @Override
                    public void onSuccess(DiscussionComment data) {
                        mActivity.hideLoading();
                        comment.setVoted(data.isVoted());
                        comment.setVoteCount(data.getVoteCount());
                        refreshComments();

                        mActivity.analytic.addMxAnalytics_db(thread.getIdentifier(),
                                data.isVoted() ? Action.DBCommentlike : Action.DBCommentUnlike,
                                course.getCourse().getName(),
                                Source.Mobile, comment.getIdentifier());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mActivity.hideLoading();
                        mActivity.showLongSnack(e.getLocalizedMessage());
                    }
                });

    }

    @Override
    public void onClickReply(DiscussionComment comment) {
        replyingToText.set(ResourceUtil.getFormattedString(mActivity.getResources(), R.string.replying_to,
                "name", comment.getDisplayName()).toString());
        replyingToVisible.set(true);
        selectedComment = comment;
        commentParentId = comment.getIdentifier();

        if (commentFocus.get()) {
            commentFocus.set(false);
        }
        commentFocus.set(true);
    }

    @Override
    public void onClickUser(DiscussionComment comment) {
        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_USERNAME, comment.getAuthor());
        ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
    }

    @Override
    public void onClickDefault(DiscussionComment comment) {
        Bundle parameters = new Bundle();
        parameters.putSerializable(Constants.KEY_ENROLLED_COURSE, course);
        parameters.putSerializable(Constants.KEY_DISCUSSION_TOPIC, topic);
        parameters.putSerializable(Constants.KEY_DISCUSSION_THREAD, thread);
        parameters.putSerializable(Constants.KEY_DISCUSSION_COMMENT, comment);
        ActivityUtil.gotoPage(mActivity, DiscussionCommentActivity.class, parameters);
    }

    private void setLoaded(){
        tab1.setLoaded();
        tab2.setLoaded();
        tab3.setLoaded();
    }

    private void refreshComments() {
        tab1.refreshList();
        tab2.refreshList();
        tab3.refreshList();
    }

    private void postThreadUpdated(){
        EventBus.getDefault().post(new DiscussionThreadUpdateEvent(thread));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(LoadMoreDiscussionCommentsEvent event){
        page++;
        fetchComments();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event){
        if (NetworkUtil.isConnected(mActivity)){
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public class CommentsPagerAdapter extends BasePagerAdapter {
        public CommentsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
