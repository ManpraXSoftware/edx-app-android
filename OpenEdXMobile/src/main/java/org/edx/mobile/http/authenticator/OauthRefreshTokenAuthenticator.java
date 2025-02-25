package org.edx.mobile.http.authenticator;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginService;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import roboguice.RoboGuice;

import static org.edx.mobile.http.util.CallUtil.executeStrict;

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
        logger.debug("Starting authentication for 401 response");

        final AuthResponse currentAuth = loginPrefs.getCurrentAuth();
        if (null == currentAuth || null == currentAuth.refresh_token) {
            logger.warn("No current auth or refresh token available");
            return null;
        }

        String responseBody = response.peekBody(200).string();
        logger.debug("401 Response body: " + responseBody);

        String errorCode = getErrorCode(responseBody);
        logger.debug("Parsed error code: " + errorCode);


        if (errorCode != null) {
            switch (errorCode) {
                case TOKEN_EXPIRED_ERROR_MESSAGE:
                    final AuthResponse refreshedAuth;
                    try {
                        refreshedAuth = refreshAccessToken(currentAuth);
                    } catch (HttpStatusException e) {
                        logger.debug("HttpStatusException: " + e.toString());
                        return null;
                    } catch (InterruptedException e) {
                        logger.debug("InterruptedException: " + e.toString());
                        throw new RuntimeException(e);
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
//        } else  {
//            try {
//                refreshAccessToken(currentAuth);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (HttpStatusException e) {
//                throw new RuntimeException(e);
//            }
//        }
        return null;
    }

    private AuthResponse refreshAccessToken(AuthResponse currentAuth) throws IOException, InterruptedException, HttpStatusException {
        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                LoginService loginService = RoboGuice.getInjector(context)
                        .getInstance(RetrofitProvider.class)
                        .getNonOAuthBased()
                        .create(LoginService.class);

                AuthResponse refreshTokenData = executeStrict(loginService.refreshAccessToken(
                        "refresh_token",
                        config.getOAuthClientId(),
                        currentAuth.refresh_token));
                loginPrefs.storeRefreshTokenResponse(refreshTokenData);
                return refreshTokenData;
            } catch (HttpStatusException e) {
                attempt++;
                if (attempt >= maxRetries) throw e;
                Thread.sleep(1000 * attempt); // Exponential backoff
            }
        }
        throw new IOException("Failed to refresh token after " + maxRetries + " attempts");
    }

    private String getErrorCode(String responseBody) {
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            if (jsonObj.has("error_code")) {
                return jsonObj.getString("error_code");
            }
            if (jsonObj.has("developer_message")) {
                Object developerMessage = jsonObj.get("developer_message");
                if (developerMessage instanceof JSONObject) {
                    JSONObject developerMessageObj = (JSONObject) developerMessage;
                    if (developerMessageObj.has("error_code")) {
                        return developerMessageObj.getString("error_code");
                    }
                }
            }
            logger.warn("No error_code found in response: " + responseBody);
            return null;
        } catch (JSONException ex) {
            logger.warn("Unable to parse error response: " + responseBody + " " + ex.toString());
            return null;
        }
    }
}
