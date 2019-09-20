package org.edx.mobile.tta.data.local.db;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;

import org.edx.mobile.tta.data.local.db.dao.CategoryDao;
import org.edx.mobile.tta.data.local.db.dao.CertificateDao;
import org.edx.mobile.tta.data.local.db.dao.ContentDao;
import org.edx.mobile.tta.data.local.db.dao.ContentListDao;
import org.edx.mobile.tta.data.local.db.dao.ContentStatusDao;
import org.edx.mobile.tta.data.local.db.dao.FeedDao;
import org.edx.mobile.tta.data.local.db.dao.NotificationDao;
import org.edx.mobile.tta.data.local.db.dao.PeriodDao;
import org.edx.mobile.tta.data.local.db.dao.ProgramDao;
import org.edx.mobile.tta.data.local.db.dao.SectionDao;
import org.edx.mobile.tta.data.local.db.dao.SourceDao;
import org.edx.mobile.tta.data.local.db.dao.UnitDao;
import org.edx.mobile.tta.data.local.db.dao.UnitStatusDao;
import org.edx.mobile.tta.data.local.db.dao.UserDao;
import org.edx.mobile.tta.data.local.db.table.Certificate;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.data.local.db.table.Notification;
import org.edx.mobile.tta.data.local.db.table.Period;
import org.edx.mobile.tta.data.local.db.table.Program;
import org.edx.mobile.tta.data.local.db.table.Section;
import org.edx.mobile.tta.data.local.db.table.Unit;
import org.edx.mobile.tta.data.local.db.table.UnitStatus;
import org.edx.mobile.tta.data.local.db.table.User;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.local.db.table.Source;

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
                Program.class,
                Section.class,
                Period.class,
                Unit.class
        },
        version = 7,
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
    public abstract ProgramDao programDao();
    public abstract SectionDao sectionDao();
    public abstract PeriodDao periodDao();
    public abstract UnitDao unitDao();

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE unit ADD COLUMN periodName TEXT");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE unit ADD COLUMN unit_id TEXT");
        }
    };
}
