package org.edx.mobile.programs;

import java.util.HashMap;

public class MyProgramListModel {


    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getProgramUUid() {
        return programUUid;
    }

    public void setProgramUUid(String programUUid) {
        this.programUUid = programUUid;
    }

    private String programName;
    private String tagName;

    public String getConvertedTagName() {
        return convertedTagName;
    }

    public void setConvertedTagName(String convertedTagName) {
        this.convertedTagName = convertedTagName;
    }

    private String convertedTagName;

    private String programUUid;

    private String program_language;


    public ResumePrograms getResume_program() {
        return resume_program;
    }

    public void setResume_program(ResumePrograms resume_program) {
        this.resume_program = resume_program;
    }

    private ResumePrograms resume_program;

    public String getProgram_language() {
        return program_language;
    }

    public void setProgram_language(String program_language) {
        this.program_language = program_language;
    }
}
