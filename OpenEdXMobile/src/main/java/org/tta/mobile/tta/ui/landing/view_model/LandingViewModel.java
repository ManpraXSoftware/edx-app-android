package org.tta.mobile.tta.ui.landing.view_model;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.support.design.widget.BottomNavigationView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.tta.mobile.R;
import org.tta.mobile.event.NetworkConnectivityChangeEvent;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private String latestVersion, currentVersion;
    private Date current_date, lastSeenDate;
    private int version_code;
    private long elapsedDays;


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
                            .text("यहाँ सभी सामग्री पाएँ")
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
        getCurrentVersion();
//        if (lastSeenDate==null)
        if (!Constants.IsUpdateDelay)
            getAppUpdate();
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


        Migration280719 spt = new Migration280719(getActivity());
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

    private void getAppUpdate() {
        latestVersion = "3.12.2";
        if (!latestVersion.equals(currentVersion)) {

            showCustomAlertDialog();
////            if (data.type.equals("flexible")){
//            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
////            builder.setIcon(R.drawable.teacherapplogo);
//            builder.setPositiveButton("ऐप्प अपडेट करें", (dialog, which) -> {
//                //Click button action
//                mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
//                        Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
//                dialog.dismiss();
//            });
//
//
//            builder.setNegativeButton("बाद में अपडेट करें", (dialog, which) -> {
//                Constants.IsUpdateDelay = true;
//                mDataManager.getAppPref().setUpdateSeenDate(current_date.toString());
//                dialog.dismiss();
//            });
//            builder.setCancelable(false);
//
//
//            LayoutInflater i = mActivity.getLayoutInflater();
//
//            View v = i.inflate(R.layout.alert_dialog_layout,null);
//
//            TextView title = v.findViewById(R.id.title);
//            title.setText("अपडेट मौजूद हैं, कृपया ऐप्प को अपडेट करें ");
//
//            TextView update = v.findViewById(R.id.showUpdate);
//            update.setText(R.string.update_tta);
//
//            builder.setView(v);
//            builder.show();

        }
    }

//        mDataManager.getUpdatedVersion(new OnResponseCallback<UpdatedVersionResponse>() {
//            @Override
//            public void onSuccess(UpdatedVersionResponse data) {
//                if (!data.updated_version.equals(currentVersion)){
//                    if (data.type.equals("flexible")){
//                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppBaseTheme);
//                        builder.setIcon(R.drawable.teacherapplogo);
//                        builder.setTitle(data.release_note);
//                        builder.setPositiveButton("Update", (dialog, which) -> {
//                            //Click button action
//                            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
//                                    Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
//                            dialog.dismiss();
//                        });
//
//                        builder.setNegativeButton("Delay", (dialog, which) -> {
//                            mDataManager.getAppPref().setUpdateSeenDate(current_date.toString());
//                            dialog.dismiss();
//                        });
//                        builder.setCancelable(false);
//                        builder.show();
//                    }else if (data.updated_version.equals("immediate")){
//                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppBaseTheme);
//                        builder.setIcon(R.drawable.teacherapplogo);
//                        builder.setTitle("An Update is Available..");
//                        builder.setPositiveButton("Update", (dialog, which) -> {
//                            //Click button action
//                            mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
//                                    Uri.parse("https://play.google.com/store/apps/details?id=org.tta.mobile")));
//                            dialog.dismiss();
//
//                        });
//
//                        builder.setCancelable(false);
//                        builder.show();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//
//            }
//        });
//
//    }

    private String getCurrentVersion() {
//        Date date = Calendar.getInstance().getTime();
        current_date = Calendar.getInstance().getTime();
        PackageManager pm = mActivity.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo = pm.getPackageInfo(mActivity.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        currentVersion = null;
        if (pInfo != null) {
            currentVersion = pInfo.versionName;
            version_code = pInfo.versionCode;
        }
//        if (mDataManager.getAppPref().getUpdateSeenDate()== null){
//            mDataManager.getAppPref().setUpdateSeenDate(current_date.toString());
//        }
        String date1 = mDataManager.getAppPref().getUpdateSeenDate();
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        try {
            if (date1 != null)
                lastSeenDate = format.parse(date1);
            System.out.println(lastSeenDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (lastSeenDate != null)
            printDifference(lastSeenDate, current_date);

        return currentVersion;
    }

    public void printDifference(Date startDate, Date endDate) {
        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : " + endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        if (elapsedDays >= 1) {
            if (!Constants.IsUpdateDelay) {
                getAppUpdate();
            }
        }

//        long elapsedHours = different / hoursInMilli;
//        different = different % hoursInMilli;
//
//        long elapsedMinutes = different / minutesInMilli;
//        different = different % minutesInMilli;
//
//        long elapsedSeconds = different / secondsInMilli;
//
//        System.out.printf(
//                "%d days, %d hours, %d minutes, %d seconds%n",
//                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
    }

    private void showCustomAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater i = mActivity.getLayoutInflater();

        View v = i.inflate(R.layout.alert_dialog_layout, null);

        TextView title = v.findViewById(R.id.release_notes);
        title.setText("अपडेट मौजूद हैं, कृपया ऐप को अपडेट करें ");


        TextView update = v.findViewById(R.id.title);
        update.setText(R.string.update_tta);

        Button mbtn_delay = v.findViewById(R.id.btn_delay);
        Button mbtn_update = v.findViewById(R.id.btn_update);

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
}


