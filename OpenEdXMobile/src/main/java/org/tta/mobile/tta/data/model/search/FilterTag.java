package org.tta.mobile.tta.data.model.search;

import androidx.annotation.NonNull;

import java.util.List;

public class FilterTag {

    private long id;

    private String value;

    private String display_name;

    private List<TagSourceCount> sources;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public List<TagSourceCount> getSources() {
        return sources;
    }

    public void setSources(List<TagSourceCount> sources) {
        this.sources = sources;
    }

    @NonNull
    @Override
    public String toString() {
        return display_name != null && !display_name.equals("") ? display_name : value;
    }
}
