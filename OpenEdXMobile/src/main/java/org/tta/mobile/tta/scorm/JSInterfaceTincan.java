package org.tta.mobile.tta.scorm;

import android.webkit.JavascriptInterface;

import org.tta.mobile.tta.ui.course.CourseScormViewActivity;

public class JSInterfaceTincan {

    CourseScormViewActivity ctx;

    public JSInterfaceTincan(CourseScormViewActivity activity) {
        ctx = activity;
    }

    @JavascriptInterface
    public void sendDataToAndroid(String data) {
        ctx.ReceiveTinCanStatement(data);
    }

    @JavascriptInterface
    public void sendResumeDataToAndroid(String resume_info) {
        ctx.ReceiveTinCanResumePayload(resume_info);
    }
    @JavascriptInterface
    public void sendTincanObject(String tincan_obj) {
        ctx.ReceiveTincanObject(tincan_obj);
    }

}
