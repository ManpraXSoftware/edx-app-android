package org.edx.mobile.discovery;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.edx.mobile.discovery.model.ActionError;
import org.edx.mobile.discovery.model.ResponseError;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Abstract class used for all Discourse Api Callbacks
 *
 * @param <T>
 */

public abstract class DiscoveryCallback<T> implements Callback<T> {
    private ResponseError mError;
    private boolean isRetrying = false;

    protected abstract void onResponse(@NonNull final T responseBody);

    protected void onFailure(ResponseError responseError, @NonNull final Throwable error) {
    }

    /**
     * Called when a 401 error is detected. Override this to handle token refresh and retry.
     * @param call The original call that failed with 401
     * @return true if token refresh was initiated and call will be retried, false otherwise
     */
    protected boolean onUnauthorized(Call<T> call) {
        return false;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponse(response.body());
        } else if (response.code() == 401 && !isRetrying) {
            // Handle 401 Unauthorized - token expired
            mError = new ResponseError(response);
            if (onUnauthorized(call)) {
                // Token refresh initiated, will retry
                return;
            } else {
                // No token refresh handler, call onFailure
                onFailure(call, mError);
            }
        } else if (response.code() == 422) {
            //special case handle response code 422 when creating post or topic or message
            mError = new ResponseError(response);
            try {
                ActionError error = new Gson().fromJson(response.errorBody().string(), ActionError.class);
                if (error.getErrors() != null && error.getErrors().length > 0) {
                    mError.setMsg(error.getErrors()[0]);
                }
                onFailure(call, mError);
            } catch (Exception e) {
                onFailure(call, mError);
            }
        } else {
            mError = new ResponseError(response);
            onFailure(call, mError);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        //  mError=new ResponseError(null);
        onFailure(mError, t);

    }

    /**
     * Retry the original call after token refresh
     * @param newCall The new call to execute (should be cloned from original with new token)
     */
    protected void retryCall(Call<T> newCall) {
        isRetrying = true;
        newCall.enqueue(this);
    }
}

