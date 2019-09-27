package org.tta.mobile.tta.task.profile;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.profile.AllCertificatesResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetAllCertificatesTask extends Task<AllCertificatesResponse> {

    @Inject
    private TaAPI taAPI;

    public GetAllCertificatesTask(Context context) {
        super(context);
    }

    @Override
    public AllCertificatesResponse call() throws Exception {
        return taAPI.getAllCertificates().execute().body();
    }
}
