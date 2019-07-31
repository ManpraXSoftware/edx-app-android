package org.tta.mobile.tta.analytics;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;

import org.tta.mobile.authentication.LoginAPI;
import org.tta.mobile.http.HttpResponseStatusException;
import org.tta.mobile.http.HttpStatus;
import org.tta.mobile.logger.Logger;
import org.tta.mobile.model.api.FormFieldMessageBody;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.data.model.SuccessResponse;

import retrofit2.Response;

/**
 * Created by JARVICE on 06-06-2018.
 */

public abstract class TinCanAnalyticTask extends Task<SuccessResponse> {

    @NonNull
    private final TincanRequest tincanAnalyticslist;

    @NonNull
    private final Context ctx;
    @NonNull
    private Gson gson;

    @Inject
    private LoginAPI loginAPI;

    @Inject
    private MxAnalyticsAPI mxAnalyticsAPI;

   /* @Inject
    private AnalyticsAPI analyticsAPI;*/

    public TinCanAnalyticTask(@NonNull Context context, @NonNull TincanRequest stringArrayList) {
        super(context);
        this.ctx = context;
        this.tincanAnalyticslist = stringArrayList;
        gson=new Gson();
    }

    @Override
    @NonNull
    public SuccessResponse call() throws Exception {
        Response<SuccessResponse> response = mxAnalyticsAPI.postTincanAnalytics(tincanAnalyticslist).execute();

        if (!response.isSuccessful()) {
            final int errorCode = response.code();
            final String errorBody = response.errorBody().string();
            if ((errorCode == HttpStatus.BAD_REQUEST || errorCode == HttpStatus.CONFLICT) && !android.text.TextUtils.isEmpty(errorBody)) {
                try {
                    final FormFieldMessageBody body = gson.fromJson(errorBody, FormFieldMessageBody.class);
                    if (body != null && body.size() > 0) {
                        throw new LoginAPI.RegistrationException(body);
                    }
                } catch (JsonSyntaxException ex) {
                    Bundle parameters = new Bundle();
                    parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, TinCanAnalyticTask.class.getName());
                    parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "call");
                    parameters.putString(Constants.KEY_DATA, "tincanAnalyticslist = " + tincanAnalyticslist);
                    Logger.logCrashlytics(ex, parameters);
                    // Looks like the response does not contain form validation errors.
                }
            }
            throw new HttpResponseStatusException(errorCode);
        }
        return response.body();
    }
}
