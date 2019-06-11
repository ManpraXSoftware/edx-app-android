package org.tta.mobile.tta.event;

import org.tta.mobile.discussion.DiscussionComment;

public class DiscussionCommentUpdateEvent {

    private DiscussionComment comment;

    public DiscussionCommentUpdateEvent(DiscussionComment comment) {
        this.comment = comment;
    }

    public DiscussionComment getComment() {
        return comment;
    }

    public void setComment(DiscussionComment comment) {
        this.comment = comment;
    }
}
