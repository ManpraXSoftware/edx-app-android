package org.edx.mobile.discovery.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TagTermResult {
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @SerializedName("term")
    @Expose
    private String term;

    public String getConverted_term() {
        return converted_term;
    }

    public void setConverted_term(String converted_term) {
        this.converted_term = converted_term;
    }

    @SerializedName("converted_term")
    @Expose
    private String converted_term;

    @SerializedName("count")
    @Expose
    private int count;
}
