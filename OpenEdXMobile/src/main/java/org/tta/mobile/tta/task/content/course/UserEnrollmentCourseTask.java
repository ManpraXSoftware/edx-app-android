package org.tta.mobile.tta.task.content.course;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.remote.api.TaAPI;

public class UserEnrollmentCourseTask extends Task<EnrolledCoursesResponse> {
    private String courseId;

    @Inject
    private TaAPI taAPI;

    public UserEnrollmentCourseTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public EnrolledCoursesResponse call() throws Exception {
        return taAPI.userEnrollmentCourse(courseId).execute().body();
    }
}
