package org.tta.mobile.tta.tincan;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;
import org.tta.mobile.tta.tincan.model.Resume;

import static org.tta.mobile.util.BrowserUtil.environment;

public class Tincan {

    public void addResumePayload(Resume resume) {
        if (TextUtils.isEmpty(resume.getCourse_Id()) || TextUtils.isEmpty(resume.getUnit_id()) ||
                TextUtils.isEmpty(resume.getResume_Payload())) {
            Log.d("MX Payload", "curse id and unit id can't be null");
            return;
        }

        Resume re= new Resume();
        re=getResumeInfo(resume.getCourse_Id(), resume.getUnit_id());
        //first check if entry exist just update otherwise create new one.
        if (re == null || TextUtils.isEmpty(re.getResume_Payload())) {
            environment.getStorage().addResumePayload(resume);
        } else {
            environment.getStorage().updateResumePayload(resume);
        }

    }

    public void deleteResumePayload(String course_id, String unit_id) {
        if (TextUtils.isEmpty(course_id) || TextUtils.isEmpty(unit_id)) {
            Log.d("MX Payload", "curse id and unit id can't be null");
            return;
        }

        environment.getStorage().deleteResumePayload(course_id, unit_id);
    }

    private Resume getResumeInfo(String course_id, String unit_id) {
        Resume payload = new Resume();
        try {
            payload = environment.getStorage().getResumeInfo(course_id, unit_id);
        } catch (Exception e) {

            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, Tincan.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getResumeInfo");
            parameters.putString(Constants.KEY_DATA, "course_id = " + course_id +
                    ", unit_id = " + unit_id);
            Logger.logCrashlytics(e, parameters);

            e.printStackTrace();
            payload = null;
            Log.d("Tincan", "Dbfetch fail");
        }
        return payload;
    }

}
