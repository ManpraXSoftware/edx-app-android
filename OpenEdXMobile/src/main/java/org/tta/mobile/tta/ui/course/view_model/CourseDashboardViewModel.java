package org.tta.mobile.tta.ui.course.view_model;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;

import org.tta.mobile.R;
import org.tta.mobile.event.NetworkConnectivityChangeEvent;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.tta.mobile.tta.ui.course.CourseMaterialTab;
import org.tta.mobile.tta.ui.course.discussion.CourseDiscussionTab;
import org.tta.mobile.tta.utils.ToolTipView;
import org.tta.mobile.util.NetworkUtil;
import org.tta.mobile.util.images.ShareUtils;
import org.tta.mobile.view.AuthenticatedWebViewFragment;
import org.tta.mobile.view.CourseHandoutFragment;
import org.tta.mobile.view.Router;
import org.tta.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseDashboardViewModel extends BaseViewModel {

    public CourseDashboardPagerAdapter adapter;
    private List<Fragment> fragments;
    private List<String> titles;

    public Content content;
    private EnrolledCoursesResponse course;
    private CourseComponent rootComponent;

    public ObservableBoolean offlineVisible = new ObservableBoolean();
    public ObservableInt initialPosition = new ObservableInt();
    public ObservableInt tabPosition = new ObservableInt();
    public ObservableBoolean emptyVisible = new ObservableBoolean();

    private int position;

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

    public CourseDashboardViewModel(BaseVMActivity activity, Content content, int tabPosition) {
        super(activity);
        this.content = content;
        position = tabPosition;
        adapter = new CourseDashboardPagerAdapter(mActivity.getSupportFragmentManager());
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    public void loadCourseData(OnResponseCallback<EnrolledCoursesResponse> callback){
        mActivity.showLoading();

        mDataManager.getCourse(content, new OnResponseCallback<EnrolledCoursesResponse>() {
            @Override
            public void onSuccess(EnrolledCoursesResponse data) {
                course = data;
                mDataManager.getCourseComponent(course.getCourse().getId(),
                        new OnResponseCallback<CourseComponent>() {
                            @Override
                            public void onSuccess(CourseComponent data) {
                                rootComponent = data;
                                mActivity.hideLoading();
                                setTabs();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                mActivity.hideLoading();
//                                mActivity.showLongSnack(e.getLocalizedMessage());
                                setTabs();
                            }
                        });
                toggleEmptyVisibility();
                callback.onSuccess(data);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                toggleEmptyVisibility();
                callback.onFailure(e);
            }
        });

    }

    public void setTabs(){
        fragments.add(CourseMaterialTab.newInstance(content, course, rootComponent));
        titles.add(mActivity.getString(R.string.course_material));

        /*CourseDiscussionTopicsFragment discussionFragment = new CourseDiscussionTopicsFragment();
        if (course != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
            discussionFragment.setArguments(bundle);
        }
        discussionFragment.setRetainInstance(true);
        fragments.add(discussionFragment);*/
        fragments.add(CourseDiscussionTab.newInstance(course, content));
        titles.add(mActivity.getString(R.string.discussion));

        CourseHandoutFragment handoutFragment = new CourseHandoutFragment();
        if (course != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
            handoutFragment.setArguments(bundle);
        }
        handoutFragment.setRetainInstance(true);
        fragments.add(handoutFragment);
        titles.add(mActivity.getString(R.string.handouts));

        Uri uri = Uri.parse(course == null ? "" : course.getCourse().getCourse_about())
                .buildUpon()
                .appendQueryParameter(Constants.KEY_HIDE_ACTION, "true")
                .build();
        fragments.add(AuthenticatedWebViewFragment.newInstance(uri.toString()));
        titles.add(mActivity.getString(R.string.about));

        /*if (rootComponent != null) {
            for (IBlock block: rootComponent.getChildren()){
                CourseComponent comp = (CourseComponent) block;

                if (comp.isContainer()) {
                    for (IBlock childBlock : comp.getChildren()) {
                        CourseComponent child = (CourseComponent) childBlock;

                        if (child.getDisplayName().contains("कोर्स के बारे में")) {
                            CourseComponent childComp = (CourseComponent) child.getChildren().get(0);
                            fragments.add(AuthenticatedWebViewFragment.newInstance(childComp.getBlockUrl()));
                            titles.add(mActivity.getString(R.string.about));
                            break;
                        }
                    }
                }
            }
        }*/

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.tabPosition.set(position);
//         showToolTipAt(position);
        PageViewStateCallback callback = (PageViewStateCallback) fragments.get(position);
        if (callback != null){
            callback.onPageShow();
        }
    }

//    private void showToolTipAt(int position) {
//        if (position==0){
//            ToolTipView.showToolTip(getActivity(), "fdsbf", , Gravity.BOTTOM);
//
//        }
//    }

    private void toggleEmptyVisibility(){
        if (course == null || course.getCourse() == null){
            emptyVisible.set(true);
        } else {
            emptyVisible.set(false);
        }
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

    public void openShareMenu(View anchor) {

        if (course == null){
            return;
        }

        ShareUtils.showCourseShareMenu(getActivity(), anchor, course,
                mDataManager.getEdxEnvironment().getAnalyticsRegistry(), mDataManager.getEdxEnvironment(),
                content.getId());

    }

    public class CourseDashboardPagerAdapter extends BasePagerAdapter {

        public CourseDashboardPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

}
