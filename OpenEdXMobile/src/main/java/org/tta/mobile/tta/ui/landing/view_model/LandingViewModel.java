package org.tta.mobile.tta.ui.landing.view_model;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.tta.mobile.R;
import org.tta.mobile.event.NetworkConnectivityChangeEvent;
import org.tta.mobile.module.db.DbStructure;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.appupdate.model.UpdateType;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.model.UpdateResponse;
import org.tta.mobile.tta.event.ContentStatusReceivedEvent;
import org.tta.mobile.tta.event.ContentStatusesReceivedEvent;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.scorm.Migration280719;
import org.tta.mobile.tta.tutorials.MxTooltip;
import org.tta.mobile.tta.ui.agenda.AgendaFragment;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.feed.FeedFragment;
import org.tta.mobile.tta.ui.library.LibraryFragment;
import org.tta.mobile.tta.ui.profile.ProfileFragment;
import org.tta.mobile.tta.ui.search.SearchFragment;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
                if (!mDataManager.getAppPref().isProfileVisited()) {
                    new MxTooltip.Builder(mActivity)
                            .anchorView(mActivity.findViewById(R.id.action_library))
                            .text(R.string.library_tab)
                            .gravity(Gravity.TOP)
                            .animated(true)
                            .transparentOverlay(true)
                            .arrowDrawable(R.drawable.down_arrow)
                            .build()
                            .show();
                }
                return true;
            case R.id.action_feed:
                selectedId = R.id.action_feed;
                showFeed();
                if (!mDataManager.getAppPref().isFeedNavVisited()) {
                    new MxTooltip.Builder(mActivity)
                            .anchorView(mActivity.findViewById(R.id.action_feed))
                            .text(getActivity().getResources().getString(R.string.feed_tab))
                            .gravity(Gravity.TOP)
                            .animated(true)
                            .transparentOverlay(true)
                            .arrowDrawable(R.drawable.down_arrow)
                            .build()
                            .show();
                    mDataManager.getAppPref().setFeedNavVisited(true);
                }
                return true;
            case R.id.action_search:
                selectedId = R.id.action_search;
                if (!mDataManager.getAppPref().isSearchVisited()) {
                    new MxTooltip.Builder(mActivity)
                            .anchorView(mActivity.findViewById(R.id.action_search))
                            .text(getActivity().getResources().getString(R.string.search_tab))
                            .gravity(Gravity.TOP)
                            .animated(true)
                            .transparentOverlay(true)
                            .arrowDrawable(R.drawable.down_arrow)
                            .build()
                            .show();
                }
                showSearch();
                return true;
            case R.id.action_agenda:
                selectedId = R.id.action_agenda;
                if (!mDataManager.getAppPref().isAgendaVisited()) {
                    new MxTooltip.Builder(mActivity)
                            .anchorView(mActivity.findViewById(R.id.action_agenda))
                            .text(getActivity().getResources().getString(R.string.agenda_tab))
                            .gravity(Gravity.TOP)
                            .animated(true)
                            .transparentOverlay(true)
                            .arrowDrawable(R.drawable.down_arrow)
                            .build()
                            .show();
                }
                showAgenda();
                return true;
            case R.id.action_profile:
                selectedId = R.id.action_profile;

                showProfile();
                return true;
            default:
                selectedId = R.id.action_library;

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

        /*if (mDataManager.checkUpdate()) {
            if (!Constants.IsUpdateDelay)
                getAppUpdate();
        } else {
            UpdateResponse res = mDataManager.getLoginPrefs().getLatestAppInfo();
            if (!Constants.IsUpdateDelay) {
                if (res.getVersion_code() > mDataManager.getCurrent_vCode()) {
                    decideUpdateUI(res);
                }
            }
        }*/
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

    public void showSearch() {
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

    private void onAppStart() {
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

        Migration280719 spt = new Migration280719(getActivity(), mDataManager);
//        spt.MigrateScromPackages();
        spt.deleteScormPackages();
        spt.MigrateConnectVideos();

        mDataManager.setContentIdForLegacyDownloads();
        mDataManager.startNotificationService();
        mDataManager.createPendingCertificatesNotification();
    }

    private void setToolTip() {
        if (!mDataManager.getAppPref().isProfileVisited()) {
            libraryToolTip.set(mActivity.getString(R.string.library_tab));
            toolTipGravity.set(Gravity.TOP);
            toolTipPosition.set(0);
//            mDataManager.getAppPref().setProfileVisited(true);
        }
    }

    public void selectLibrary() {
        selectedId = R.id.action_library;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (NetworkUtil.isConnected(mActivity)) {
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ContentStatusReceivedEvent event) {
        if (statuses == null) {
            statuses = new ArrayList<>();
        }
        ContentStatus contentStatus = event.getContentStatus();
        if (statuses.contains(contentStatus)) {
            ContentStatus prev = statuses.get(statuses.indexOf(contentStatus));
            if (prev.getCompleted() == null && contentStatus.getCompleted() != null) {
                prev.setCompleted(contentStatus.getCompleted());
            }
            if (prev.getStarted() == null && contentStatus.getStarted() != null) {
                prev.setStarted(contentStatus.getStarted());
            }
        } else {
            statuses.add(contentStatus);
        }
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    //hit the api one a day
    //store the latest version info in login pref
    //refresh latest version info every day.
    //now check the show update panel by checking the current and api latest version app
    private void getAppUpdate() {
        mDataManager.getUpdatedVersion(new OnResponseCallback<UpdateResponse>() {
            @Override
            public void onSuccess(UpdateResponse res) {
                if(res==null|| res.version_code==null)
                    return;

                //set last update date time
                mDataManager.getAppPref().setUpdateSeenDate(Calendar.getInstance().getTime().toString());

                //store latest version info ,in-case user go to play store and come back without update.
                mDataManager.getLoginPrefs().storeLatestAppInfo(res);
                if (res.getVersion_code()>mDataManager.getCurrent_vCode()) {
                    decideUpdateUI(res);
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        },mDataManager.getCurrentV_name(),mDataManager.getCurrent_vCode());
    }

    private void decideUpdateUI(UpdateResponse res)
    {
        if(res==null || res.version_code==null)
            return;

        String mFinal_notes = new String();
        if(res.getRelease_note()==null || res.getRelease_note().isEmpty())
            mFinal_notes=Constants.DefaultUpdateMessage;
        else
            mFinal_notes=res.getRelease_note();

        if (res.getStatus().toLowerCase().equals(UpdateType.FLEXIBLE.toString().toLowerCase())) {
            showFlexibleUpdate(mFinal_notes);

        } else if (res.getStatus().toLowerCase().equals(UpdateType.IMMEDIATE.toString().toLowerCase())) {
            showImmediateUpdate(mFinal_notes);
        }
    }

    private void showFlexibleUpdate(String notes_html) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater i = mActivity.getLayoutInflater();

        View v = i.inflate(R.layout.alert_dialog_layout, null);
        TextView title = v.findViewById(R.id.title);
        title.setText("नया अपडेट उपलब्ध हैं |");

        Button mbtn_delay = v.findViewById(R.id.btn_delay);
        Button mbtn_update = v.findViewById(R.id.btn_update);

        WebView flxible_notes_wv=v.findViewById(R.id.notes_flxible_wv);

        flxible_notes_wv.loadData(notes_html,
                "text/html", "UTF-8");

        builder.setView(v);
        AlertDialog dialog = builder.create();

        mbtn_delay.setOnClickListener(v1 -> {
            Constants.IsUpdateDelay = true;
            dialog.dismiss();
        });

        mbtn_update.setOnClickListener(v12 -> {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
            dialog.dismiss();
        });

        builder.setCancelable(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void showImmediateUpdate(String notes_html) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater i = mActivity.getLayoutInflater();

        View v = i.inflate(R.layout.alert_dialog_full_screen, null);
        WebView immediate_notes_wv=v.findViewById(R.id.immediate_notes_wv);

        immediate_notes_wv.loadData(notes_html,
                "text/html", "UTF-8");

        TextView title = v.findViewById(R.id.tv_title);
        title.setText("नया अपडेट उपलब्ध हैं |");

        Button mbtn_update = v.findViewById(R.id.btn_update);
        ImageView miv_close = v.findViewById(R.id.iv_close);

        builder.setView(v);
        AlertDialog dialog = builder.create();

        mbtn_update.setOnClickListener(v12 -> {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
            dialog.dismiss();
        });

        miv_close.setOnClickListener(v1 -> {
            mActivity.finishAffinity();
            System.exit(0);
        });

        builder.setCancelable(false);
        dialog.setCancelable(false);
        dialog.show();
    }
}