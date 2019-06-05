package org.tta.mobile.tta.event;

import org.tta.mobile.tta.data.model.feed.SuggestedUser;

public class UserFollowingChangedEvent {

    private SuggestedUser user;

    public UserFollowingChangedEvent(SuggestedUser user) {
        this.user = user;
    }

    public SuggestedUser getUser() {
        return user;
    }

    public void setUser(SuggestedUser user) {
        this.user = user;
    }
}
