package org.tta.mobile.user;

import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

@Singleton
public class UserAPI {
    @Inject
    private UserService userService;

    public Call<ResponseBody> setProfileImage(@NonNull String username, @NonNull final File file) {
        final String mimeType = "image/jpeg";
        return userService.setProfileImage(
                username,
                "attachment;filename=filename." + MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType),
                RequestBody.create(MediaType.parse(mimeType), file));
    }

    //TTA

    public Call<Account> getAccount(String username){
        return userService.getAccount(username);
    }
}
