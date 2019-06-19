package org.tta.mobile.tta.event;

import org.tta.mobile.tta.wordpress_client.model.Comment;

public class ConnectCommentAddedEvent {

    private Comment comment;

    public ConnectCommentAddedEvent(Comment comment) {
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
