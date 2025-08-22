package org.edx.mobile.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class OrientationUtils {

    public static int getOrientation(Activity context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        boolean isLandscape = displayMetrics.widthPixels > displayMetrics.heightPixels;
        if(isLandscape)
            return 2;
        return 1;
    }
}
