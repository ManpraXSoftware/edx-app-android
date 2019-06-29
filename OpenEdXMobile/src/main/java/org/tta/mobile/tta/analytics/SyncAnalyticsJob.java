package org.tta.mobile.tta.analytics;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SyncAnalyticsJob extends JobService {

    boolean isWorking = false;
    boolean jobCancelled = false;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("_____TAG_____", "Sync analytics job started!");
        isWorking = true;
        // We need 'jobParameters' so we can call 'jobFinished'
        startWorkOnNewThread(params); // Services do NOT run on a separate thread

        return isWorking;
    }

    private void startWorkOnNewThread(JobParameters jobParameters) {

        new Thread(() -> doWork(jobParameters)).start();

    }

    private void doWork(JobParameters jobParameters) {

        if (jobCancelled)
            return;

        Analytic analytic = new Analytic(this);
        analytic.syncAnalytics();

        Log.d("_____TAG_____", "Sync analytics job finished!");
        isWorking = false;
        boolean needsReschedule = false;
        jobFinished(jobParameters, needsReschedule);

    }


    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("_____TAG_____", "Sync analytics job cancelled before being completed.");
        jobCancelled = true;
        boolean needsReschedule = isWorking;
        jobFinished(params, needsReschedule);
        return needsReschedule;
    }
}
