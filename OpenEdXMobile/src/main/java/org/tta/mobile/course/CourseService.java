package org.tta.mobile.course;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.tta.mobile.event.EnrolledInCourseEvent;
import org.tta.mobile.http.callback.ErrorHandlingCallback;
import org.tta.mobile.http.provider.RetrofitProvider;
import org.tta.mobile.model.api.EnrolledCoursesResponse;
import org.tta.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.tta.mobile.model.api.VideoResponseModel;
import org.tta.mobile.model.course.CourseStructureV1Model;
import org.tta.mobile.view.common.TaskProgressCallback;

import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CourseService {
    /**
     * A RoboGuice TaProvider implementation for CourseService.
     */
    class Provider implements com.google.inject.Provider<CourseService> {
        @Inject
        RetrofitProvider retrofitProvider;

        @Override
        public CourseService get() {
            return retrofitProvider.getWithOfflineCache().create(CourseService.class);
        }
    }

    /**
     * @return Enrolled courses of given user.
     */
    @GET("/api/mobile/v0.5/users/{username}/course_enrollments")
    Call<List<EnrolledCoursesResponse>> getEnrolledCourses(@Path("username") final String username,
                                                           @Query("org") final String org);

    /**
     * @return List of videos in a particular course.
     */
    @GET("/api/mobile/v0.5/video_outlines/courses/{course_id}")
    Call<List<VideoResponseModel>> getVideosByCourseId(@Path("course_id") final String courseId);

    @PATCH("/api/mobile/v0.5/users/{username}/course_status_info/{course_id}")
    Call<SyncLastAccessedSubsectionResponse> syncLastAccessedSubsection(
            @Path("username") final String username,
            @Path("course_id") final String courseId,
            @Body final SyncLastAccessedSubsectionBody body);

    @GET("/api/mobile/v0.5/users/{username}/course_status_info/{course_id}")
    Call<SyncLastAccessedSubsectionResponse> getLastAccessedSubsection(
            @Path("username") final String username,
            @Path("course_id") final String courseId);

    @POST("/api/enrollment/v1/enrollment")
    Call<ResponseBody> enrollInACourse(@Body final EnrollBody body);

    @GET("/api/courses/v1/blocks/?" +
            "depth=all&" +
            "requested_fields=graded,format,student_view_multi_device,due&" +
            "student_view_data=video,discussion&" +
            "block_counts=video&" +
            "nav_depth=3")
    Call<CourseStructureV1Model> getCourseStructure(
            @Header("Cache-Control") String cacheControlHeaderParam,
            @Query("username") final String username,
            @Query("course_id") final String courseId);

    final class SyncLastAccessedSubsectionBody {
        @NonNull
        private final String lastVisitedModuleId;
        @NonNull
        private final Date modificationDate = new Date();

        public SyncLastAccessedSubsectionBody(@NonNull final String lastVisitedModuleId) {
            this.lastVisitedModuleId = lastVisitedModuleId;
        }
    }

    final class EnrollBody {
        @NonNull
        private final CourseDetails courseDetails;

        public EnrollBody(@NonNull final String courseId, final boolean emailOptIn) {
            courseDetails = new CourseDetails(courseId, emailOptIn);
        }

        private static class CourseDetails {
            @NonNull
            private final String courseId;
            private final boolean emailOptIn;

            CourseDetails(@NonNull final String courseId, final boolean emailOptIn) {
                this.courseId = courseId;
                this.emailOptIn = emailOptIn;
            }
        }
    }

    class EnrollCallback extends ErrorHandlingCallback<ResponseBody> {
        public EnrollCallback(@NonNull final Context context) {
            super(context);
        }

        public EnrollCallback(@NonNull final Context context,
                              @Nullable final TaskProgressCallback progressCallback) {
            super(context, progressCallback);
        }

        @Override
        protected void onResponse(@NonNull final ResponseBody responseBody) {
            EventBus.getDefault().post(new EnrolledInCourseEvent());
        }
    }
}
