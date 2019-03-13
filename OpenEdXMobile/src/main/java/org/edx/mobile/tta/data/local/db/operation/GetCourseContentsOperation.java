package org.edx.mobile.tta.data.local.db.operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DbStructure;
import org.edx.mobile.module.db.impl.DbOperationSelect;
import org.edx.mobile.tta.data.enums.DownloadType;

import java.util.ArrayList;
import java.util.List;

public class GetCourseContentsOperation extends DbOperationSelect<List<Long>> {
    public GetCourseContentsOperation() {
        super(true, DbStructure.Table.DOWNLOADS, new String[]{DbStructure.Column.CONTENT_ID},
                "(" + DbStructure.Column.TYPE + "=? OR " + DbStructure.Column.TYPE + "=?) AND "
                + DbStructure.Column.DOWNLOADED + "=?",
                new String[]{DownloadType.SCORM.name(), DownloadType.PDF.name(),
                        String.valueOf(DownloadEntry.DownloadedState.DOWNLOADED.ordinal())},
                DbStructure.Column.CONTENT_ID, null);
    }

    @Override
    public List<Long> execute(SQLiteDatabase db) {
        List<Long> contentIds = new ArrayList<>();

        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                contentIds.add(c.getLong(c.getColumnIndex(DbStructure.Column.CONTENT_ID)));
            } while (c.moveToNext());
        }
        c.close();
        return contentIds;
    }

    @Override
    public List<Long> getDefaultValue() {
        return null;
    }
}
