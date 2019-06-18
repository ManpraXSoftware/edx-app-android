package org.tta.mobile.tta.event;

import org.tta.mobile.tta.wordpress_client.model.Comment;

public class ConnectCommentChangedEvent {

    private Comment comment;

    public ConnectCommentChangedEvent(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
