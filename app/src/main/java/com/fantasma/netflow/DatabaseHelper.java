package com.fantasma.netflow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TABLE = "LOG_TABLE", COLUMN_DATE = "DATE", COLUMN_PURPOSE = "PURPOSE", COLUMN_AMOUNT = "AMOUNT", COLUMN_IS_POSITIVE = "POSITIVE";

    private Context context;

    DatabaseHelper(@Nullable Context context) {
        super(context, "logs.db", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + LOG_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_DATE + " TEXT, " + COLUMN_PURPOSE + " TEXT, " + COLUMN_AMOUNT + " DECIMAL(14,2), " + COLUMN_IS_POSITIVE + " BOOL)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void addOne(LogModel log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, log.getTimeStamp());
        cv.put(COLUMN_PURPOSE, log.getPurpose());
        cv.put(COLUMN_AMOUNT, log.getAmount());
        cv.put(COLUMN_IS_POSITIVE, log.isPositive());

        long result = db.insert(LOG_TABLE,null, cv);
        db.close();
        if(result == -1) {
            Toast.makeText(context, "Failed to Add Log", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, (log.isPositive() ? "Gain" : "Loss") + (log.getID().isEmpty() ? " Added" : " Duplicated"), Toast.LENGTH_SHORT).show();
        }
    }

    void updateLog(LogModel log) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PURPOSE, log.getPurpose());
        cv.put(COLUMN_AMOUNT, log.getAmount());
        cv.put(COLUMN_IS_POSITIVE, log.isPositive());

        long result = db.update(LOG_TABLE, cv, "ID=?", new String[]{log.getID()});
        db.close();
        if(result == -1) {
            Toast.makeText(context, "Failed to Update Log", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Log Updated", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteLog(String ID) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(LOG_TABLE,"ID=?", new String[]{ID});
        if(result == -1) {
            Toast.makeText(context, "Failed to Delete Log", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Log Deleted", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    LogModel getLogAt(int index) {
        String query = "SELECT * FROM " + LOG_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToPosition(index);
        LogModel logModel = createLogFromCursor(cursor);

        cursor.close();
        db.close();

        return logModel;
    }

    List<LogModel> getLogs() {
        List<LogModel> returnList = new ArrayList<>();

        String query = "SELECT * FROM " + LOG_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToLast()) {
            do {
                returnList.add(createLogFromCursor(cursor));
            } while (cursor.moveToPrevious());
        }

        cursor.close();
        db.close();
        return returnList;
    }

    private LogModel createLogFromCursor(Cursor cursor) {
        String ID = cursor.getString(0);
        String date = cursor.getString(1);
        String purpose = cursor.getString(2);
        Double amount = cursor.getDouble(3);
        boolean positive = cursor.getInt(4) == 1;

        return new LogModel(date, purpose, amount, positive, ID);
    }

}
