package org.tta.mobile.tta.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import org.tta.mobile.tta.firebase.FirebaseTokenUpdateTask;
import org.tta.mobile.tta.firebase.FirebaseUpdateTokenResponse;
import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class FirebaseUtil {
    private Context mCtx;

    public FirebaseUtil(Context ctx)
    {
        mCtx=ctx;
    }

    public void  syncFirebaseToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener((Activity) mCtx, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.d("Mx_firebase", "token is=====>" + newToken);
                if(loginPrefs.getFireBaseToken()==null ||
                        loginPrefs.getFireBaseToken().isEmpty())
                {
                    saveFirebaseToken(newToken);
                    updateFirebaseTokenToServer(newToken);
                }
                else if(!loginPrefs.getFireBaseToken().trim().toLowerCase()
                        .equals(newToken.trim().toLowerCase()))
                {
                    saveFirebaseToken(newToken);
                    updateFirebaseTokenToServer(newToken);
                }
                updateFirebaseTokenToServer(newToken);
            }
        });
    }

    private void saveFirebaseToken(String token)
    {
        loginPrefs.storeFireBaseToken(token);
    }

    private void updateFirebaseTokenToServer(String token) {
        if (loginPrefs == null || loginPrefs.getUsername() == null || loginPrefs.getUsername().equals(""))
            return;

        Bundle parameters = new Bundle();
        parameters.putString("user_id", loginPrefs.getUsername());
        parameters.putString("token_id", token);
        //parameters.putString("device_info",device_info);

        // Add custom implementation, as needed.
        //update Firebase token , we will update it on sign-in or registration too

        Log.d("Mx_firebase", "token is=====>" + token);

        try {
            FirebaseTokenUpdateTask task = new FirebaseTokenUpdateTask(mCtx, parameters) {
                @Override
                public void onSuccess(@NonNull FirebaseUpdateTokenResponse result) {
                    Log.d("Mx_FireBase", "firebase token update to server successfull");
                }

                @Override
                public void onException(Exception ex) {
                    Log.d("Mx_FireBase", "firebase token update to server fail ex=>" + ex.toString());
                }
            };
            task.execute();
        } catch (Exception ex) {
            Log.d("Mx_FireBase", "firebase token update to server fail ex=>" + ex.toString());
        }
    }
}
