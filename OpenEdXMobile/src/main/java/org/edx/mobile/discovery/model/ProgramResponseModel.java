package org.edx.mobile.discovery.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProgramResponseModel {

    private int count;
    private String next;
    private String previous;

    @SerializedName("results")
    private List<Program> programs;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Program> getPrograms() {
        return programs;
    }

    public static class Program {
        private String title;
        private String cardImageUrl;

        @SerializedName("authoring_organization_uuids")
        private List<String> authoringOrganizationUuids;

        @SerializedName("content_type")
        private String contentType;

        @SerializedName("min_hours_effort_per_week")
        private String minHoursEffortPerWeek;

        @SerializedName("marketing_url")
        private String marketingUrl;

        @SerializedName("weeks_to_complete_min")
        private String weeksToCompleteMin;

        private String subtitle;
        private String uuid;

        @SerializedName("aggregation_key")
        private String aggregationKey;

        @SerializedName("search_card_display")
        private List<String> searchCardDisplay;

        private boolean published;

        @SerializedName("program_subjects")
        private List<String> programSubjects;

        private String partner;

        @SerializedName("weeks_to_complete_max")
        private String weeksToCompleteMax;

        @SerializedName("staff_uuids")
        private List<String> staffUuids;

        private boolean hidden;

        @SerializedName("program_language")
        private String programLanguage;

        private List<String> language;
        private String type;

        @SerializedName("program_topics")
        private List<String> programTopics;

        @SerializedName("is_program_eligible_for_one_click_purchase")
        private boolean isProgramEligibleForOneClickPurchase;

        @SerializedName("max_hours_effort_per_week")
        private String maxHoursEffortPerWeek;

        @SerializedName("authoring_organizations")
        private List<AuthoringOrganization> authoringOrganizations;

        @SerializedName("subject_uuids")
        private List<String> subjectUuids;

        private String status;

        public String getTitle() {
            return title;
        }

        public String getCardImageUrl() {
            return cardImageUrl;
        }

        public List<String> getAuthoringOrganizationUuids() {
            return authoringOrganizationUuids;
        }

        public String getContentType() {
            return contentType;
        }

        public String getMinHoursEffortPerWeek() {
            return minHoursEffortPerWeek;
        }

        public String getMarketingUrl() {
            return marketingUrl;
        }

        public String getWeeksToCompleteMin() {
            return weeksToCompleteMin;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getUuid() {
            return uuid;
        }

        public String getAggregationKey() {
            return aggregationKey;
        }

        public List<String> getSearchCardDisplay() {
            return searchCardDisplay;
        }

        public boolean isPublished() {
            return published;
        }

        public List<String> getProgramSubjects() {
            return programSubjects;
        }

        public String getPartner() {
            return partner;
        }

        public String getWeeksToCompleteMax() {
            return weeksToCompleteMax;
        }

        public List<String> getStaffUuids() {
            return staffUuids;
        }

        public boolean isHidden() {
            return hidden;
        }

        public String getProgramLanguage() {
            return programLanguage;
        }

        public List<String> getLanguage() {
            return language;
        }

        public String getType() {
            return type;
        }

        public List<String> getProgramTopics() {
            return programTopics;
        }

        public boolean isProgramEligibleForOneClickPurchase() {
            return isProgramEligibleForOneClickPurchase;
        }

        public String getMaxHoursEffortPerWeek() {
            return maxHoursEffortPerWeek;
        }

        public List<AuthoringOrganization> getAuthoringOrganizations() {
            return authoringOrganizations;
        }

        public List<String> getSubjectUuids() {
            return subjectUuids;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class AuthoringOrganization {
        private String key;
        private List<String> tags;
        private String uuid;

        @SerializedName("banner_image_url")
        private String bannerImageUrl;

        private String description;

        @SerializedName("auto_generate_course_run_keys")
        private boolean autoGenerateCourseRunKeys;

        private String slug;

        @SerializedName("logo_image_url")
        private String logoImageUrl;

        @SerializedName("homepage_url")
        private String homepageUrl;

        @SerializedName("certificate_logo_image_url")
        private String certificateLogoImageUrl;

        @SerializedName("marketing_url")
        private String marketingUrl;

        private String name;

        public String getKey() {
            return key;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getUuid() {
            return uuid;
        }

        public String getBannerImageUrl() {
            return bannerImageUrl;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAutoGenerateCourseRunKeys() {
            return autoGenerateCourseRunKeys;
        }

        public String getSlug() {
            return slug;
        }

        public String getLogoImageUrl() {
            return logoImageUrl;
        }

        public String getHomepageUrl() {
            return homepageUrl;
        }

        public String getCertificateLogoImageUrl() {
            return certificateLogoImageUrl;
        }

        public String getMarketingUrl() {
            return marketingUrl;
        }

        public String getName() {
            return name;
        }
    }
}
