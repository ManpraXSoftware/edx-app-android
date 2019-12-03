package org.tta.mobile.tta.receiver;

import android.content.Context;
import android.content.Intent;

import org.tta.mobile.tta.data.DataManager;

import roboguice.receiver.RoboBroadcastReceiver;

public class DeleteFeedsReceiver extends RoboBroadcastReceiver {

    @Override
    protected void handleReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            DataManager dataManager = DataManager.getInstance(context);
            if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                dataManager.scheduleDeleteFeeds();
                return;
            }

            dataManager.deleteAllFeeds();
        }
    }
}
