package org.tta.mobile.tta.analytics;

import java.util.ArrayList;

/**
 * Created by JARVICE on 06-06-2018.
 */

public class TincanRequest {
    public String user_id;
    public int version;
    ArrayList<Tincan> tincanObj=new ArrayList<>();

    @Override
    public String toString() {
        return "TincanRequest{" +
                "user_id='" + user_id + '\'' +
                ", version=" + version +
                ", tincanObj=" + tincanObj +
                '}';
    }
}
