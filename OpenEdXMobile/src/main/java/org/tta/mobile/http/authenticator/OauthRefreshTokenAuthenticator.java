package org.tta.mobile.http.authenticator;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.tta.mobile.R;
import org.tta.mobile.authentication.LoginService;

import org.tta.mobile.authentication.AuthResponse;
import org.tta.mobile.http.provider.RetrofitProvider;
import org.tta.mobile.http.HttpStatusException;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.module.prefs.LoginPrefs;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.DataManager;
import org.tta.mobile.util.Config;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import roboguice.RoboGuice;

import static org.tta.mobile.http.util.CallUtil.executeStrict;

/**
 * Authenticator for 401 responses for refreshing oauth tokens. Checks for
 * the expired oauth token case and then uses the refresh token to retrieve a
 * new access token. Using the new access token, the original http request
 * that received the 401 will be attempted again. If no refresh_token is
 * present, no authentication attempt is made.
 */
public class OauthRefreshTokenAuthenticator implements Authenticator {

    private final Logger logger = new Logger(getClass().getName());
    private final static String TOKEN_EXPIRED_ERROR_MESSAGE = "token_expired";
    private final static String TOKEN_NONEXISTENT_ERROR_MESSAGE = "token_nonexistent";
    private final static String TOKEN_INVALID_GRANT_ERROR_MESSAGE = "invalid_grant";
    private Context context;

    @Inject
    Config config;

    @Inject
    LoginPrefs loginPrefs;


    public OauthRefreshTokenAuthenticator(Context context) {
        this.context = context;
        RoboGuice.injectMembers(context, this);
    }

    @Override
    public synchronized Request authenticate(Route route, final Response response) throws IOException {
        logger.warn(response.toString());

        final AuthResponse currentAuth = loginPrefs.getCurrentAuth();
        if (null == currentAuth || null == currentAuth.refresh_token) {
            logout();
            return null;
        }

        String errorCode = getErrorCode(response.peekBody(200).string());

        if (errorCode != null) {
            switch (errorCode) {
                case TOKEN_EXPIRED_ERROR_MESSAGE:
                    final AuthResponse refreshedAuth;
                    try {
                        refreshedAuth = refreshAccessToken(currentAuth);
                    } catch (HttpStatusException e) {
                        Bundle parameters = new Bundle();
                        parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, OauthRefreshTokenAuthenticator.class.getName());
                        parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "authenticate");
                        parameters.putString(Constants.KEY_DATA, "response = " + response.toString());
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
    private AuthResponse refreshAccessToken(AuthResponse currentAuth)
            throws IOException, HttpStatusException {
        // RoboGuice doesn't seem to allow this to be injected via annotation at initialization
        // time. TODO: Investigate whether this is a bug in RoboGuice.
        LoginService loginService = RoboGuice.getInjector(context)
                .getInstance(RetrofitProvider.class).getNonOAuthBased().create(LoginService.class);

        AuthResponse refreshTokenData = executeStrict(loginService.refreshAccessToken(
                "refresh_token", config.getOAuthClientId(), currentAuth.refresh_token));
        loginPrefs.storeRefreshTokenResponse(refreshTokenData);
        return refreshTokenData;
    }

    @Nullable
    private String getErrorCode(String responseBody) {
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            return jsonObj.getString("error_code");
        } catch (JSONException ex) {
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, OauthRefreshTokenAuthenticator.class.getName());
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
