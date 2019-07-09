package org.tta.mobile.tta.ui.feed.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowFeedBinding;
import org.tta.mobile.databinding.TRowFeedWithUserBinding;
import org.tta.mobile.databinding.TRowSuggestedTeacherBinding;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.enums.BadgeType;
import org.tta.mobile.tta.data.enums.SourceName;
import org.tta.mobile.tta.data.enums.SourceType;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.model.StatusResponse;
import org.tta.mobile.tta.data.model.feed.SuggestedUser;
import org.tta.mobile.tta.event.UserFollowingChangedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.tutorials.MxTooltip;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.connect.ConnectDashboardActivity;
import org.tta.mobile.tta.ui.course.CourseDashboardActivity;
import org.tta.mobile.tta.ui.feed.FeedShareBottomSheet;
import org.tta.mobile.tta.ui.feed.NotificationsFragment;
import org.tta.mobile.tta.ui.feed.RecommendedUsersFragment;
import org.tta.mobile.tta.ui.profile.OtherProfileActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.AppUtil;
import org.tta.mobile.tta.utils.BadgeHelper;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.tta.utils.DataUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class FeedViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public FeedAdapter feedAdapter;
    public SuggestedUsersAdapter suggestedUsersAdapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean suggestedUsersVisible = new ObservableBoolean();
    public ObservableBoolean featureListVisible = new ObservableBoolean();
    public ObservableBoolean emptyVisible = new ObservableBoolean();
    public ObservableBoolean tooltipVisible = new ObservableBoolean();
    public ObservableField<String> feedToolTip;
    public ObservableField<String> shareToolTip;
    public ObservableInt sharetoolTipGravity;
    public ObservableInt toolTipGravity;

    private List<Feed> feeds;
    List<SuggestedUser> users;
//    private int take, skip;
    private int skip;
    private boolean allLoaded;
    private String delimiterTagChunks, delimiterSectionTag, replacementTagSpace;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        getFeeds();
        return true;
    };

    public View.OnClickListener viewMoreListener = v -> {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new RecommendedUsersFragment(),
                R.id.dashboard_fragment,
                RecommendedUsersFragment.TAG,
                true,
                null
        );
    };

    public FeedViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
//        setToolTip();
        feeds = new ArrayList<>();
        users = new ArrayList<>();
//        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;
        delimiterTagChunks = Constants.DELIMITER_TAG_CHUNKS;
        delimiterSectionTag = Constants.DELIMITER_SECTION_TAG;
        replacementTagSpace = Constants.REPLACEMENT_TAG_SPACE;

        feedAdapter = new FeedAdapter(context);
        suggestedUsersAdapter = new SuggestedUsersAdapter(mActivity);


        feedAdapter.setItems(feeds);
        feedAdapter.setItemClickListener((view, item) -> {

            switch (view.getId()) {
                case R.id.meta_user_layout:
                    showOtherUserProfile(item.getMeta_data().getUser_username());
                    break;
                case R.id.feed_share:
                    openShareMenu(item);
                    break;
                case R.id.feed_comment_layout:
                    if (item.getMeta_data().getSource_name().equalsIgnoreCase(SourceName.course.name())) {
                        mActivity.showLoading();
                        mDataManager.getContentFromSourceIdentity(item.getMeta_data().getId(),
                                new OnResponseCallback<Content>() {
                                    @Override
                                    public void onSuccess(Content data) {
                                        mActivity.hideLoading();

                                        Bundle parameters = new Bundle();
                                        parameters.putParcelable(Constants.KEY_CONTENT, data);
                                        parameters.putInt(Constants.KEY_TAB_POSITION, 1);
                                        ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        mActivity.hideLoading();
                                        mActivity.showLongSnack(e.getLocalizedMessage());
                                    }
                                });
                        break;
                    }
                default:
                    try {
                        switch (Action.valueOf(item.getAction())) {
                            case CertificateGenerate:
                            case GenerateCertificate:
                            case Certificate:
                            case Badge:
                                showOtherUserProfile(item.getMeta_data().getUser_username());
                                break;

                            case DBComment:
                            case DBCommentlike:
                            case DBCommentReply:
                            case DBLike:
                            case DBPost:
                                if (item.getMeta_data().getCourse_key() != null) {
                                    mActivity.showLoading();
                                    mDataManager.getContentFromSourceIdentity(item.getMeta_data().getCourse_key(),
                                            new OnResponseCallback<Content>() {
                                                @Override
                                                public void onSuccess(Content data) {
                                                    mActivity.hideLoading();
                                                    showContentDashboard(data);
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    mActivity.hideLoading();
                                                    mActivity.showLongSnack(e.getLocalizedMessage());
                                                }
                                            });
                                    break;
                                }

                            default:
                                if (item.getMeta_data().getId() != null) {
                                    mActivity.showLoading();
                                    mDataManager.getContentFromSourceIdentity(item.getMeta_data().getId(),
                                            new OnResponseCallback<Content>() {
                                                @Override
                                                public void onSuccess(Content data) {
                                                    mActivity.hideLoading();
                                                    showContentDashboard(data);
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    mActivity.hideLoading();
                                                    mActivity.showLongSnack(e.getLocalizedMessage());
                                                }
                                            });
                                } else if (item.getMeta_data().getSource_name().equalsIgnoreCase(Action.appupdate.name())){
                                    AppUtil.openAppOnPlayStore(mActivity, mActivity.getPackageName());
                                }

                        }

                        break;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                /*case R.id.feed_like_layout:

                    switch (Action.valueOf(item.getAction())){
                        case CourseLike:
                        case LikePost:
                        case MostPopular:
                        case CommentPost:
                        case Share:
                        case SharePostApp:
                        case ShareCourse:
                        case NewPost:
                            mActivity.showLoading();
                            mDataManager.setLikeUsingSourceIdentity(item.getMeta_data().getId(),
                                    new OnResponseCallback<StatusResponse>() {
                                        @Override
                                        public void onSuccess(StatusResponse data) {
                                            mActivity.hideLoading();
                                            item.getMeta_data().setLiked(data.getStatus());
                                            item.getMeta_data().setLike_count(
                                                    data.getStatus() ? item.getMeta_data().getLike_count() + 1:
                                                            item.getMeta_data().getLike_count() - 1
                                            );
                                            feedAdapter.notifyItemChanged(feedAdapter.getItemPosition(item));
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            mActivity.hideLoading();
                                            mActivity.showLongSnack(e.getLocalizedMessage());
                                        }
                                    });

                            break;

                        case DBComment:
                        case DBLike:
                            mActivity.showLoading();
                            mDataManager.likeDiscussionThread(item.getMeta_data().getId(), !item.getMeta_data().isLiked(),
                                    new OnResponseCallback<DiscussionThread>() {
                                        @Override
                                        public void onSuccess(DiscussionThread data) {
                                            mActivity.hideLoading();
                                            item.getMeta_data().setLiked(data.isVoted());
                                            item.getMeta_data().setLike_count(data.getVoteCount());
                                            feedAdapter.notifyItemChanged(feedAdapter.getItemPosition(item));
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            mActivity.hideLoading();
                                            mActivity.showLongSnack(e.getLocalizedMessage());
                                        }
                                    });

                            break;

                    }

                    break;*/
            }

        });

        suggestedUsersAdapter.setItems(users);
        suggestedUsersAdapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.follow_btn:
                    mActivity.showLoading();

                    mDataManager.followUnfollowUser(item.getUsername(), new OnResponseCallback<StatusResponse>() {
                        @Override
                        public void onSuccess(StatusResponse data) {
                            mActivity.hideLoading();
                            item.setFollowed(data.getStatus());
                            suggestedUsersAdapter.notifyItemChanged(suggestedUsersAdapter.getItemPosition(item));

                            if (data.getStatus()) {
                                mActivity.analytic.addMxAnalytics_db(item.getUsername(), Action.FollowUser,
                                        Nav.feed.name(), Source.Mobile, item.getUsername());
                            } else {
                                mActivity.analytic.addMxAnalytics_db(item.getUsername(), Action.UnfollowUser,
                                        Nav.feed.name(), Source.Mobile, item.getUsername());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mActivity.hideLoading();
                            mActivity.showLongSnack(e.getLocalizedMessage());
                        }
                    });
                    break;

                default:
                    showOtherUserProfile(item.getUsername());
            }
        });

        mActivity.showLoading();
        getFeeds();
        getSuggestedUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new LinearLayoutManager(mActivity);
    }

    public void getFeatureList(OnResponseCallback<List<Content>> callback) {

        mDataManager.getFeedFeatureList(new OnResponseCallback<List<Content>>() {
            @Override
            public void onSuccess(List<Content> data) {
                callback.onSuccess(data);
                featureListVisible.set(true);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
                featureListVisible.set(false);
            }
        });

    }

    private void getFeeds() {

        mDataManager.getFeeds(skip, new OnResponseCallback<List<Feed>>() {
            @Override
            public void onSuccess(List<Feed> data) {
                mActivity.hideLoading();
                if (data.size() < 1) {
                    allLoaded = true;
                }
                populateFeeds(data);
                feedAdapter.setLoadingDone();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                allLoaded = true;
                feedAdapter.setLoadingDone();
                toggleEmptyVisibility();
            }
        });

    }
//    public void showTooLTip() {
//        tooltipVisible.set(true);
//    }

    private void getSuggestedUsers() {

        mDataManager.getSuggestedUsers(10, 0, new OnResponseCallback<List<SuggestedUser>>() {
            @Override
            public void onSuccess(List<SuggestedUser> data) {
                suggestedUsersVisible.set(true);
                users.addAll(data);
                suggestedUsersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                suggestedUsersVisible.set(false);
            }
        });

    }

    private void populateFeeds(List<Feed> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (Feed feed : data) {
            if (!feeds.contains(feed)) {
                feeds.add(feed);
                newItemsAdded = true;
                n++;
            }
        }
        if (newItemsAdded) {
            feedAdapter.notifyItemRangeInserted(feeds.size() - n, n);
        }
        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (feeds == null || feeds.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    public void showContentDashboard(Content selectedContent) {

        Bundle parameters = new Bundle();
        parameters.putParcelable(Constants.KEY_CONTENT, selectedContent);
        if (selectedContent.getSource().getType().equalsIgnoreCase(SourceType.course.name()) ||
                selectedContent.getSource().getType().equalsIgnoreCase(SourceType.edx.name())) {
            ActivityUtil.gotoPage(mActivity, CourseDashboardActivity.class, parameters);
        } else {
            ActivityUtil.gotoPage(mActivity, ConnectDashboardActivity.class, parameters);
        }

    }

    private void showOtherUserProfile(String username) {
        if (username != null) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_USERNAME, username);
            ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
        }
    }

    public void showNotifications() {
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new NotificationsFragment(),
                R.id.dashboard_fragment,
                NotificationsFragment.TAG,
                true,
                null
        );
    }


    private void setToolTip() {
        if (!mDataManager.getAppPref().isFeedVisited()) {
            feedToolTip = new ObservableField<>(mActivity.getResources().getString(R.string.feed_follow));
            shareToolTip = new ObservableField<>(mActivity.getResources().getString(R.string.feed_share));
            toolTipGravity = new ObservableInt(Gravity.BOTTOM);
            sharetoolTipGravity = new ObservableInt(Gravity.BOTTOM);
//            mDataManager.getAppPref().setFeedVisited(true);
        }
    }

    private void openShareMenu(Feed feed) {

        FeedShareBottomSheet bottomSheet = FeedShareBottomSheet.newInstance(
                (componentName, shareType) -> {

                    mActivity.analytic.addMxAnalytics_db(feed.getMeta_data().getText(), Action.Share,
                            feed.getMeta_data().getSource_name(), Source.Mobile, feed.getMeta_data().getId(),
                            BreadcrumbUtil.getBreadcrumb() + "/" + shareType.name());

                }, feed
        );
        bottomSheet.show(mActivity.getSupportFragmentManager(), FeedShareBottomSheet.TAG);

    }

    private String getFeedTitle(Feed feed) {

        try {
            switch (Action.valueOf(feed.getAction())) {
                case CourseLike:
                case LikePost:
                    if (feed.getState() == null) {
                        return String.format(
                                mActivity.getString(R.string.feed_format_content_like),
                                feed.getAction_by(), feed.getMeta_data().getSource_title()
                        );
                    } else {
                        return String.format(
                                mActivity.getString(R.string.feed_format_content_like_from),
                                feed.getAction_by(), DataUtil.getStateNameFromValue(feed.getState()),
                                feed.getMeta_data().getSource_title()
                        );
                    }

                case DBComment:
                    if (feed.getState() == null) {
                        return String.format(mActivity.getString(R.string.feed_format_db_comment), feed.getAction_by());
                    } else {
                        return String.format(mActivity.getString(R.string.feed_format_db_comment_from),
                                feed.getAction_by(), DataUtil.getStateNameFromValue(feed.getState()));
                    }

                case MostPopular:
                    if (feed.getState() == null) {
                        return String.format(mActivity.getString(R.string.feed_format_most_popular),
                                feed.getMeta_data().getSource_title());
                    } else {
                        return String.format(mActivity.getString(R.string.feed_format_most_popular_in),
                                feed.getMeta_data().getSource_title(), DataUtil.getStateNameFromValue(feed.getState()));
                    }

                case CommentPost:
                    if (feed.getState() == null) {
                        return String.format(
                                mActivity.getString(R.string.feed_format_comment_post),
                                feed.getAction_by(), feed.getMeta_data().getSource_title()
                        );
                    } else {
                        return String.format(
                                mActivity.getString(R.string.feed_format_comment_post_from),
                                feed.getAction_by(), DataUtil.getStateNameFromValue(feed.getState()),
                                feed.getMeta_data().getSource_title()
                        );
                    }

                case Share:
                case SharePostApp:
                case ShareCourse:
                    if (feed.getAction_by() != null) {
                        if (feed.getState() == null) {
                            return String.format(
                                    mActivity.getString(R.string.feed_format_share),
                                    feed.getAction_by(), feed.getMeta_data().getSource_title()
                            );
                        } else {
                            return String.format(
                                    mActivity.getString(R.string.feed_format_share_from),
                                    feed.getAction_by(), DataUtil.getStateNameFromValue(feed.getState()),
                                    feed.getMeta_data().getSource_title()
                            );
                        }
                    } else {
                        if (feed.getState() == null) {
                            return String.format(
                                    mActivity.getString(R.string.feed_format_advanced_share),
                                    String.valueOf(feed.getCount()), feed.getMeta_data().getSource_title()
                            );
                        } else {
                            return String.format(
                                    mActivity.getString(R.string.feed_format_advanced_share_from),
                                    String.valueOf(feed.getCount()), DataUtil.getStateNameFromValue(feed.getState()),
                                    feed.getMeta_data().getSource_title()
                            );
                        }
                    }

                case GenerateCertificate:
                case CertificateGenerate:
                case Certificate:
                    return String.format(
                            mActivity.getString(R.string.feed_format_certificate),
                            feed.getAction_by(), feed.getMeta_data().getText()
                    );

                case Badge:
                    return String.format(
                            mActivity.getString(R.string.feed_format_badge),
                            feed.getAction_by(), feed.getMeta_data().getSource_title()
                    );

                case NewPost:
                    return String.format(
                            mActivity.getString(R.string.feed_format_new_post),
                            feed.getAction_by(), feed.getMeta_data().getSource_title()
                    );

                case Like:
                    if (feed.getState() == null) {
                        return String.format(
                                mActivity.getString(R.string.feed_format_advanced_like),
                                String.valueOf(feed.getCount()), feed.getMeta_data().getSource_title()
                        );
                    } else {
                        return String.format(
                                mActivity.getString(R.string.feed_format_advanced_like_from),
                                String.valueOf(feed.getCount()), DataUtil.getStateNameFromValue(feed.getState()),
                                feed.getMeta_data().getSource_title()
                        );
                    }

                case Comment:
                    if (feed.getState() == null) {
                        return String.format(
                                mActivity.getString(R.string.feed_format_advanced_comment),
                                String.valueOf(feed.getCount()), feed.getMeta_data().getSource_title()
                        );
                    } else {
                        return String.format(
                                mActivity.getString(R.string.feed_format_advanced_comment_from),
                                String.valueOf(feed.getCount()), DataUtil.getStateNameFromValue(feed.getState()),
                                feed.getMeta_data().getSource_title()
                        );
                    }

                case TTAFeed:
                    return feed.getTitle();

                default:
                    return feed.getMeta_data().getSource_title();
            }
        } catch (IllegalArgumentException e) {
            return feed.getMeta_data().getSource_title();
        }

    }

    private String getUserClasses(String tagLabel) {
        StringBuilder builder = new StringBuilder("कक्षाएँ - ");

        if (tagLabel == null || tagLabel.length() == 0) {
            return builder.append("N/A").toString();
        }

        String[] section_tag_list = tagLabel.split(delimiterTagChunks);
        boolean classesAdded = false;

        for (String section_tag : section_tag_list) {
            String[] duet = section_tag.split(delimiterSectionTag);
            if (duet[0].contains("कक्षा")) {
                duet[1] = duet[1].replace(replacementTagSpace, " ");
                builder.append(duet[1]).append(", ");
                classesAdded = true;
            }
        }

        if (classesAdded) {
            return builder.substring(0, builder.length() - 2);
        } else {
            return builder.append("N/A").toString();
        }

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UserFollowingChangedEvent event) {
        if (users.contains(event.getUser())) {
            int position = users.indexOf(event.getUser());
            users.get(position).setFollowed(event.getUser().isFollowed());
            suggestedUsersAdapter.notifyItemChanged(position);
        }
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public class SuggestedUsersAdapter extends MxFiniteAdapter<SuggestedUser> {
        /**
         * Base constructor.
         * Allocate adapter-related objects here if needed.
         *
         * @param context Context needed to retrieve LayoutInflater
         */
        public SuggestedUsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull SuggestedUser model, @Nullable OnRecyclerItemClickListener<SuggestedUser> listener) {
            if (binding instanceof TRowSuggestedTeacherBinding) {
                TRowSuggestedTeacherBinding teacherBinding = (TRowSuggestedTeacherBinding) binding;
                teacherBinding.userName.setText(model.getName());
                setToolTip();
                Glide.with(getContext())
                        .load(model.getProfileImage().getImageUrlLarge())
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(teacherBinding.userImage);

                if (getItemPosition(model) == 0) {
                    if (!mDataManager.getAppPref().isFeedVisited()) {
                        new MxTooltip.Builder(mActivity)
                                .anchorView(teacherBinding.followBtn)
                                .text("अन्य शिक्षको से जुड़ने के लिए फॉलो बटन दबाएँ ")
                                .gravity(Gravity.BOTTOM)
                                .animated(true)
                                .transparentOverlay(true)
                                .arrowDrawable(R.drawable.up_arrow)
                                .build()
                                .show();
//                        ToolTipView.showToolTip(mActivity, "अन्य शिक्षको से जुड़ने के लिए फॉलो बटन दबाएँ ", teacherBinding.followBtn, Gravity.BOTTOM);
                    }
                    mDataManager.getAppPref().setFeedVisited(true);

                }


                if (model.isFollowed()) {
                    teacherBinding.followBtn.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.btn_selector_filled));
                    teacherBinding.followBtn.setTextColor(ContextCompat.getColor(mActivity, R.color.white));
                    teacherBinding.followBtn.setText(mActivity.getString(R.string.following));

                } else {
                    teacherBinding.followBtn.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.btn_selector_hollow));
                    teacherBinding.followBtn.setTextColor(ContextCompat.getColor(mActivity, R.color.primary_cyan));
                    teacherBinding.followBtn.setText(mActivity.getString(R.string.follow));
                }

                teacherBinding.followBtn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                teacherBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

            }
        }
    }

    public class FeedAdapter extends MxInfiniteAdapter<Feed> {
        public FeedAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Feed model, @Nullable OnRecyclerItemClickListener<Feed> listener) {
            if (binding instanceof TRowFeedBinding) {
                TRowFeedBinding feedBinding = (TRowFeedBinding) binding;
                feedBinding.setViewModel(model);
                feedBinding.playIcon.setVisibility(View.GONE);
                feedBinding.feedMetaSubtext.setVisibility(View.GONE);
                feedBinding.feedBtn.setVisibility(View.GONE);
                feedBinding.itemImage.setVisibility(View.GONE);
                feedBinding.itemText.setVisibility(View.GONE);
                feedBinding.feedLikeCommentLayout.setVisibility(View.GONE);

                feedBinding.feedTitle.setText(Html.fromHtml(getFeedTitle(model)));

                if (feedBinding.feedShare.getVisibility() == View.VISIBLE) {
                    if (getItemPosition(model) == 0) {
                        if (!mDataManager.getAppPref().isFeedVisited()) {
                            new MxTooltip.Builder(mActivity)
                                    .anchorView(feedBinding.feedShare)
                                    .text(" सभी के साथ यह सामग्री साझा करने के लिए यहाँ दबायें ")
                                    .gravity(Gravity.BOTTOM)
                                    .animated(true)
                                    .transparentOverlay(true)
                                    .arrowDrawable(R.drawable.up_arrow)
                                    .build()
                                    .show();
                            mDataManager.getAppPref().setFeedVisited(true);
                        }
                    }
                }


                try {
                    switch (Action.valueOf(model.getAction())) {
                        case LikePost:
                        case CourseLike:
                        case MostPopular:
                        case CommentPost:
                        case Share:
                        case SharePostApp:
                        case ShareCourse:
                        case Like:
                        case Comment:

                            Glide.with(getContext())
                                    .load(model.getMeta_data().getIcon())
                                    .placeholder(R.drawable.placeholder_course_card_image)
                                    .into(feedBinding.feedContentImage);
                            feedBinding.feedMetaText.setText(model.getMeta_data().getText());
                            feedBinding.feedBtn.setText(getContext().getString(R.string.view));
                            feedBinding.feedBtn.setVisibility(View.VISIBLE);
                            feedBinding.feedLikeCommentLayout.setVisibility(View.VISIBLE);

                            break;

                        case Certificate:
                        case GenerateCertificate:
                        case CertificateGenerate:

                            Glide.with(getContext())
                                    .load(mDataManager.getConfig().getApiHostURL() +
                                            model.getMeta_data().getUser_icon().getFull())
                                    .placeholder(R.drawable.profile_photo_placeholder)
                                    .into(feedBinding.feedContentImage);
                            feedBinding.feedMetaText.setText(model.getMeta_data().getUser_name());
                            feedBinding.feedMetaSubtext.setText(getUserClasses(model.getMeta_data().getTag_label()));
                            feedBinding.feedMetaSubtext.setVisibility(View.VISIBLE);
                            Glide.with(getContext())
                                    .load(R.drawable.t_image_cert_1)
                                    .into(feedBinding.itemImage);
                            feedBinding.itemImage.setVisibility(View.VISIBLE);

                            break;

                        case Badge:

                            Glide.with(getContext())
                                    .load(mDataManager.getConfig().getApiHostURL() +
                                            model.getMeta_data().getUser_icon().getFull())
                                    .placeholder(R.drawable.profile_photo_placeholder)
                                    .into(feedBinding.feedContentImage);
                            feedBinding.feedMetaText.setText(model.getMeta_data().getUser_name());
                            feedBinding.feedMetaSubtext.setText(getUserClasses(model.getMeta_data().getTag_label()));
                            feedBinding.feedMetaSubtext.setVisibility(View.VISIBLE);
                            feedBinding.itemImage.setImageResource(BadgeHelper.getBadgeIcon(
                                    BadgeType.valueOf(model.getMeta_data().getSource_name())));
                            feedBinding.itemImage.setVisibility(View.VISIBLE);
                            feedBinding.itemText.setText(model.getMeta_data().getSource_title());
                            feedBinding.itemText.setVisibility(View.VISIBLE);

                            break;

                        case NewPost:

                            Glide.with(getContext())
                                    .load(model.getMeta_data().getIcon())
                                    .placeholder(R.drawable.placeholder_course_card_image)
                                    .into(feedBinding.feedContentImage);
                            feedBinding.feedMetaText.setText(model.getMeta_data().getText());
                            break;

                        case TTAFeed:

                            if (model.getMeta_data().getSource_name().equalsIgnoreCase(Action.appupdate.name())) {
                                Glide.with(getContext())
                                        .load(R.drawable.tta_launcher_foreground)
                                        .into(feedBinding.feedContentImage);
                            } else {
                                Glide.with(getContext())
                                        .load(model.getMeta_data().getIcon())
                                        .placeholder(R.drawable.placeholder_course_card_image)
                                        .into(feedBinding.feedContentImage);
                            }
                            feedBinding.feedMetaText.setText(model.getMessage());
                            break;

                    }
                } catch (IllegalArgumentException e) {
                    Glide.with(getContext())
                            .load(model.getMeta_data().getIcon())
                            .placeholder(R.drawable.placeholder_course_card_image)
                            .into(feedBinding.feedContentImage);
                    feedBinding.feedMetaText.setText(model.getMeta_data().getText());
                }

                if (model.getMeta_data().isLiked()) {
                    feedBinding.feedLikeImage.setImageResource(R.drawable.t_icon_like_filled);
                } else {
                    feedBinding.feedLikeImage.setImageResource(R.drawable.t_icon_like);
                }

                if (model.getMeta_data().getShare_url() != null) {
                    feedBinding.feedShare.setVisibility(View.VISIBLE);
                    feedBinding.feedShare.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onItemClick(v, model);
                        }
                    });
                } else {
                    feedBinding.feedShare.setVisibility(View.GONE);
                }

                feedBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                /*feedBinding.metaContent.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                feedBinding.feedLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });*/

                feedBinding.feedCommentLayout.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

            } else if (binding instanceof TRowFeedWithUserBinding) {
                TRowFeedWithUserBinding feedWithUserBinding = (TRowFeedWithUserBinding) binding;
                feedWithUserBinding.setViewModel(model);

                feedWithUserBinding.feedTitle.setText(Html.fromHtml(getFeedTitle(model)));
                Glide.with(getContext())
                        .load(mDataManager.getConfig().getApiHostURL() +
                                model.getMeta_data().getUser_icon().getLarge())
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(feedWithUserBinding.feedUserImage);
                feedWithUserBinding.feedUserName.setText(model.getMeta_data().getUser_name());
                feedWithUserBinding.feedUserClasses.setText(getUserClasses(model.getMeta_data().getTag_label()));
                feedWithUserBinding.feedContentTitle.setText(model.getMeta_data().getText());

                try {
                    switch (Action.valueOf(model.getAction())) {
                        case DBComment:

                            feedWithUserBinding.feedContentImage.setVisibility(View.GONE);
                            feedWithUserBinding.feedContentViewBtn.setText(getContext().getString(R.string.view_discussion));

                            break;
                        default:
                            feedWithUserBinding.feedContentImage.setVisibility(View.VISIBLE);
                            feedWithUserBinding.feedContentViewBtn.setText(getContext().getString(R.string.view));
                            Glide.with(getContext())
                                    .load(model.getMeta_data().getIcon())
                                    .placeholder(R.drawable.placeholder_course_card_image)
                                    .into(feedWithUserBinding.feedContentImage);
                    }
                } catch (IllegalArgumentException e) {
                    feedWithUserBinding.feedContentViewBtn.setText(getContext().getString(R.string.view));
                }

                if (model.getMeta_data().isLiked()) {
                    feedWithUserBinding.feedLikeImage.setImageResource(R.drawable.t_icon_like_filled);
                } else {
                    feedWithUserBinding.feedLikeImage.setImageResource(R.drawable.t_icon_like);
                }

                feedWithUserBinding.metaUserLayout.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                if (model.getMeta_data().getShare_url() != null) {
                    feedWithUserBinding.feedShare.setVisibility(View.VISIBLE);
                    feedWithUserBinding.feedShare.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onItemClick(v, model);
                        }
                    });
                } else {
                    feedWithUserBinding.feedShare.setVisibility(View.GONE);
                }

                feedWithUserBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                /*feedWithUserBinding.metaContent.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                feedWithUserBinding.feedLikeLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });

                feedWithUserBinding.feedCommentLayout.setOnClickListener(v -> {
                    if (listener != null){
                        listener.onItemClick(v, model);
                    }
                });*/

            }
        }

        @Override
        public int getItemLayout(int position) {

            if (isLoadMoreDisplayable() && position == getItemCount() - 1) {
                return getLoadingMoreLayout();
            } else {
                try {
                    switch (Action.valueOf(getItem(position).getAction())) {

                        case LikePost:
                        case MostPopular:
                        case CommentPost:
                        case Share:
                        case SharePostApp:
                        case NewPost:
                        case DBComment:
                        case DBLike:
                        case Like:
                        case Comment:
                        case TTAFeed:
                            if (getItem(position).getMeta_data() != null &&
                                    getItem(position).getMeta_data().getUser_name() != null) {
                                return R.layout.t_row_feed_with_user;
                            }

                        default:
                            return R.layout.t_row_feed;

                    }
                } catch (IllegalArgumentException e) {
                    return R.layout.t_row_feed;
                }
            }
        }


    }
}
