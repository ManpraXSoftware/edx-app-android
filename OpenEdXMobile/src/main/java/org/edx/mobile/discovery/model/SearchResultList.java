package org.edx.mobile.discovery.model;
import java.util.List;

public class SearchResultList {
    private String title;
    private String key;
    private String uuid;
    private String fullDescription;
    private List<String> organizations;
    private List<CourseRun> courseRuns;
    private List<String> subjects;
    private List<String> seatTypes;
    private String shortDescription;
    private String imageUrl;
    private String contentType;
    private List<String> languages;
    private String aggregationKey;
    private String cardImageUrl;
    private SearchProgramDetails programDetails;

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public List<String> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public List<CourseRun> getCourseRuns() {
        return courseRuns;
    }

    public void setCourseRuns(List<CourseRun> courseRuns) {
        this.courseRuns = courseRuns;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public List<String> getSeatTypes() {
        return seatTypes;
    }

    public void setSeatTypes(List<String> seatTypes) {
        this.seatTypes = seatTypes;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getAggregationKey() {
        return aggregationKey;
    }

    public void setAggregationKey(String aggregationKey) {
        this.aggregationKey = aggregationKey;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public SearchProgramDetails getProgramDetails() {
        return programDetails;
    }

    public void setProgramDetails(SearchProgramDetails programDetails) {
        this.programDetails = programDetails;
    }

    // Inner class for CourseRun
    public static class CourseRun {
        private String availability;
        private String end;
        private Integer weeksToComplete;
        private String modified;
        private double firstEnrollablePaidSeatPrice;
        private String pacingType;
        private boolean isEnrollable;
        private String key;
        private String start;
        private int estimatedHours;
        private String enrollmentEnd;
        private String goLiveDate;
        private Integer minEffort;
        private Integer maxEffort;
        private String enrollmentStart;
        private String language;
        private String enrollmentMode;

        // Getters and Setters for CourseRun
        public String getAvailability() {
            return availability;
        }

        public void setAvailability(String availability) {
            this.availability = availability;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public Integer getWeeksToComplete() {
            return weeksToComplete;
        }

        public void setWeeksToComplete(Integer weeksToComplete) {
            this.weeksToComplete = weeksToComplete;
        }

        public String getModified() {
            return modified;
        }

        public void setModified(String modified) {
            this.modified = modified;
        }

        public double getFirstEnrollablePaidSeatPrice() {
            return firstEnrollablePaidSeatPrice;
        }

        public void setFirstEnrollablePaidSeatPrice(double firstEnrollablePaidSeatPrice) {
            this.firstEnrollablePaidSeatPrice = firstEnrollablePaidSeatPrice;
        }

        public String getPacingType() {
            return pacingType;
        }

        public void setPacingType(String pacingType) {
            this.pacingType = pacingType;
        }

        public boolean isEnrollable() {
            return isEnrollable;
        }

        public void setEnrollable(boolean isEnrollable) {
            this.isEnrollable = isEnrollable;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public int getEstimatedHours() {
            return estimatedHours;
        }

        public void setEstimatedHours(int estimatedHours) {
            this.estimatedHours = estimatedHours;
        }

        public String getEnrollmentEnd() {
            return enrollmentEnd;
        }

        public void setEnrollmentEnd(String enrollmentEnd) {
            this.enrollmentEnd = enrollmentEnd;
        }

        public String getGoLiveDate() {
            return goLiveDate;
        }

        public void setGoLiveDate(String goLiveDate) {
            this.goLiveDate = goLiveDate;
        }

        public Integer getMinEffort() {
            return minEffort;
        }

        public void setMinEffort(Integer minEffort) {
            this.minEffort = minEffort;
        }

        public Integer getMaxEffort() {
            return maxEffort;
        }

        public void setMaxEffort(Integer maxEffort) {
            this.maxEffort = maxEffort;
        }

        public String getEnrollmentStart() {
            return enrollmentStart;
        }

        public void setEnrollmentStart(String enrollmentStart) {
            this.enrollmentStart = enrollmentStart;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getEnrollmentMode() {
            return enrollmentMode;
        }

        public void setEnrollmentMode(String enrollmentMode) {
            this.enrollmentMode = enrollmentMode;
        }
    }

    // Inner class for SearchProgramDetails
    public static class SearchProgramDetails {
        private List<String> programs;
        private List<String> programId;
        private List<Tag> tags;

        // Getters and Setters for SearchProgramDetails
        public List<String> getPrograms() {
            return programs;
        }

        public void setPrograms(List<String> programs) {
            this.programs = programs;
        }

        public List<String> getProgramId() {
            return programId;
        }

        public void setProgramId(List<String> programId) {
            this.programId = programId;
        }

        public List<Tag> getTags() {
            return tags;
        }

        public void setTags(List<Tag> tags) {
            this.tags = tags;
        }

        // Inner class for Tag
        public static class Tag {
            private String programName;
            private List<String> tags;
            private String programId;

            // Getters and Setters for Tag
            public String getProgramName() {
                return programName;
            }

            public void setProgramName(String programName) {
                this.programName = programName;
            }

            public List<String> getTags() {
                return tags;
            }

            public void setTags(List<String> tags) {
                this.tags = tags;
            }

            public String getProgramId() {
                return programId;
            }

            public void setProgramId(String programId) {
                this.programId = programId;
            }
        }
    }
}
