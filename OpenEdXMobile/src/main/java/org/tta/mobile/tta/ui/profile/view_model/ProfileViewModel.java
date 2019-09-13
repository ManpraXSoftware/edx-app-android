package org.tta.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.view.Gravity;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.model.search.FilterSection;
import org.tta.mobile.tta.data.model.search.SearchFilter;
import org.tta.mobile.tta.event.UserFollowingChangedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.TaBaseFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.profile.FollowersFragment;
import org.tta.mobile.tta.ui.profile.FollowingFragment;
import org.tta.mobile.tta.ui.profile.MyCertificatesFragment;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.user.Account;
import org.tta.mobile.user.ProfileImage;
import org.tta.mobile.util.BrowserUtil;
import org.tta.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class ProfileViewModel extends BaseViewModel {

    public ProfileModel profileModel;
    public ProfileImage profileImage;
    public Account account;
    public SearchFilter searchFilter;
//    public BadgeListAdapter adapter;

    public ObservableInt userImagePlaceholder = new ObservableInt(R.drawable.profile_photo_placeholder);
    public ObservableField<String> classes = new ObservableField<>();
    public ObservableField<String> skills = new ObservableField<>();
    public ObservableField<String> following = new ObservableField<>();
    public ObservableField<String> followers = new ObservableField<>();
    public ObservableField<String> userImageUrl = new ObservableField<>();
    public ObservableField<String> nCertificates = new ObservableField<>("0");
    public ObservableField<String> profiletoolTip = new ObservableField<>("");
    public ObservableInt profiletoolTipGravity = new ObservableInt(0);

    private boolean accountReceived, filtersReceived;
    private String tagLabel;
    private String delimiterTagChunks, delimiterSectionTag, replacementTagSpace;

    public ProfileViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        delimiterTagChunks = Constants.DELIMITER_TAG_CHUNKS;
        delimiterSectionTag = Constants.DELIMITER_SECTION_TAG;
        replacementTagSpace = Constants.REPLACEMENT_TAG_SPACE;
//        adapter = new BadgeListAdapter(context);
        fetchAccount();
        fetchFilters();
        fetchCertificates();
//        showToolTip();
//        adapter.setItems(setbadge());

    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromLocal();
        setDetails();
//        showToolTip();
    }

    /*private List<Badge> setbadge(){
        List<Badge> list= new ArrayList<>();

        Badge badge1 = new Badge();
        badge1.setBadgeName("शिक्षक सितारा");
        badge1.setType(BadgeType.star_teacher.name());
        list.add(badge1);

        Badge badge2 = new Badge();
        badge2.setBadgeName("जिज्ञासु");
        badge2.setType(BadgeType.inquisitive.name());
        list.add(badge2);

        Badge badge3 = new Badge();
        badge3.setBadgeName("जागरूक श्रोता");
        badge3.setType(BadgeType.aware_listener.name());
        list.add(badge3);

        Badge badge4 = new Badge();
        badge4.setBadgeName("प्रमाणपत्र");
        badge4.setType(BadgeType.certificate.name());
        list.add(badge4);

        Badge badge5 = new Badge();
        badge5.setBadgeName("मूल्यांकन");
        badge5.setType(BadgeType.evaluator.name());
        list.add(badge5);

        Badge badge6 = new Badge();
        badge6.setBadgeName("प्रशंसक");
        badge6.setType(BadgeType.fan.name());
        list.add(badge6);

        Badge badge7 = new Badge();
        badge7.setBadgeName("रायशुमार");
        badge7.setType(BadgeType.opinion.name());
        list.add(badge7);

        Badge badge8 = new Badge();
        badge8.setBadgeName("उस्ताद");
        badge8.setType(BadgeType.master.name());
        list.add(badge8);

        return list;
    }*/


    private void refreshFromLocal() {
        profileModel = loginPrefs.getCurrentUserProfile();
        profileImage = loginPrefs.getProfileImage();
        if (profileModel != null) {
            tagLabel = profileModel.getTagLabel();
            followers.set(String.valueOf(profileModel.getFollowers()));
            following.set(String.valueOf(profileModel.getFollowing()));
        }
        if (profileImage != null) {
            userImageUrl.set(profileImage.getImageUrlFull());
        }
    }

    private void fetchFilters() {

        mDataManager.getSearchFilter(new OnResponseCallback<SearchFilter>() {
            @Override
            public void onSuccess(SearchFilter data) {
                filtersReceived = true;
                hideLoading();
                searchFilter = data;
                if (accountReceived) {
                    setDetails();
                }
            }

            @Override
            public void onFailure(Exception e) {
                filtersReceived = true;
                hideLoading();
                if (accountReceived) {
                    setDetails();
                }
//                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void fetchAccount() {
        mActivity.showLoading();
        mDataManager.getAccount(new OnResponseCallback<Account>() {
            @Override
            public void onSuccess(Account data) {
                accountReceived = true;
                hideLoading();
                account = data;
                refreshFromLocal();
                if (account != null) {
                    tagLabel = account.getTagLabel();
                }
                if (filtersReceived) {
                    setDetails();
                }
            }

            @Override
            public void onFailure(Exception e) {
                accountReceived = true;
                if (filtersReceived) {
                    setDetails();
                }
                hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    /*public void onCliCkMoreButton(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                AllBadgesFragment.newInstance(),
                R.id.dashboard_fragment,
                AllBadgesFragment.TAG,
                true,
                null);
    }
    public void onCliCkLl(){
        ActivityUtil.replaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                PointsFragment.newInstance(),
                R.id.dashboard_fragment,
                PointsFragment.TAG,
                true,
                null);
    }*/

    private void fetchCertificates() {

        mDataManager.getMyCertificates(new OnResponseCallback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> data) {
                nCertificates.set(String.valueOf(data.size()));
            }

            @Override
            public void onFailure(Exception e) {
                //Do nothing
            }
        });

    }

    public void showToolTip(){
        if (!mDataManager.getAppPref().isSettingVisited()) {
//            ToolTipView.showToolTip(view.getContext(), "सेटिंग्स ऑप्शन के लिए यह बटन दबायें ", optionsBtn, Gravity.BOTTOM);
            profiletoolTip.set("सेटिंग्स में जाने के लिए यह बटन दबाएँ।");
            profiletoolTipGravity.set(Gravity.BOTTOM);
            mDataManager.getAppPref().setSettingsVisited(true);
        }
    }

    public void showCertificates() {
        if (nCertificates.get() != null && !nCertificates.get().equals("") && !nCertificates.get().equals("0")) {
            ActivityUtil.replaceFragmentInActivity(mActivity.getSupportFragmentManager(),
                    new MyCertificatesFragment(),
                    R.id.dashboard_fragment,
                    MyCertificatesFragment.TAG,
                    true,
                    null);
        }
    }

    public void showFollowers(){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        if (followers.get() != null && !followers.get().equals("") && !followers.get().equals("0")) {
            ActivityUtil.replaceFragmentInActivity(mActivity.getSupportFragmentManager(),
                    new FollowersFragment(),
                    R.id.dashboard_fragment,
                    FollowersFragment.TAG,
                    true,
                    null);
        }
    }

    public void showFollowing(){
        if (!NetworkUtil.isConnected(mActivity)){
            mActivity.showLongSnack(mActivity.getString(R.string.no_connection_exception));
            return;
        }
        if (following.get() != null && !following.get().equals("") && !following.get().equals("0")) {
            ActivityUtil.replaceFragmentInActivity(mActivity.getSupportFragmentManager(),
                    new FollowingFragment(),
                    R.id.dashboard_fragment,
                    FollowingFragment.TAG,
                    true,
                    null);
        }
    }

    public void showPrivacyPolicy(){
        mDataManager.getEdxEnvironment().getRouter().showAuthenticatedWebviewActivity(
                mActivity, BrowserUtil.config.getApiHostURL()+"privacy",
                mActivity.getString(R.string.privacy_policy)
        );
    }

    private void setDetails() {
        classes.set("");
        skills.set("");
        if ((account == null && profileModel == null) ||
                searchFilter == null || searchFilter.getResult() == null) {
            return;
        }

        String[] section_tag_list;

        if (tagLabel == null || tagLabel.length() == 0) {
            return;
        }

        try {
            section_tag_list = tagLabel.split(delimiterTagChunks);

            Map<String, List<String>> sectionTagsMap = new HashMap<>();
            for (String section_tag : section_tag_list) {
                String[] duet = section_tag.split(delimiterSectionTag);
                if (!sectionTagsMap.containsKey(duet[0])) {
                    sectionTagsMap.put(duet[0], new ArrayList<>());
                }
                sectionTagsMap.get(duet[0]).add(duet[1]);
            }

            for (FilterSection section : searchFilter.getResult()) {
                if (section.isIn_profile()) {
                    if (sectionTagsMap.containsKey(section.getName())) {
                        StringBuilder builder = new StringBuilder();
                        for (String tag : sectionTagsMap.get(section.getName())) {
                            tag = tag.replace(replacementTagSpace, " ");
                            builder.append(tag + ", ");
                        }
                        if (builder.length() > 0) {
                            builder.deleteCharAt(builder.length() - 1);
                            builder.deleteCharAt(builder.length() - 1);
                        }

                        if (section.getName().contains("कक्षा")) {
                            classes.set(builder.toString());
                        } else if (section.getName().contains("क्षमता")) {
                            skills.set(builder.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, ProfileViewModel.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "setDetails");
            parameters.putString(Constants.KEY_DATA, "Taglabel = " + tagLabel);
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }

    }

    public void logout() {
        mDataManager.logout();
    }

    private void hideLoading() {
        if (accountReceived && filtersReceived) {
            mActivity.hideLoading();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UserFollowingChangedEvent event) {
        profileModel.toggleFollowing(event.getUser().isFollowed());
        mDataManager.getLoginPrefs().setCurrentUserProfileInCache(profileModel);
        refreshFromLocal();
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    /*public class BadgeListAdapter extends MxFiniteAdapter<Badge> {

        public BadgeListAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Badge model, @Nullable OnRecyclerItemClickListener<Badge> listener) {
            if (binding instanceof TRowBadgeBinding) {
                TRowBadgeBinding tRowBadgeBinding = (TRowBadgeBinding) binding;
                tRowBadgeBinding.tvbadgename.setText(model.getBadgeName());
                tRowBadgeBinding.ivBadgeimage.setImageResource(BadgeHelper.getBadgeIcon(BadgeType.valueOf(model.getType())));
                tRowBadgeBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }*/

}
