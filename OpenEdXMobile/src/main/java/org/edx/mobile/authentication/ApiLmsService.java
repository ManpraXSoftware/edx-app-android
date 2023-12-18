package org.edx.mobile.authentication;

import org.edx.mobile.discovery.model.ResponseEnrollmentModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import static org.edx.mobile.http.constants.ApiConstants.URL_ENROLL_CHECK;

public interface ApiLmsService {
    @GET(URL_ENROLL_CHECK)
    Call<ResponseEnrollmentModel> ApiMethod(
            @Header("Authorization") String authorizationHeader,
            @Query("username") String username,
            @Query("program_uuid") String programUuid
    );
}
