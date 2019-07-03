package org.tta.mobile.tta.ui.landing.view_model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.TooltipCompat;
import android.view.Gravity;
import android.view.MenuItem;

import com.facebook.login.widget.ToolTipPopup;

import org.tta.mobile.R;
import org.tta.mobile.event.NetworkConnectivityChangeEvent;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.event.ContentStatusReceivedEvent;
import org.tta.mobile.tta.event.ContentStatusesReceivedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.scorm.Migration280719;
import org.tta.mobile.tta.ui.agenda.AgendaFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.feed.FeedFragment;
import org.tta.mobile.tta.ui.library.LibraryFragment;
import org.tta.mobile.tta.ui.profile.ProfileFragment;
import org.tta.mobile.tta.ui.search.SearchFragment;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.ToolTipView;
import org.tta.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class LandingViewModel extends BaseViewModel {

    private int selectedId = R.id.action_library;
    private MenuItem menuItem;

    public ObservableBoolean navShiftMode = new ObservableBoolean();
    public ObservableBoolean offlineVisible = new ObservableBoolean();

    private List<ContentStatus> statuses;

    public ObservableField<String> libraryToolTip = new ObservableField<>();
    public ObservableInt toolTipGravity = new ObservableInt();
    public ObservableInt toolTipPosition = new ObservableInt();


    public BottomNavigationView.OnNavigationItemSelectedListener itemSelectedListener = item -> {
        menuItem = item;
        if (item.getItemId() == selectedId) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_library:
                showLibrary();
                selectedId = R.id.action_library;
//                ToolTipView.showToolTip(getActivity(), "यहाँ सभी सामग्री पाए",mActivity.findViewById(R.id.action_library),Gravity.TOP);
                if (!mDataManager.getAppPref().isProfileVisited()) {
                    ToolTipView.showToolTip(getActivity(), "  यहाँ सभी सामग्री पाएँ  "  , mActivity.findViewById(R.id.action_library), Gravity.TOP);
                }
                return true;
            case R.id.action_feed:
                selectedId = R.id.action_feed;
                showFeed();
                if (!mDataManager.getAppPref().isFeedVisited()) {
                    ToolTipView.showToolTip(getActivity(), getActivity().getResources().getString(R.string.feed_tab), mActivity.findViewById(R.id.action_feed), Gravity.TOP);
                }
                return true;
            case R.id.action_search:
                selectedId = R.id.action_search;
                if (!mDataManager.getAppPref().isSearchVisited()) {
                    ToolTipView.showToolTip(getActivity(),getActivity().getResources().getString(R.string.search_tab) , mActivity.findViewById(R.id.action_search), Gravity.TOP);
                }
                showSearch();
                return true;
            case R.id.action_agenda:
                selectedId = R.id.action_agenda;
                if (!mDataManager.getAppPref().isAgendaVisited()) {
                    ToolTipView.showToolTip(getActivity(), getActivity().getResources().getString(R.string.agenda_tab), mActivity.findViewById(R.id.action_agenda), Gravity.TOP);
                }
                showAgenda();
                return true;
            case R.id.action_profile:
                selectedId = R.id.action_profile;
//                if (!mDataManager.getAppPref().isProfileVisited()) {
//                    ToolTipView.showToolTip(getActivity(), "रूपरेखा",mActivity.findViewById(R.id.action_profile),Gravity.TOP);
//                }
                showProfile();
                return true;
            default:
                selectedId = R.id.action_library;

//                ToolTipView.showToolTip(getActivity(), "यहाँ सभी सामग्री पाए",mActivity.findViewById(R.id.action_library),Gravity.TOP);
                showLibrary();
                return true;
        }
    };

    public LandingViewModel(BaseVMActivity activity) {
        super(activity);
        mDataManager.setWpProfileCache();
        navShiftMode.set(false);
        selectedId = R.id.action_library;
        statuses = new ArrayList<>();
        showLibrary();
        onAppStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    public void showLibrary() {
        ActivityUtil.clearBackstackAndReplaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                LibraryFragment.newInstance(() -> selectedId = R.id.action_search),
                R.id.dashboard_fragment,
                LibraryFragment.TAG,
                false,
                null
        );
        setToolTip();
    }

    public void showFeed() {
        ActivityUtil.clearBackstackAndReplaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new FeedFragment(),
                R.id.dashboard_fragment,
                FeedFragment.TAG,
                false,
                null
        );
    }

    public void showSearch(){
        ActivityUtil.clearBackstackAndReplaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new SearchFragment(),
                R.id.dashboard_fragment,
                SearchFragment.TAG,
                false,
                null
        );
    }

    public void showAgenda() {
        ActivityUtil.clearBackstackAndReplaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new AgendaFragment(),
                R.id.dashboard_fragment,
                AgendaFragment.TAG,
                false,
                null
        );
    }

    public void showProfile() {
        ActivityUtil.clearBackstackAndReplaceFragmentInActivity(
                mActivity.getSupportFragmentManager(),
                new ProfileFragment(),
                R.id.dashboard_fragment,
                ProfileFragment.TAG,
                false,
                null
        );
    }

    private void onAppStart(){
        mDataManager.getMyContentStatuses(new OnResponseCallback<List<ContentStatus>>() {
            @Override
            public void onSuccess(List<ContentStatus> data) {
                statuses.addAll(data);
                EventBus.getDefault().postSticky(new ContentStatusesReceivedEvent(statuses));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });


        Migration280719 spt=new Migration280719(getActivity());
        spt.MigrateScromPackages();
        spt.MigrateConnectVideos();

        mDataManager.setContentIdForLegacyDownloads();
    }

    public void setToolTip() {
        if (!mDataManager.getAppPref().isProfileVisited()) {
            libraryToolTip.set("  यहाँ सभी सामग्री पाएँ  ");
            toolTipGravity.set(Gravity.TOP);
            toolTipPosition.set(0);
//            mDataManager.getAppPref().setProfileVisited(true);
        }
    }

    public void selectLibrary(){
        selectedId = R.id.action_library;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event){
        if (NetworkUtil.isConnected(mActivity)){
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event){
        if (statuses == null){
            statuses = new ArrayList<>();
        }
        ContentStatus contentStatus = event.getContentStatus();
        if (statuses.contains(contentStatus)){
            ContentStatus prev = statuses.get(statuses.indexOf(contentStatus));
            if (prev.getCompleted() == null && contentStatus.getCompleted() != null){
                prev.setCompleted(contentStatus.getCompleted());
            }
            if (prev.getStarted() == null && contentStatus.getStarted() != null){
                prev.setStarted(contentStatus.getStarted());
            }
        } else {
            statuses.add(contentStatus);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }
}
