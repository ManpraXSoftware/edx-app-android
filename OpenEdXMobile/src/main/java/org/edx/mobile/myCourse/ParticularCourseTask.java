package org.edx.mobile.myCourse;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.programs.Programs;
import org.edx.mobile.task.Task;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ParticularCourseTask extends Task<ArrayList<EnrolledCoursesResponse>> {

    @Inject
    private LoginAPI loginAPI;

    private String userId;
    private String courseId;

    public ParticularCourseTask(Context context, String userId, String courseId) {
        super(context);
        this.userId = userId;
        this.courseId = courseId;
    }

    @Override
    public ArrayList<EnrolledCoursesResponse> call() throws Exception {
        return loginAPI.getParticularCourseTask(userId, courseId);
    }
}
