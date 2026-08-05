package org.edx.mobile.view;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.VideoBlockModel;

import java.io.IOException;

import static org.edx.mobile.http.util.CallUtil.executeStrict;

public class CourseUnitYoutubeVideoFragmentTest extends UiTest {

    private static final String YOUTUBE_IN_APP_PLAYER = "YOUTUBE_IN_APP_PLAYER";

    @Override
    protected JsonObject generateConfigProperties() throws IOException {
        // Add the mock youtube api key in the test config properties
        final JsonObject properties = super.generateConfigProperties();
        properties.add(YOUTUBE_IN_APP_PLAYER, getYoutubeMockConfig());
        return properties;
    }

    private JsonElement getYoutubeMockConfig() {
        final String serializedData = "{\"ENABLED\":\"True\", \"API_KEY\":\"TEST_YOUTUBE_API_KEY\"}";
        return new JsonParser().parse(serializedData);
    }

    VideoBlockModel getVideoUnit() {
        final EnrolledCoursesResponse courseData;
        try {
            courseData = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final String courseId = courseData.getCourse().getId();
        final CourseStructureV1Model model;
        final CourseComponent courseComponent;
        try {
            model = executeStrict(courseAPI.getCourseStructure(config.getApiUrlVersionConfig().getBlocksApiVersion(), courseId));
            courseComponent = (CourseComponent) CourseAPI.normalizeCourseStructure(model, courseId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return courseComponent.getVideos().get(0);
    }

    @org.junit.Test
    public void initializeTest() {
        final CourseUnitYoutubePlayerFragment fragment = CourseUnitYoutubePlayerFragment.newInstance(getVideoUnit());
        org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startVisibleFragment(fragment, roboguice.activity.RoboFragmentActivity.class, android.R.id.content);
        org.junit.Assert.assertTrue(fragment.getRetainInstance());

        final android.view.View view = fragment.getView();
        org.junit.Assert.assertNotNull(view);
    }
}
