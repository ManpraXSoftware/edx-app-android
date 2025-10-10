package org.edx.mobile.discovery.model;
import java.util.List;
import java.io.Serializable;

public class SearchResultModel {
        private String course_id;
        private String course_name;
        private String course_key;
        private String course_lang;
        //private List<String> program_name;
        private String subject;
        private String content_text;
        private List<String> tags;
        private ProgramDetails program_details;

        // Getters and Setters
        public String getCourseId() {
            return course_id;
        }

        public void setCourseId(String course_id) {
            this.course_id = course_id;
        }

        public String getCourseName() {
            return course_name;
        }

        public void setCourseName(String course_name) {
            this.course_name = course_name;
        }

        public String getCourseKey() {
            return course_key;
        }

        public void setCourseKey(String course_key) {
            this.course_key = course_key;
        }

        public String getCourseLang() {
            return course_lang;
        }

        public void setCourseLang(String course_lang) {
            this.course_lang = course_lang;
        }

        /*public List<String> getProgramName() {
            return program_name;
        }

        public void setProgramName(List<String> program_name) {
            this.program_name = program_name;
        }*/

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContentText() {
            return content_text;
        }

        public void setContentText(String content_text) {
            this.content_text = content_text;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public ProgramDetails getProgramDetails() {
            return program_details;
        }

        public void setProgramDetails(ProgramDetails program_details) {
            this.program_details = program_details;
        }

        // Inner class for ProgramDetails
        public static class ProgramDetails implements Serializable {
            private List<String> programs;
            private List<String> program_id;
            private List<Tag> tags;

            public List<String> getPrograms() {
                return programs;
            }

            public void setPrograms(List<String> programs) {
                this.programs = programs;
            }

            public List<String> getProgramId() {
                return program_id;
            }

            public void setProgramId(List<String> program_id) {
                this.program_id = program_id;
            }

            public List<Tag> getTags() {
                return tags;
            }

            public void setTags(List<Tag> tags) {
                this.tags = tags;
            }

            // Inner class for Tag
            public static class Tag implements Serializable {
                private String program_name;
                private String program_id;
                private List<String> tags;

                public String getProgramName() {
                    return program_name;
                }

                public void setProgramName(String program_name) {
                    this.program_name = program_name;
                }

                public String getProgramId() {
                    return program_id;
                }

                public void setProgramId(String program_id) {
                    this.program_id = program_id;
                }

                public List<String> getTags() {
                    return tags;
                }

                public void setTags(List<String> tags) {
                    this.tags = tags;
                }
            }
        }

    private String unit_id;
    private boolean is_enroll;

    public String getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(String unit_id) {
        this.unit_id = unit_id;
    }

    public boolean isIs_enroll() {
        return is_enroll;
    }

    public void setIs_enroll(boolean is_enroll) {
        this.is_enroll = is_enroll;
    }
    }
