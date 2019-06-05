package org.tta.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.discussion.CourseTopics;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.remote.api.DiscussionApi;

public class GetDiscussionTopicsTask extends Task<CourseTopics> {

    private String courseId;

    @Inject
    private DiscussionApi api;

    public GetDiscussionTopicsTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    @Override
    public CourseTopics call() throws Exception {
        return api.getCourseTopics(courseId).execute().body();
    }
}
