package org.tta.mobile.tta.task.content.course;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.model.course.CourseComponent;
import org.tta.mobile.services.CourseManager;
import org.tta.mobile.task.Task;

public class GetCourseDataFromPersistableCacheTask extends Task<CourseComponent> {

    private String courseId;

    @Inject
    private CourseManager courseManager;

    public GetCourseDataFromPersistableCacheTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public CourseComponent call() throws Exception {
        return courseManager.getCourseDataFromPersistableCache(courseId);
    }
}
