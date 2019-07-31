package org.tta.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.tta.mobile.R;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.analytics.analytics_enums.Action;
import org.tta.mobile.tta.analytics.analytics_enums.Nav;
import org.tta.mobile.tta.analytics.analytics_enums.Source;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.interfaces.OnResponseCallback;
import org.tta.mobile.tta.ui.base.BasePagerAdapter;
import org.tta.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.tta.mobile.tta.ui.course.view_model.CourseDashboardViewModel;
import org.tta.mobile.tta.ui.landing.LandingActivity;
import org.tta.mobile.tta.utils.ActivityUtil;
import org.tta.mobile.tta.utils.BreadcrumbUtil;
import org.tta.mobile.view.common.PageViewStateCallback;

public class CourseDashboardActivity extends BaseVMActivity {
    private int RANK;

    private Content content;
    private boolean isPush = false;
    private int tabPosition;
    private CourseDashboardViewModel viewModel;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.course.name()));
        getExtras();
        viewModel = new CourseDashboardViewModel(this, content, tabPosition);
        binding(R.layout.t_activity_course_dashboard, viewModel);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                logD("SELECTED::::=> "+position);
            }
        });
//        viewPager.post(new Runnable() {
//            @Override
//            public void run() {
//              ToolTipView.showToolTip(CourseDashboardActivity.this, "fdsbf", tabLayout.getChildAt(0), Gravity.BOTTOM);
//            }
//        });

        viewModel.registerEventBus();

        analytic.addMxAnalytics_db(content.getName(), Action.CourseView, content.getName(),
                Source.Mobile, content.getSource_identity(),
                content.getSource_identity(), content.getId());

        viewModel.loadCourseData(new OnResponseCallback<EnrolledCoursesResponse>() {
            @Override
            public void onSuccess(EnrolledCoursesResponse data) {

            }

            @Override
            public void onFailure(Exception e) {
                toolbar.post(() -> {
                    toolbar.getMenu().findItem(R.id.action_share).setVisible(false);
                });
            }
        });
    }

    private void getExtras() {
        Bundle parameters = getIntent().getExtras();
        if (parameters != null) {
            if (parameters.containsKey(Constants.KEY_IS_PUSH)){
                isPush = parameters.getBoolean(Constants.KEY_IS_PUSH);
            }
            if (parameters.containsKey(Constants.KEY_CONTENT)){
                content = parameters.getParcelable(Constants.KEY_CONTENT);
            }
            if (parameters.containsKey(Constants.KEY_TAB_POSITION)){
                tabPosition = parameters.getInt(Constants.KEY_TAB_POSITION);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect_dashboard_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_share:
                viewModel.openShareMenu();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!isPush){
            super.onBackPressed();
        } else {
            if (!LandingActivity.isAlreadyOpened) {
                ActivityUtil.gotoPage(this, LandingActivity.class);
            }
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.course.name()));
        viewPager.post(() -> {
            try {
                PageViewStateCallback callback = (PageViewStateCallback) ((BasePagerAdapter) viewPager.getAdapter())
                        .getItem(viewModel.initialPosition.get());
                if (callback != null){
                    callback.onPageShow();
                }
            } catch (Exception e) {
                Bundle parameters = new Bundle();
                parameters.putString(Constants.KEY_CLASS_NAME, CourseDashboardActivity.class.getName());
                parameters.putString(Constants.KEY_FUNCTION_NAME, "onResume");
                parameters.putString(Constants.KEY_DATA, "Content id = " + content.getId());
                Logger.logCrashlytics(e, parameters);
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.unRegisterEventBus();
    }
}
