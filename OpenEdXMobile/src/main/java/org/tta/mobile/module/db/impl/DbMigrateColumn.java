package org.tta.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DbMigrateColumn extends DbOperationBase<Integer> {

    private String table;
    private String mcolumn;

    DbMigrateColumn(String table, String column) {
        this.table = table;
        this.mcolumn = column;
    }

    @Override
    public Integer execute(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }

        if (mcolumn == null || mcolumn.isEmpty()) {
            throw new IllegalArgumentException("values must be provided");
        }

        try {
            if (!isColumnExists(db)) {
                Cursor crs = db.rawQuery("ALTER TABLE " + table + " ADD " + mcolumn + " TEXT", null);
                crs.getCount();
                crs.close();
            }

            if (!isColumnExists(db))
                return 0;
            else
                return 1;

        }
        catch (Exception e)
        {
            logger.warn("Column===>"+mcolumn+" unable to add in Table==>"+table);
            return 0;
        }
    }

    @Override
    public Integer getDefaultValue() {
        return -1;
    }

    private boolean isColumnExists(SQLiteDatabase db)
    {
        boolean exist=false;
        Cursor c = db.rawQuery("SELECT COUNT(*) AS CNTREC FROM pragma_table_info("+ table +") WHERE name = ?" , new String[] {mcolumn});

        int count = c.getCount();
        c.close();

        if(count>0)
            exist=true;

        return exist;
    }
}