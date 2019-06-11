package org.tta.mobile.user;

import android.support.annotation.Nullable;

public interface ProfileImageProvider {
    @Nullable
    ProfileImage getProfileImage();

    void setProfileImage(ProfileImage image);
}
