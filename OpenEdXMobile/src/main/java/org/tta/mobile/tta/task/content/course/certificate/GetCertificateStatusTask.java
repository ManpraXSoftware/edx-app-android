package org.tta.mobile.tta.task.content.course.certificate;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.content.CertificateStatusResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetCertificateStatusTask extends Task<CertificateStatusResponse> {

    private String courseId;

    @Inject
    private TaAPI taAPI;

    public GetCertificateStatusTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public CertificateStatusResponse call() throws Exception {
        return taAPI.getCertificateStatus(courseId).execute().body();
    }
}
