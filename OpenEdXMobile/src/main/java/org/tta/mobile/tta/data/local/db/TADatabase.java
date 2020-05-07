package org.tta.mobile.tta.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.tta.mobile.tta.data.local.db.dao.AccountDao;
import org.tta.mobile.tta.data.local.db.dao.BookmarkDao;
import org.tta.mobile.tta.data.local.db.dao.CategoryDao;
import org.tta.mobile.tta.data.local.db.dao.CertificateDao;
import org.tta.mobile.tta.data.local.db.dao.ContentDao;
import org.tta.mobile.tta.data.local.db.dao.ContentListDao;
import org.tta.mobile.tta.data.local.db.dao.ContentStatusDao;
import org.tta.mobile.tta.data.local.db.dao.FeedDao;
import org.tta.mobile.tta.data.local.db.dao.NotificationDao;
import org.tta.mobile.tta.data.local.db.dao.PendingCertificateDao;
import org.tta.mobile.tta.data.local.db.dao.SourceDao;
import org.tta.mobile.tta.data.local.db.dao.StateContentDao;
import org.tta.mobile.tta.data.local.db.dao.UnitStatusDao;
import org.tta.mobile.tta.data.local.db.dao.UserDao;
import org.tta.mobile.tta.data.local.db.table.Bookmark;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.local.db.table.Notification;
import org.tta.mobile.tta.data.local.db.table.PendingCertificate;
import org.tta.mobile.tta.data.local.db.table.StateContent;
import org.tta.mobile.tta.data.local.db.table.UnitStatus;
import org.tta.mobile.tta.data.local.db.table.User;
import org.tta.mobile.tta.data.local.db.table.Category;
import org.tta.mobile.tta.data.local.db.table.Content;
import org.tta.mobile.tta.data.local.db.table.ContentList;
import org.tta.mobile.tta.data.local.db.table.Source;
import org.tta.mobile.user.Account;

@Database(
        entities = {
                User.class,
                Category.class,
                Content.class,
                ContentList.class,
                Source.class,
                Feed.class,
                Certificate.class,
                Notification.class,
                ContentStatus.class,
                UnitStatus.class,
                Account.class,
                Bookmark.class,
                StateContent.class,
                PendingCertificate.class
        },
        version = 8,
        exportSchema = false
)
@TypeConverters({DbTypeConverters.class})
public abstract class TADatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ContentDao contentDao();
    public abstract ContentListDao contentListDao();
    public abstract SourceDao sourceDao();
    public abstract FeedDao feedDao();
    public abstract CertificateDao certificateDao();
    public abstract NotificationDao notificationDao();
    public abstract ContentStatusDao contentStatusDao();
    public abstract UnitStatusDao unitStatusDao();
    public abstract AccountDao accountDao();
    public abstract BookmarkDao bookmarkDao();
    public abstract StateContentDao stateContentDao();
    public abstract PendingCertificateDao pendingCertificateDao();

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `pending_certificate` (`course_id` TEXT NOT NULL, "
                    + "`course_name` TEXT, `image` TEXT, `username` TEXT NOT NULL, PRIMARY KEY(`course_id`))");
        }
    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `pending_certificate` (`course_id` TEXT NOT NULL, "
                    + "`course_name` TEXT, `image` TEXT, `username` TEXT NOT NULL, PRIMARY KEY(`course_id`))");
        }
    };
}
