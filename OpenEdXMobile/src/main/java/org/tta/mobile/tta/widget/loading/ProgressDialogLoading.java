package org.tta.mobile.tta.widget.loading;

import android.content.Context;


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
            e.printStackTrace();
        }
    }

    @Override
    public void hideLoading() {
        try {
            mPd.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            mPd.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
