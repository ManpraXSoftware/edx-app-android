package org.tta.mobile.tta.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.ui.logistration.SigninRegisterActivity;

import java.io.File;


/**
 * Created by Arjun on 2018/3/9.
 */

public class ActivityUtil {
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static void replaceFragmentInActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId, String tag,
                                                 boolean addToBackStack, String stackName) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment, tag);
        if (addToBackStack){
            transaction.addToBackStack(stackName);
        }
        transaction.commit();
    }

    public static void clearBackstackAndReplaceFragmentInActivity(@NonNull FragmentManager fragmentManager,
                                                 @NonNull Fragment fragment, int frameId, String tag,
                                                 boolean addToBackStack, String stackName) {
        clearBackStack(fragmentManager);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment, tag);
        if (addToBackStack){
            transaction.addToBackStack(stackName);
        }
        transaction.commit();
    }

    public static void gotoPage(Context context, Class<?> activityClass) {
        context.startActivity(new Intent(context, activityClass));
    }

    public static void gotoPage(Context context, Class<?> activityClass, Bundle bundle) {
        Intent intent = new Intent(context, activityClass);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public static void gotoPage(Context context, Class<?> activityClass, int flags) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(flags);
        context.startActivity(intent);
    }

    public static void gotoLogin(Context context) {
        Intent intent = new Intent(context, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void viewPDF(Context ctx, File filePath)
    {
        /** Pdf reader code */
        // File file = new File(Environment.getExternalStorageDirectory() + "/" + "abc.pdf");

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(ctx,
                    ctx.getApplicationContext().getPackageName() + ".provider", filePath);
        } else {
            uri = Uri.fromFile(filePath);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try
        {
            ctx.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Bundle parameters = new Bundle();
            parameters.putString(Constants.KEY_CLASS_NAME, ActivityUtil.class.getName());
            parameters.putString(Constants.KEY_FUNCTION_NAME, "viewPDF");
            parameters.putString(Constants.KEY_DATA, "Filepath = " + filePath.getAbsolutePath());
            Logger.logCrashlytics(e, parameters);
            Toast.makeText(ctx, "NO Pdf Viewer", Toast.LENGTH_SHORT).show();
        }
    }

    public static void playVideo(String filePath, Context context){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(filePath), "video/*");
        context.startActivity(Intent.createChooser(intent, "Complete action using"));

    }

    private static void clearBackStack(FragmentManager fragmentManager) {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStackImmediate(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }
}
