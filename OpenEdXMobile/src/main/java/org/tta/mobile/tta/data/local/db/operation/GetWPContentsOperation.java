package org.tta.mobile.tta.data.local.db.operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.tta.mobile.model.db.DownloadEntry;
import org.tta.mobile.module.db.DbStructure;
import org.tta.mobile.module.db.impl.DbOperationSelect;
import org.tta.mobile.tta.data.enums.DownloadType;
import org.tta.mobile.util.Sha1Util;

import java.util.ArrayList;
import java.util.List;

import static org.tta.mobile.util.BrowserUtil.loginPrefs;

public class GetWPContentsOperation extends DbOperationSelect<List<Long>> {
    public GetWPContentsOperation(String sourceName) {
        super(true, DbStructure.Table.DOWNLOADS, new String[]{DbStructure.Column.CONTENT_ID},
                DbStructure.Column.USERNAME + "=? AND "
                + DbStructure.Column.TYPE + "=? AND "
                + DbStructure.Column.CHAPTER + "=? AND "
                + DbStructure.Column.DOWNLOADED + "=?",
                new String[]{Sha1Util.SHA1(loginPrefs.getUsername()),
                        DownloadType.CONNECTVIDEO.name(), sourceName,
                        String.valueOf(DownloadEntry.DownloadedState.DOWNLOADED.ordinal())
                },
                null);
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
