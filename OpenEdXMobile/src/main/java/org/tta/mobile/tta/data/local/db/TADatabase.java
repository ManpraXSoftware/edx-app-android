package org.tta.mobile.tta.data.local.db;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import org.tta.mobile.tta.data.local.db.dao.AccountDao;
import org.tta.mobile.tta.data.local.db.dao.CategoryDao;
import org.tta.mobile.tta.data.local.db.dao.CertificateDao;
import org.tta.mobile.tta.data.local.db.dao.ContentDao;
import org.tta.mobile.tta.data.local.db.dao.ContentListDao;
import org.tta.mobile.tta.data.local.db.dao.ContentStatusDao;
import org.tta.mobile.tta.data.local.db.dao.FeedDao;
import org.tta.mobile.tta.data.local.db.dao.NotificationDao;
import org.tta.mobile.tta.data.local.db.dao.SourceDao;
import org.tta.mobile.tta.data.local.db.dao.UnitStatusDao;
import org.tta.mobile.tta.data.local.db.dao.UserDao;
import org.tta.mobile.tta.data.local.db.table.Certificate;
import org.tta.mobile.tta.data.local.db.table.ContentStatus;
import org.tta.mobile.tta.data.local.db.table.Feed;
import org.tta.mobile.tta.data.local.db.table.Notification;
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
                Account.class
        },
        version = 4,
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
}
