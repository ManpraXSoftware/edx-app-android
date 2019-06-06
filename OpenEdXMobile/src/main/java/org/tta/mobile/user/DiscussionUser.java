
package org.tta.mobile.user;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DiscussionUser implements Serializable {
    @NonNull
    @SerializedName("profile")
    private Profile profile;

    @NonNull
    public Profile getProfile() {
        return profile;
    }

    public static class Profile implements Serializable {
        @Nullable
        @SerializedName("image")
        private ProfileImage image;

        @Nullable
        public ProfileImage getImage() {
            return image;
        }

        @SerializedName("display_name")
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }
    }
}
