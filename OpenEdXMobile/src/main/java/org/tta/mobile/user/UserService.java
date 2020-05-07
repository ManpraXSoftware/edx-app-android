package org.tta.mobile.user;

import com.google.inject.Inject;

import org.tta.mobile.http.provider.RetrofitProvider;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {
    /**
     * A RoboGuice TaProvider implementation for UserService.
     */
    class Provider implements com.google.inject.Provider<UserService> {
        @Inject
        private RetrofitProvider retrofitProvider;

        @Override
        public UserService get() {
            return retrofitProvider.getWithOfflineCache().create(UserService.class);
        }
    }

    @GET("/api/user/v1/accounts/{username}")
    Call<Account> getAccount(@Path("username") String username);

    @Headers("Cache-Control: no-cache")
    @POST("/api/user/v1/accounts/{username}/image")
    Call<ResponseBody> setProfileImage(@Path("username") String username, @Header("Content-Disposition") String contentDisposition, @Body RequestBody file);

}
