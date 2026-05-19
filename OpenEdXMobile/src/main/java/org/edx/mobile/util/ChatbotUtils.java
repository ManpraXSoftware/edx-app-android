package org.edx.mobile.util;

import org.edx.mobile.model.ChatbotModal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ChatbotUtils {

    public static boolean isCourseIdPresentInData(ChatbotModal chatbotModal, String courseId) {
        if (chatbotModal == null || chatbotModal.getData() == null || courseId == null) {
            return false;
        }
        String data = chatbotModal.getData();
       return  data.contains(courseId);
    }

    private static final String COURSE_IDS_SECTION = "Relevant Course IDs:";
    private static final Pattern COURSE_ID_PATTERN = Pattern.compile("course-v1:[\\w+\\-]+");

    // ... (other methods remain the same)

    public static String removeCourseIdsSection(ChatbotModal chatbotModal) {
        if (chatbotModal == null || chatbotModal.getData() == null) {
            return "";
        }

        String data = chatbotModal.getData();
        StringBuilder result = new StringBuilder(data);

        // Remove the "Relevant Course IDs:" section
        int courseIdsSectionIndex = result.indexOf(COURSE_IDS_SECTION);
        if (courseIdsSectionIndex != -1) {
            int endIndex = result.indexOf("\n\n", courseIdsSectionIndex);
            if (endIndex == -1) {
                endIndex = result.length();
            }
            result.delete(courseIdsSectionIndex, endIndex);
        }

        // Remove individual course IDs
        List<String> courseIds = chatbotModal.getCourseIds();
        if (courseIds != null) {
            for (String courseId : courseIds) {
                if (courseId != null) {
                    int index;
                    while ((index = result.indexOf(courseId)) != -1) {
                        result.delete(index, index + courseId.length());
                    }
                }
            }
        }



        String relevantCourseIds = "Relevant Course IDs";
        int idx;
        while ((idx = result.indexOf(relevantCourseIds)) != -1) {
            result.delete(idx, idx + relevantCourseIds.length());
        }

        String relevantCourseId = "Relevant Course ID";
        int index;
        while ((index = result.indexOf(relevantCourseId)) != -1) {
            result.delete(index, index + relevantCourseId.length());
        }

        // Clean up any extra whitespace or newlines
        return result.toString().trim();
    }
    public static List<String> extractCourseIdsFromData(ChatbotModal chatbotModal) {
        if (chatbotModal == null || chatbotModal.getData() == null) {
            return new ArrayList<>();
        }

        String data = chatbotModal.getData();
        List<String> allCourseIds = chatbotModal.getCourseIds();

        if (allCourseIds == null || allCourseIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> presentCourseIds = new ArrayList<>();

        for (String courseId : allCourseIds) {
            if (data.contains(courseId)) {
                presentCourseIds.add(courseId);
            }
        }

        return presentCourseIds;
    }
}
