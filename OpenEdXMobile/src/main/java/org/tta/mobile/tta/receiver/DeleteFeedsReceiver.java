package org.tta.mobile.tta.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.tta.mobile.tta.data.DataManager;

public class DeleteFeedsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
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
