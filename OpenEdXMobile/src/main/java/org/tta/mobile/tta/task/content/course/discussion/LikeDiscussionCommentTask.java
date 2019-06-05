package org.tta.mobile.tta.task.content.course.discussion;

import android.content.Context;

import com.google.inject.Inject;

import org.tta.mobile.discussion.DiscussionComment;
import org.tta.mobile.task.Task;
import org.tta.mobile.tta.data.remote.api.DiscussionApi;

public class LikeDiscussionCommentTask extends Task<DiscussionComment> {

    private String commentId;
    private boolean liked;

    @Inject
    private DiscussionApi discussionApi;

    public LikeDiscussionCommentTask(Context context, String commentId, boolean liked) {
        super(context);
        this.commentId = commentId;
        this.liked = liked;
    }

    @Override
    public DiscussionComment call() throws Exception {
        return discussionApi.likeComment(commentId, liked).execute().body();
    }
}
