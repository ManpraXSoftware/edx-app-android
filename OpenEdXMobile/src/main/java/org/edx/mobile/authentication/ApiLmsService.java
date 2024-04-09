package org.edx.mobile.authentication;

import org.edx.mobile.discovery.model.ResponseEnrollmentModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static org.edx.mobile.http.constants.ApiConstants.URL_ENROLL_CHECK;
import static org.edx.mobile.http.constants.ApiConstants.URL_PARTICULAR_COURSE;

public interface ApiLmsService {
    @GET(URL_ENROLL_CHECK)
    Call<ResponseEnrollmentModel> unrollCheck(
            @Header("Authorization") String authorizationHeader,
            @Query("username") String username,
            @Query("program_uuid") String programUuid
    );

    @GET(URL_PARTICULAR_COURSE)
    Call<ArrayList<EnrolledCoursesResponse>> getParticularCourse(
            @Header("Authorization") String authorizationHeader,
            @Path("userId") String userId,
            @Query("course_id") String courseId
    );
}
