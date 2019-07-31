package org.tta.mobile.tta.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;

public class AppUtil {

    public static boolean appInstalledOrNot(String uri, PackageManager pm) {
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, AppUtil.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "appInstalledOrNot");
            parameters.putString(Constants.KEY_DATA, "Uri = " + uri);
            Logger.logCrashlytics(e, parameters);
            app_installed = false;
        }
        return app_installed;
    }

    public static void openAppOnPlayStore(Context context, String packageName){
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, AppUtil.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "openAppOnPlayStore");
            parameters.putString(Constants.KEY_DATA, "Package = " + packageName);
            Logger.logCrashlytics(anfe, parameters);
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }

}
