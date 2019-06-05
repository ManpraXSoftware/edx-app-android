package org.tta.mobile.tta.task.content.course.certificate;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.model.content.MyCertificatesResponse;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class GetCertificateTask extends Task<MyCertificatesResponse> {

    private String courseId;

    @Inject
    private TaAPI taAPI;

    public GetCertificateTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public MyCertificatesResponse call() throws Exception {
        return taAPI.getCertificate(courseId).execute().body();
    }
}
