package org.tta.mobile.tta.utils;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;

import java.lang.reflect.Field;

public class BottomNavigationViewHelper {

    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                //noinspection RestrictedApi
                item.setShifting(false);
                // set once again checked value, so view will be updated
                //noinspection RestrictedApi
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, BottomNavigationViewHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "disableShiftMode");
            Logger.logCrashlytics(e, parameters);
            Log.e("BNVHelper", "Unable to get shift mode field", e);
        } catch (IllegalAccessException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, BottomNavigationViewHelper.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "disableShiftMode");
            Logger.logCrashlytics(e, parameters);
            Log.e("BNVHelper", "Unable to change value of shift mode", e);
        }
    }

}
