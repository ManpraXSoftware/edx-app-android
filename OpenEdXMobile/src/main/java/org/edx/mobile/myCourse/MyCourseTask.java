package org.edx.mobile.myCourse;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.Task;

import java.util.List;

public class MyCourseTask extends Task<List<EnrolledCoursesResponse>> {
    @Inject
    private LoginAPI loginAPI;

    @NonNull
    private final String program_uuid ;
    @NonNull
    private final String token;
    @NonNull
    private final String username;

    @NonNull
    private final String language;

    public MyCourseTask(Context context, String program_uuid,String username,String token,String language) {
        super(context);
        this.program_uuid = program_uuid;
        this.token = token;
        this.username=username;
        this.language=language;
    }

    @Override
    public List<EnrolledCoursesResponse> call() throws Exception {
        return loginAPI.getMyCourses(token,program_uuid,username,language);
    }
}
