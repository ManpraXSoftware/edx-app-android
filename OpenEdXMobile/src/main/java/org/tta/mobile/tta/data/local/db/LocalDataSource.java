package org.tta.mobile.tta.data.local.db;

import org.tta.mobile.tta.data.local.db.table.Bookmark;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.ContentList;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.local.db.table.StateContent;
import org.tta.mobile.tta.data.local.db.table.UnitStatus;
import org.tta.mobile.tta.data.local.db.table.User;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.user.Account;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
public class LocalDataSource implements ILocalDataSource {

    private final TADatabase mAppDatabase;

    public LocalDataSource(TADatabase appDatabase) {
        this.mAppDatabase = appDatabase;
    }

    @Override
    public Observable<List<User>> getAllUsers() {
        return Observable.fromCallable(new Callable<List<User>>() {
            @Override
            public List<User> call() throws Exception {
                return mAppDatabase.userDao().getAll();
            }
        });
    }

    @Override
    public Observable<Boolean> insertUser(final User user) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.userDao().insert(user);
                return true;
            }
        });
    }

    @Override
    public void clear() {
        mAppDatabase.clearAllTables();
    }

    @Override
    public CollectionConfigResponse getConfiguration() {
        CollectionConfigResponse response = new CollectionConfigResponse();
        response.setCategory(mAppDatabase.categoryDao().getAll());
        response.setContent_list(mAppDatabase.contentListDao().getAll());
        response.setSource(mAppDatabase.sourceDao().getAll());

        return response;
    }

    @Override
    public void insertConfiguration(CollectionConfigResponse response) {
        mAppDatabase.categoryDao().insert(response.getCategory());
        mAppDatabase.contentListDao().insert(response.getContent_list());
        mAppDatabase.sourceDao().insert(response.getSource());
    }

    @Override
    public List<Content> getContents() {
        return mAppDatabase.contentDao().getAll();
    }

    @Override
    public void insertContents(List<Content> contents) {
        mAppDatabase.contentDao().insert(contents);
    }

    @Override
    public List<Feed> getFeeds(String username, int take, int skip) {
        return mAppDatabase.feedDao().getAll(username, take, skip);
    }

    @Override
    public void insertFeeds(List<Feed> feeds) {
        mAppDatabase.feedDao().insert(feeds);
    }

    @Override
    public void deleteFeeds(String username) {
        mAppDatabase.feedDao().deleteAll(username);
    }

    @Override
    public Category getCategoryBySourceId(long sourceId) {
        return mAppDatabase.categoryDao().getBySourceId(sourceId);
    }

    @Override
    public List<ContentList> getContentListsByCategoryId(long categoryId) {
        return mAppDatabase.contentListDao().getAllByCategoryId(categoryId);
    }

    @Override
    public List<ContentList> getContentListsByCategoryIdAndMode(long categoryId, String mode) {
        return mAppDatabase.contentListDao().getAllByCategoryIdAndMode(categoryId, mode);
    }

    @Override
    public List<ContentList> getContentListsByRootCategory(String rootCategory) {
        return mAppDatabase.contentListDao().getByRootCategory(rootCategory);
    }

    @Override
    public List<Source> getSources() {
        return mAppDatabase.sourceDao().getAll();
    }

    @Override
    public Content getContentById(long id) {
        return mAppDatabase.contentDao().getById(id);
    }

    @Override
    public void insertContent(Content content) {
        mAppDatabase.contentDao().insert(content);
    }

    @Override
    public void insertOrIgnoreContent(Content content) {
        mAppDatabase.contentDao().insertOrIgnore(content);
    }

    @Override
    public Content getContentBySourceIdentity(String sourceIdentity) {
        return mAppDatabase.contentDao().getBySourceIdentity(sourceIdentity);
    }

    @Override
    public List<Certificate> getAllCertificates(String username) {
        return mAppDatabase.certificateDao().getAll(username);
    }

    @Override
    public Certificate getCertificate(String courseId, String username) {
        return mAppDatabase.certificateDao().getByCourseId(courseId, username);
    }

    @Override
    public void insertCertificates(List<Certificate> certificates) {
        mAppDatabase.certificateDao().insert(certificates);
    }

    @Override
    public void insertCertificate(Certificate certificate) {
        mAppDatabase.certificateDao().insert(certificate);
    }

    @Override
    public List<Notification> getAllNotifications(String username) {
        return mAppDatabase.notificationDao().getAll(username);
    }

    @Override
    public List<Notification> getAllNotificationsInPage(String username, int take, int skip) {
        return mAppDatabase.notificationDao().getAllInPage(username, take, skip);
    }

    @Override
    public List<Notification> getAllUncreatedNotifications(String username) {
        return mAppDatabase.notificationDao().getAllUncreated(username);
    }

    @Override
    public List<Notification> getAllUnupdatedNotifications(String username) {
        return mAppDatabase.notificationDao().getAllUnupdated(username);
    }

    @Override
    public Notification getNotificationById(String username, long id) {
        return mAppDatabase.notificationDao().getById(username, id);
    }

    @Override
    public Notification getNotificationByCreatedTime(String username, long createdTime) {
        return mAppDatabase.notificationDao().getByCreatedTime(username, createdTime);
    }

    @Override
    public Notification getNotificationByLocalId(String username, long localId) {
        return mAppDatabase.notificationDao().getByLocalId(username, localId);
    }

    @Override
    public void insertNotification(Notification notification) {
        mAppDatabase.notificationDao().insert(notification);
    }

    @Override
    public void insertNotifications(List<Notification> notifications) {
        mAppDatabase.notificationDao().insert(notifications);
    }

    @Override
    public void updateNotifications(List<Notification> notifications) {
        mAppDatabase.notificationDao().update(notifications);
    }

    @Override
    public void updateNotification(Notification notification) {
        mAppDatabase.notificationDao().update(notification);
    }

    @Override
    public List<ContentStatus> getMyContentStatuses(String username) {
        return mAppDatabase.contentStatusDao().getAll(username);
    }

    @Override
    public List<ContentStatus> getContentStatusesByContentIds(List<Long> contentIds, String username) {
        return mAppDatabase.contentStatusDao().getAllByContentIds(contentIds, username);
    }

    @Override
    public ContentStatus getContentStatusByContentId(long contentId, String username) {
        return mAppDatabase.contentStatusDao().getByContentId(contentId, username);
    }

    @Override
    public void insertContentStatus(ContentStatus contentStatus) {
        mAppDatabase.contentStatusDao().insert(contentStatus);
    }

    @Override
    public void insertContentStatuses(List<ContentStatus> statuses) {
        mAppDatabase.contentStatusDao().insert(statuses);
    }

    @Override
    public List<UnitStatus> getUnitStatusByCourse(String username, String courseId) {
        return mAppDatabase.unitStatusDao().getAllByCourse(username, courseId);
    }

    @Override
    public void insertUnitStatuses(List<UnitStatus> statuses) {
        mAppDatabase.unitStatusDao().insert(statuses);
    }

    @Override
    public Account getAccount(String username) {
        return mAppDatabase.accountDao().getByUsername(username);
    }

    @Override
    public void insertAccount(Account account) {
        mAppDatabase.accountDao().insert(account);
    }

    @Override
    public List<Content> getBookmarkedContents(long sourceId) {
        return mAppDatabase.bookmarkDao().getAllContents(sourceId);
    }

    @Override
    public Bookmark getBookmark(long contentId) {
        return mAppDatabase.bookmarkDao().getByContentId(contentId);
    }

    @Override
    public void insertBookmark(Bookmark bookmark) {
        mAppDatabase.bookmarkDao().insert(bookmark);
    }

    @Override
    public void insertBookmarks(List<Bookmark> bookmarks) {
        mAppDatabase.bookmarkDao().insert(bookmarks);
    }

    @Override
    public void deleteBookmark(Bookmark bookmark) {
        mAppDatabase.bookmarkDao().delete(bookmark);
    }

    @Override
    public void deleteAllBookmarks() {
        mAppDatabase.bookmarkDao().deleteAll();
    }

    @Override
    public List<Content> getStateContents(long sourceId) {
        return mAppDatabase.stateContentDao().getAllContents(sourceId);
    }

    @Override
    public void insertStateContents(List<StateContent> stateContents) {
        mAppDatabase.stateContentDao().insert(stateContents);
    }

    @Override
    public void deleteAllStateContents() {
        mAppDatabase.stateContentDao().deleteAll();
    }
}
