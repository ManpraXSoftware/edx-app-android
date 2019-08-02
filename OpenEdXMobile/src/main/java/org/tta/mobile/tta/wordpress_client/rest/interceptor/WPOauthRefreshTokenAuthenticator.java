package org.tta.mobile.tta.wordpress_client.rest.interceptor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.inject.Inject;

import org.tta.mobile.R;
import org.tta.mobile.authentication.AuthResponse;
import org.tta.mobile.http.HttpResponseStatusException;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.DataManager;
import org.tta.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.tta.mobile.tta.wordpress_client.rest.WpClientRetrofit;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;


/**
 * Created by JARVICE on 28-12-2017.
 */

public class WPOauthRefreshTokenAuthenticator implements Authenticator {

    private final Logger logger = new Logger(getClass().getName());
    private final static String TOKEN_EXPIRED_ERROR_MESSAGE = "401";
    private final static String TOKEN_NONEXISTENT_ERROR_MESSAGE = "token_nonexistent";
    private final static String TOKEN_INVALID_GRANT_ERROR_MESSAGE = "invalid_grant";
    private Context context;

    public WPOauthRefreshTokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public synchronized Request authenticate(Route route, final Response response) throws IOException {
        logger.warn(response.toString());

        final AuthResponse currentAuth = loginPrefs.getWPCurrentAuth();
        if (null == currentAuth || null == currentAuth.refresh_token) {
            logout();
            return null;
        }

        String errorCode = String.valueOf(response.code()); //getErrorCode(response.peekBody(200).string());


        if (errorCode != null) {
            switch (errorCode) {
                case TOKEN_EXPIRED_ERROR_MESSAGE:
                    final WpAuthResponse refreshedAuth;
                    try {
                        refreshedAuth = refreshAccessToken(currentAuth);
                    } catch (HttpResponseStatusException e) {
                        Bundle parameters = new Bundle();
                        parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, WPOauthRefreshTokenAuthenticator.class.getName());
                        parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "authenticate");
                        parameters.putString(Constants.KEY_DATA, "currentAuth = " + currentAuth.toString());
                        Logger.logCrashlytics(e, parameters);
                        logout();
                        return null;
                    }
                    return response.request().newBuilder()
                            .header("Authorization", refreshedAuth.token_type + " " + refreshedAuth.access_token)
                            .build();
                case TOKEN_NONEXISTENT_ERROR_MESSAGE:
                case TOKEN_INVALID_GRANT_ERROR_MESSAGE:
                    // Retry request with the current access_token if the original access_token used in
                    // request does not match the current access_token. This case can occur when
                    // asynchronous calls are made and are attempting to refresh the access_token where
                    // one call succeeds but the other fails. https://github.com/edx/edx-app-android/pull/834
                    if (!response.request().headers().get("Authorization").split(" ")[1].equals(currentAuth.access_token)) {
                        return response.request().newBuilder()
                                .header("Authorization", currentAuth.token_type + " " + currentAuth.access_token)
                                .build();
                    }
            }
        }
        return null;
    }

    @NonNull
    private WpAuthResponse refreshAccessToken(AuthResponse currentAuth)
            throws IOException, HttpResponseStatusException {

        WpClientRetrofit clientRetrofit=new WpClientRetrofit(false);
        retrofit2.Response<WpAuthResponse> refreshTokenResponse=  clientRetrofit.refreshAccessToken(currentAuth.refresh_token).execute();

        if (!refreshTokenResponse.isSuccessful()) {
            throw new HttpResponseStatusException(refreshTokenResponse.code());
        }

        WpAuthResponse refreshTokenData = refreshTokenResponse.body();
        loginPrefs.storeWPRefreshTokenResponse(refreshTokenData);
        return refreshTokenData;
    }

    @Nullable
    private String getErrorCode(String responseBody) {
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            return jsonObj.getString("error_code");
        } catch (JSONException ex) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, WPOauthRefreshTokenAuthenticator.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getErrorCode");
            parameters.putString(Constants.KEY_DATA, "responseBody = " + responseBody);
            Logger.logCrashlytics(ex, parameters);

            logger.warn("Unable to get error_code from 401 response");
            return null;
        }
    }

    private void logout(){
        if (loginPrefs.isLoggedIn()) {
            loginPrefs.clear();
            DataManager dataManager = DataManager.getInstance(context.getApplicationContext());
            dataManager.showToastFromOtherThread(context.getString(R.string.session_expire), Toast.LENGTH_LONG);
            dataManager.logout();
        }
    }
}
