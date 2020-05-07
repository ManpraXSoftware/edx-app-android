package org.tta.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.tta.mobile.R;
import org.tta.mobile.databinding.TRowSuggestedTeacherGridBinding;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.model.StatusResponse;
import org.tta.mobile.tta.data.model.feed.SuggestedUser;
import org.tta.mobile.tta.event.UserFollowingChangedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.profile.OtherProfileActivity;
import org.tta.mobile.tta.utils.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class FollowersViewModel extends BaseViewModel {

    private static final int DEFAULT_TAKE = 10;
    private static final int DEFAULT_SKIP = 0;

    public UsersAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ObservableBoolean emptyVisible = new ObservableBoolean();

    private List<SuggestedUser> users;
    private int take, skip;
    private boolean allLoaded;

    public MxInfiniteAdapter.OnLoadMoreListener loadMoreListener = page -> {
        if (allLoaded)
            return false;
        this.skip++;
        getUsers();
        return true;
    };

    public FollowersViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        users = new ArrayList<>();
        take = DEFAULT_TAKE;
        skip = DEFAULT_SKIP;
        allLoaded = false;

        adapter = new UsersAdapter(mActivity);
        adapter.setItems(users);
        adapter.setItemClickListener((view, item) -> {
            switch (view.getId()) {
                case R.id.follow_btn:
                    mActivity.showLoading();

                    mDataManager.followUnfollowUser(item.getUsername(), new OnResponseCallback<StatusResponse>() {
                        @Override
                        public void onSuccess(StatusResponse data) {
                            mActivity.hideLoading();
                            item.setFollowed(data.getStatus());
                            adapter.notifyItemChanged(adapter.getItemPosition(item));
                            EventBus.getDefault().post(new UserFollowingChangedEvent(item));

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
                    Bundle parameters = new Bundle();
                    parameters.putString(Constants.KEY_USERNAME, item.getUsername());
                    ActivityUtil.gotoPage(mActivity, OtherProfileActivity.class, parameters);
            }
        });

        mActivity.showLoading();
        getUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void getUsers() {

        mDataManager.getFollowersOrFollowing(true, take, skip, new OnResponseCallback<List<SuggestedUser>>() {
            @Override
            public void onSuccess(List<SuggestedUser> data) {
                mActivity.hideLoading();
                if (data.size() < take) {
                    allLoaded = true;
                }
                populateUsers(data);
                adapter.setLoadingDone();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                allLoaded = true;
                adapter.setLoadingDone();
                toggleEmptyVisibility();
            }
        });

    }

    private void populateUsers(List<SuggestedUser> data) {
        boolean newItemsAdded = false;
        int n = 0;
        for (SuggestedUser user : data) {
            if (!users.contains(user)) {
                users.add(user);
                newItemsAdded = true;
                n++;
            }
        }
        if (newItemsAdded) {
            adapter.notifyItemRangeInserted(users.size() - n, n);
        }

        toggleEmptyVisibility();
    }

    private void toggleEmptyVisibility() {
        if (users == null || users.isEmpty()) {
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UserFollowingChangedEvent event) {
        if (users.contains(event.getUser())) {
            int position = users.indexOf(event.getUser());
            users.get(position).setFollowed(event.getUser().isFollowed());
            adapter.notifyItemChanged(position);
        }
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public class UsersAdapter extends MxInfiniteAdapter<SuggestedUser> {

        public UsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull SuggestedUser model, @Nullable OnRecyclerItemClickListener<SuggestedUser> listener) {
            if (binding instanceof TRowSuggestedTeacherGridBinding) {
                TRowSuggestedTeacherGridBinding teacherBinding = (TRowSuggestedTeacherGridBinding) binding;
                teacherBinding.setViewModel(model);
                Glide.with(getContext())
                        .load(model.getProfileImage().getImageUrlLarge())
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(teacherBinding.userImage);

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
}
