package org.tta.mobile.tta.wordpress_client.model;

/**
 * Created by JARVICE on 02-01-2018.
 */

public class CustomComment {
    public Long author;
    public String author_ip;
    public String author_url ;
    public String author_user_agent;
    public String content;
    public String date;
    public String date_gmt;
    public int parent;
    public Long post;

    @Override
    public String toString() {
        return "CustomComment{" +
                "author=" + author +
                ", author_ip='" + author_ip + '\'' +
                ", author_url='" + author_url + '\'' +
                ", author_user_agent='" + author_user_agent + '\'' +
                ", content='" + content + '\'' +
                ", date='" + date + '\'' +
                ", date_gmt='" + date_gmt + '\'' +
                ", parent=" + parent +
                ", post=" + post +
                '}';
    }
}
