package org.tta.mobile.tta.task.content.course.scorm;

import android.content.Context;

import androidx.annotation.NonNull;

import org.tta.mobile.task.Task;

public class GetAllDownloadedScromCountTask extends Task<Integer> {

    public GetAllDownloadedScromCountTask(@NonNull Context context) {
        super(context);
    }

    @Override
    public Integer call() throws Exception {
        return environment.getStorage().getDownloadedScromCount();
    }

}
