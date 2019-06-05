package org.tta.mobile.tta.task.content.course.certificate;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.content.MyCertificatesResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetMyCertificatesTask extends Task<MyCertificatesResponse> {

    @Inject
    private TaAPI taAPI;

    public GetMyCertificatesTask(Context context) {
        super(context);
    }

    @Override
    public MyCertificatesResponse call() throws Exception {
        return taAPI.getMyCertificates().execute().body();
    }
}
