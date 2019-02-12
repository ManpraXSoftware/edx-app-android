package org.edx.mobile.tta.data.local.db;

import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.data.local.db.table.User;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
public interface ILocalDataSource {
    Observable<List<User>> getAllUsers();
    Observable<Boolean> insertUser(final User user);

    void clear();

    CollectionConfigResponse getConfiguration();
    void insertConfiguration(CollectionConfigResponse response);

    List<Content> getContents();
    void insertContents(List<Content> contents);

    List<Feed> getFeeds();
    void insertFeeds(List<Feed> feeds);
}