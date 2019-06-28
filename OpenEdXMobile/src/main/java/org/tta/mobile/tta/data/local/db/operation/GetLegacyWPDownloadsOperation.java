package org.tta.mobile.tta.data.local.db.operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.tta.mobile.model.VideoModel;
import org.tta.mobile.module.db.DatabaseModelFactory;
import org.tta.mobile.module.db.DbStructure;
import org.tta.mobile.module.db.impl.DbOperationSelect;
import org.tta.mobile.tta.data.enums.DownloadType;

import java.util.ArrayList;
import java.util.List;

public class GetLegacyWPDownloadsOperation extends DbOperationSelect<List<VideoModel>> {

    public GetLegacyWPDownloadsOperation() {
        super(false, DbStructure.Table.DOWNLOADS, null,
                DbStructure.Column.CONTENT_ID + "=0 AND " + DbStructure.Column.TYPE + "=?",
                new String[]{DownloadType.CONNECTVIDEO.name()}, null);
    }

    @Override
    public List<VideoModel> execute(SQLiteDatabase db) {
        List<VideoModel> list = new ArrayList<>();

        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                VideoModel video = DatabaseModelFactory.getModel(c);
                list.add(video);
            } while (c.moveToNext());
        }
        c.close();

        return list;
    }

    @Override
    public List<VideoModel> getDefaultValue() {
        return new ArrayList<>();
    }
}
