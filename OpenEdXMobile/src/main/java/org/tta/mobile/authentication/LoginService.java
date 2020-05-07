package org.tta.mobile.authentication;

import androidx.annotation.NonNull;

import com.google.inject.Inject;

import org.tta.mobile.http.constants.ApiConstants;
import org.tta.mobile.http.constants.ApiConstants.TokenType;
import org.tta.mobile.model.api.ProfileModel;
import org.tta.mobile.tta.data.model.authentication.FieldInfo;
import org.tta.mobile.tta.data.model.authentication.MobileNumberVerificationResponse;
import org.tta.mobile.tta.data.model.authentication.ResetForgotedPasswordResponse;
import org.tta.mobile.tta.data.model.authentication.SendOTPResponse;
import org.tta.mobile.tta.data.model.authentication.VerifyOTPForgotedPasswordResponse;
import org.tta.mobile.tta.data.model.authentication.VerifyOTPResponse;
import org.tta.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.tta.mobile.tta.data.model.profile.UserAddressResponse;
import org.tta.mobile.tta.firebase.FirebaseUpdateTokenResponse;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

import static org.tta.mobile.http.constants.ApiConstants.URL_MY_USER_INFO;

public interface LoginService {

    /**
     * A RoboGuice TaProvider implementation for LoginService.
     */
    class Provider implements com.google.inject.Provider<LoginService> {
        @Inject
        private Retrofit retrofit;

        @Override
        public LoginService get() {
            return retrofit.create(LoginService.class);
        }
    }

    /**
     * If there are form validation errors, this call will fail with 400 or 409 error code.
     * In case of validation errors the response body will be {@link org.tta.mobile.model.api.FormFieldMessageBody}.
     */
    @NonNull
    @FormUrlEncoded
    @POST(ApiConstants.URL_REGISTRATION)
    Call<ResponseBody> register(@FieldMap Map<String, String> parameters);

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are sending a user and password to get the AuthResponse.
     */
    @NonNull
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    Call<AuthResponse> getAccessToken(@Field("grant_type") String grant_type,
                                      @Field("client_id") String client_id,
                                      @Field("username") String username,
                                      @Field("password") String password);

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are using our refresh_token to get a new AuthResponse.
     */
    @NonNull
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    Call<AuthResponse> refreshAccessToken(@Field("grant_type") String grant_type,
                                          @Field("client_id") String client_id,
                                          @Field("refresh_token") String refresh_token);


    /**
     * Revoke the specified refresh or access token, along with all other tokens based on the same
     * application grant.
     *
     * @param clientId      The client ID
     * @param token         The refresh or access token to be revoked
     * @param tokenTypeHint The type of the token to be revoked; This should be either
     *                      'access_token' or 'refresh_token'
     */
    @NonNull
    @FormUrlEncoded
    @POST(ApiConstants.URL_REVOKE_TOKEN)
    Call<ResponseBody> revokeAccessToken(@Field("client_id") String clientId,
                                         @Field("token") String token,
                                         @Field("token_type_hint") @TokenType String tokenTypeHint);

    @POST(ApiConstants.URL_LOGIN)
    Call<RequestBody> login();

    /**
     * @return basic profile information for currently authenticated user.
     */
    @NonNull
    @GET(URL_MY_USER_INFO)
    Call<ProfileModel> getProfile();

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_MOBILE_NUMBER_VERIFICATION)
    Call<MobileNumberVerificationResponse> mxMobileNumberVerification(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_VERIFY_OTP_FOR_FORGOTED_PASSWORD)
    Call<VerifyOTPForgotedPasswordResponse> mxOTPVerification_For_ForgotedPassword(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_RESET_FORGOTED_PASSWORD)
    Call<ResetForgotedPasswordResponse> mxResetForgotedPassword(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_GENERATE_OTP)
    Call<SendOTPResponse> mxGenerateOTP(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_VERIFY_OTP)
    Call<VerifyOTPResponse> mxVerifyOTP(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_GET_USER_ADDRESS)
    Call<UserAddressResponse> mxGetUserAddress(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_UPDATE_PROFILE)
    Call<UpdateMyProfileResponse> mxUpdateProfile(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_FIREBASE_TOKEN_UPDATE)
    Call<FirebaseUpdateTokenResponse> updateFirebaseToken(@FieldMap Map<String, String> parameters);

    @GET(ApiConstants.URL_MX_CUSTOM_FIELD_ATTRIBUTES)
    Call<FieldInfo> mxGetCustomStateFieldAttributes();
}
