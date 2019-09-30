package org.tta.mobile.tta.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.DataManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PendingCertificateNotificationService extends JobService {

    boolean isWorking = false;
    boolean jobCancelled = false;

    private DataManager dataManager;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("_____TAG_____", "PendingCertificateNotificationService started!");
        isWorking = true;

        doWork(params);

        return isWorking;
    }

    private void doWork(JobParameters params) {

        if (jobCancelled)
            return;

        dataManager = DataManager.getInstance(getApplicationContext());
        dataManager.createPendingCertificatesNotification();

        Log.d("_____TAG_____", "PendingCertificateNotificationService finished!");
        isWorking = false;
        boolean needsReschedule = false;
        try {
            jobFinished(params, needsReschedule);
        } catch (Exception e) {
            Bundle parameters1 = new Bundle();
            parameters1.putString(Constants.KEY_CLASS_NAME, PendingCertificateNotificationService.class.getName());
            parameters1.putString(Constants.KEY_FUNCTION_NAME, "doWork");
            Logger.logCrashlytics(e, parameters1);
            e.printStackTrace();
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scheduleRefresh();
        }

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("_____TAG_____", "PendingCertificateNotificationService cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        try {
            jobFinished(params, needsReschedule);
        } catch (Exception e) {
            Bundle parameters1 = new Bundle();
            parameters1.putString(Constants.KEY_CLASS_NAME, PendingCertificateNotificationService.class.getName());
            parameters1.putString(Constants.KEY_FUNCTION_NAME, "onStopJob");
            Logger.logCrashlytics(e, parameters1);
            e.printStackTrace();
        }
        return needsReschedule;
    }

    private void scheduleRefresh() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ComponentName componentName = new ComponentName(getApplicationContext(),
                    PendingCertificateNotificationService.class);
            JobInfo.Builder builder = new JobInfo.Builder(Constants.REQUEST_CODE_PENDING_CERTIFICATE_NOTIFICATION,
                    componentName);
            builder.setPersisted(true);
            builder.setMinimumLatency(Constants.INTERVAL_PENDING_CERTIFICATE_NOTIFICATION);
            JobInfo jobInfo = builder.build();

            JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = jobScheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d("_____TTA LOG_____", "PendingCertificateNotificationService scheduled!");
            } else {
                Log.d("_____TTA LOG_____", "PendingCertificateNotificationService not scheduled");
            }
        }
    }

}
