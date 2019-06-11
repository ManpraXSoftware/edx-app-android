package org.tta.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.discussion.DiscussionThread;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.remote.api.DiscussionApi;

public class GetDiscussionThreadTask extends Task<DiscussionThread> {

    private String threadId;

    @Inject
    private DiscussionApi api;

    public GetDiscussionThreadTask(Context context, String threadId) {
        super(context);
        this.threadId = threadId;
    }

    @Override
    public DiscussionThread call() throws Exception {
        return api.getDiscussionThread(threadId).execute().body();
    }
}
