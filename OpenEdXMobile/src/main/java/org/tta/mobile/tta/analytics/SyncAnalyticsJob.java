package org.tta.mobile.tta.analytics;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.tta.mobile.tta.Constants;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SyncAnalyticsJob extends JobService {

    boolean isWorking = false;
    boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("_____TAG_____", "Sync analytics job started!");
        isWorking = true;
        // We need 'jobParameters' so we can call 'jobFinished'
//        startWorkOnNewThread(params); // Services do NOT run on a separate thread

        doWork(params);

        return isWorking;
    }

    private void startWorkOnNewThread(JobParameters jobParameters) {

        new Thread(() -> doWork(jobParameters)).start();

    }

    private void doWork(JobParameters jobParameters) {

        if (jobCancelled)
            return;

        Analytic analytic = new Analytic(getApplicationContext());
        analytic.syncAnalytics();

        Log.d("_____TAG_____", "Sync analytics job finished!");
        isWorking = false;
        boolean needsReschedule = false;
        jobFinished(jobParameters, needsReschedule);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            scheduleRefresh();
        }

    }


    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("_____TAG_____", "Sync analytics job cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        jobFinished(params, needsReschedule);
        return needsReschedule;
    }

    private void scheduleRefresh() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ComponentName componentName = new ComponentName(getApplicationContext(), SyncAnalyticsJob.class);
            JobInfo.Builder builder = new JobInfo.Builder(12, componentName);
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                builder.setPeriodic(Constants.INTERVAL_SYNC_ANALYTICS_JOB);
            } else {
                builder.setMinimumLatency(Constants.INTERVAL_SYNC_ANALYTICS_JOB);
            }
            JobInfo jobInfo = builder.build();

            JobScheduler jobScheduler = (JobScheduler)getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = jobScheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Log.d("_____TAG_____", "Sync analytics job scheduled!");
            } else {
                Log.d("_____TAG_____", "Sync analytics job not scheduled");
            }
        }

    }
}
