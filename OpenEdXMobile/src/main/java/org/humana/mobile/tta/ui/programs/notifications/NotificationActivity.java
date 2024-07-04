package org.humana.mobile.tta.ui.programs.notifications;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.MenuItem;


import org.humana.mobile.R;

import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.Constants;

import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;

import org.humana.mobile.tta.ui.programs.notifications.viewModel.NotificationViewModel;
import org.humana.mobile.view.Router;


public class NotificationActivity extends BaseVMActivity {
    private NotificationViewModel viewModel;
    private EnrolledCoursesResponse course;
    private long periodId;
    private String periodName, unitId, courseId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = null;
        if (getIntent().getExtras() != null) {
            bundle = getIntent().getExtras();
        } else if (savedInstanceState != null) {
            bundle = savedInstanceState;
        }

        if (bundle != null) {
            getBundledData(bundle);
        } else {
            // Handle the case where both getIntent().getExtras() and savedInstanceState are null
            // For example, you can log an error or provide default behavior
            Log.e("NotificationActivity", "No data available in Intent extras or savedInstanceState.");
        }

        viewModel = new NotificationViewModel(this);
        binding(R.layout.t_fragment_notifications, viewModel);
        setSupportActionBar(findViewById(R.id.toolbar));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.KEY_PERIOD_ID, periodId);
        if (periodName != null){
            outState.putString(Constants.KEY_PERIOD_NAME, periodName);
        }
        if (course != null){
            outState.putSerializable(Router.EXTRA_COURSE_DATA, course);
        }
    }

    private void getBundledData(Bundle parameters){
        if (parameters.containsKey(Constants.KEY_PERIOD_ID)){
            periodId = parameters.getLong(Constants.KEY_PERIOD_ID);
        }
        if (parameters.containsKey(Constants.KEY_PERIOD_NAME)){
            periodName = parameters.getString(Constants.KEY_PERIOD_NAME);
        }
        if (parameters.containsKey(Router.EXTRA_COURSE_DATA)){
            course = (EnrolledCoursesResponse) parameters.getSerializable(Router.EXTRA_COURSE_DATA);
        }

    }
}