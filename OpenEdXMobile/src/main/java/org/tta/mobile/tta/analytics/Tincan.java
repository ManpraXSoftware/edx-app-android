package org.tta.mobile.tta.analytics;

/**
 * Created by JARVICE on 07-06-2018.
 */

public class Tincan {
    //primary key in Db
    public String analytic_id;
    public String course_id;
    public String metadata;
    public String action_id;
    public String nav;
    public String source_id;
    public long content_id;

    @Override
    public String toString() {
        return "Tincan{" +
                "analytic_id='" + analytic_id + '\'' +
                ", course_id='" + course_id + '\'' +
                ", metadata='" + metadata + '\'' +
                ", action_id='" + action_id + '\'' +
                ", nav='" + nav + '\'' +
                ", source_id='" + source_id + '\'' +
                ", content_id=" + content_id +
                '}';
    }
}
