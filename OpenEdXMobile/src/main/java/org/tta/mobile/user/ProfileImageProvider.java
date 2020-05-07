package org.tta.mobile.user;

import androidx.annotation.Nullable;

public interface ProfileImageProvider {
    @Nullable
    ProfileImage getProfileImage();

    void setProfileImage(ProfileImage image);
}
