package org.tta.mobile.tta.utils;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.tta.mobile.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;


public class TimeHandler {

    String givenDateString = "Tue June 14 14:00:08 GMT+05:30 2019";
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

    Date mDate;

    {
        try {
            mDate = sdf.parse(givenDateString);
        } catch (ParseException e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, TimeHandler.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "Default block");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
    }

    long timeInMilliseconds = Objects.requireNonNull(mDate).getTime();
    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            Log.d("Timehandle", "Time handler hitt...");
            mHandler.postDelayed(mHandlerTask, timeInMilliseconds);
        }
    };

    void startRepeatingTask() {
        mHandlerTask.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }


}
