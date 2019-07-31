package org.tta.mobile.tta.widget.loading;

import android.content.Context;
import android.os.Bundle;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;


/**
 * Created by Arjun on 2018/3/14.
 */

public class ProgressDialogLoading implements ILoading {

    private DialogProgressLoading mPd;

    public ProgressDialogLoading(Context context) {
        mPd = new DialogProgressLoading(context);
    }

    @Override
    public void showLoading() {
        try {
            mPd.show();
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, ProgressDialogLoading.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "showLoading");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
    }

    @Override
    public void hideLoading() {
        try {
            mPd.dismiss();
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, ProgressDialogLoading.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "hideLoading");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            mPd.dismiss();
        } catch (Exception e) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, ProgressDialogLoading.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "dismiss");
            Logger.logCrashlytics(e, parameters);
            e.printStackTrace();
        }
    }
}
