package org.edx.mobile.programs;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public class Programs {
    private String program_title;
    private String program_uuid;

    public ResumePrograms getResumePrograms() {
        return resume_program;
    }

    public void setResumePrograms(ResumePrograms resumePrograms) {
        this.resume_program = resumePrograms;
    }

    private ResumePrograms resume_program;

    public String getProgram_title() {
        return program_title;
    }

    public void setProgram_title(String program_title) {
        this.program_title = program_title;
    }

    public String getProgram_uuid() {
        return program_uuid;
    }

    public void setProgram_uuid(String program_uuid) {
        this.program_uuid = program_uuid;
    }

    public String getConverted_program_title() {
        return converted_program_title;
    }

    public void setConverted_program_title(String converted_program_title) {
        this.converted_program_title = converted_program_title;
    }

    public List<MyProgramTags> getTags() {
        return tags;
    }

    public void setTags(List<MyProgramTags> tags) {
        this.tags = tags;
    }

    private List<MyProgramTags> tags;

    public ResumePrograms getResume_program() {
        return resume_program;
    }

    public void setResume_program(ResumePrograms resume_program) {
        this.resume_program = resume_program;
    }
    @SerializedName("converted_program_title")
    private String converted_program_title;

    @SerializedName("program_language")
    private String program_language;

    public String getProgram_language() {
        return program_language;
    }

    public void setProgram_language(String program_language) {
        this.program_language = program_language;
    }
}
