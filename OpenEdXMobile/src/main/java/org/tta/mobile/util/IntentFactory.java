package org.tta.mobile.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;

import org.tta.mobile.BuildConfig;

public enum IntentFactory {
    ;

    /**
     * @return a new Intent that will start the given activity.
     */
    @NonNull
    public static <T extends Activity> Intent newIntentForComponent(@NonNull Class<T> activityClass) {
        return new Intent().setComponent(new ComponentName(BuildConfig.APPLICATION_ID, activityClass.getName()));
    }
}
