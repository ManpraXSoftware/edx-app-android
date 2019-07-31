package org.tta.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import org.tta.mobile.logger.Logger;
import org.tta.mobile.tta.Constants;

public abstract class DbOperationSelect<T> extends DbOperationBase<T> {
    
    private String table;
    private String[] columns;
    private String whereClause;
    private String[] whereArgs;
    private String orderBy;
    private boolean distinct;
    private String groupBy;
    private String limit;

    public DbOperationSelect(boolean distinct,String table, String[] columns, String whereClause, String[] whereArgs, String orderBy) {
        this.distinct = distinct;
        this.table = table;
        this.columns = columns;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
        this.orderBy = orderBy;
    }

    public DbOperationSelect(boolean distinct, String table, String[] columns, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        this.distinct = distinct;
        this.table = table;
        this.columns = columns;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
        this.groupBy = groupBy;
        this.orderBy = orderBy;
        this.limit = limit;
    }


    
    public Cursor getCursor(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }
        try {
            Cursor c = db.query(distinct, table, columns, whereClause, whereArgs, groupBy, null, orderBy, limit);
            return c;
        }catch (Exception ex){
            Bundle parameters = new Bundle();
            parameters.putString(org.tta.mobile.tta.Constants.KEY_CLASS_NAME, DbOperationSelect.class.getName());
            parameters.putString(org.tta.mobile.tta.Constants.KEY_FUNCTION_NAME, "getCursor");
            Logger.logCrashlytics(ex, parameters);
            logger.error(ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
}
