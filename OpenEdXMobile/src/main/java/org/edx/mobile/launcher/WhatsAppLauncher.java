package org.edx.mobile.launcher;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

public class WhatsAppLauncher {

    private static final String PACKAGE_NAME = "com.whatsapp";
    private static final String ACTION_VIEW = Intent.ACTION_VIEW;
    private static final String DEEP_LINK_FORMAT = "https://chat.whatsapp.com/";

    private final Context context;

    public WhatsAppLauncher(Context context) {
        this.context = context;
    }

    public void openWhatsAppGroup(String groupLink) {
        if (isWhatsAppInstalled()) {
            String fullUrl = DEEP_LINK_FORMAT + groupLink;
            Uri uri = Uri.parse(fullUrl);
            Intent intent = createSendIntent(uri);
            startActivity(intent);
        } else {
            showInstallWhatsAppToast();
        }
    }

    private boolean isWhatsAppInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private Intent createSendIntent(Uri uri) {
        Intent intent = new Intent(ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Ensure new task for better UX
        return intent;
    }

    private void startActivity(Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle potential fallback or error reporting
            showInstallWhatsAppToast(); // Or provide relevant instructions
        }
    }

    private void showInstallWhatsAppToast() {
        Toast.makeText(context, "Please install WhatsApp to join this group", Toast.LENGTH_SHORT).show();
    }
}
