package org.tta.mobile.tta.firebase;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class TaFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        //update Firebase token , we will update it on sign-in or registration too

        Log.d("firebaseToken",token);
        if(loginPrefs==null || !loginPrefs.isLoggedIn() ||this.getApplicationContext()==null)
            return;

        FirebaseHelper fireBaseHelper=new FirebaseHelper();
        try
        {
            fireBaseHelper.updateFirebasetokenToServer(this.getApplicationContext(),fireBaseHelper.getFireBaseParams(loginPrefs.getUsername()));
        }
        catch (Exception ex)
        {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, TaFirebaseInstanceIDService.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "sendRegistrationToServer");
            parameters.putString(Constants.KEY_DATA, "token = " + token);
            Logger.logCrashlytics(ex, parameters);
            Log.d("ManpraxFirebase","MyFirebaseInstanceIDService class ID update crash");
        }
    }
    public String getFireBaseToken()
    {
        return  FirebaseInstanceId.getInstance().getToken();
    }

}
