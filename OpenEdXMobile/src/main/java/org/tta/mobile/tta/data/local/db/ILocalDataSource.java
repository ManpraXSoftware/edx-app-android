package org.tta.mobile.tta.data.local.db;

import org.tta.mobile.tta.data.local.db.table.Bookmark;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.ContentList;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.tta.data.local.db.table.StateContent;
import org.tta.mobile.tta.data.local.db.table.UnitStatus;
import org.tta.mobile.tta.data.local.db.table.User;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.model.library.CollectionConfigResponse;
import org.tta.mobile.user.Account;

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
    Content getContentById(long id);
    void insertContent(Content content);
    void insertOrIgnoreContent(Content content);
    Content getContentBySourceIdentity(String sourceIdentity);
    List<Content> getContentsBySourceId(long sourceId, int take, int skip);
    List<Content> getContents(int take, int skip);

    List<Feed> getFeeds(String username, int take, int skip);
    void insertFeeds(List<Feed> feeds);
    void deleteFeeds(String username);

    Category getCategoryBySourceId(long sourceId);

    List<ContentList> getContentListsByCategoryId(long categoryId);
    List<ContentList> getContentListsByCategoryIdAndMode(long categoryId, String mode);
    List<ContentList> getContentListsByRootCategory(String rootCategory);

    List<Source> getSources();

    List<Certificate> getAllCertificates(String username);
    Certificate getCertificate(String courseId, String username);
    void insertCertificates(List<Certificate> certificates);
    void insertCertificate(Certificate certificate);

    List<Notification> getAllNotifications(String username);
    List<Notification> getAllNotificationsInPage(String username, int take, int skip);
    List<Notification> getAllUncreatedNotifications(String username);
    List<Notification> getAllUnupdatedNotifications(String username);
    Notification getNotificationById(String username, long id);
    Notification getNotificationByCreatedTime(String username, long createdTime);
    Notification getNotificationByLocalId(String username, long localId);
    void insertNotification(Notification notification);
    void insertNotifications(List<Notification> notifications);
    void updateNotifications(List<Notification> notifications);
    void updateNotification(Notification notification);

    List<ContentStatus> getMyContentStatuses(String username);
    List<ContentStatus> getContentStatusesByContentIds(List<Long> contentIds, String username);
    ContentStatus getContentStatusByContentId(long contentId, String username);
    void insertContentStatus(ContentStatus contentStatus);
    void insertContentStatuses(List<ContentStatus> statuses);

    List<UnitStatus> getUnitStatusByCourse(String username, String courseId);
    void insertUnitStatuses(List<UnitStatus> statuses);

    Account getAccount(String username);
    void insertAccount(Account account);

    List<Content> getBookmarkedContents(long sourceId);
    Bookmark getBookmark(long contentId);
    void insertBookmark(Bookmark bookmark);
    void insertBookmarks(List<Bookmark> bookmarks);
    void deleteBookmark(Bookmark bookmark);
    void deleteAllBookmarks(long sourceId);

    List<Content> getStateContents(long sourceId);
    void insertStateContents(List<StateContent> stateContents);
    void deleteAllStateContents(long sourceId);
}