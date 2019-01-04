package org.edx.mobile.tta.data.local.db;

import org.edx.mobile.tta.data.local.db.table.User;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.data.local.db.table.Content;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
public interface ILocalDataSource {
    Observable<List<User>> getAllUsers();
    Observable<Boolean> insertUser(final User user);

    ConfigurationResponse getConfiguration();
    void insertConfiguration(ConfigurationResponse response);

    List<Content> getContents();
    void insertContents(List<Content> contents);
}