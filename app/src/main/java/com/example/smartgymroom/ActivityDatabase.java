package com.example.smartgymroom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class ActivityDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ActivityDatabase.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_NAME = "activities";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    private static final String COLUMN_DURATION = "duration";

    public ActivityDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_QUERY =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DURATION + " TEXT, " +
                        COLUMN_ACTIVITY_TYPE + " TEXT, " +
                        COLUMN_DATE + " TEXT" +
                        ")";
        db.execSQL(CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertActivity(String duration, String activityType, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DURATION, duration);
        contentValues.put(COLUMN_ACTIVITY_TYPE, activityType);
        contentValues.put(COLUMN_DATE, date);

        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();

        // Check if the insertion was successful
        return result != -1;
    }

    public Cursor getLimitedActivities(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + "_id" + " DESC LIMIT " + limit;
        return db.rawQuery(query, null);
    }

    public Map<String, Integer> getSessionCounts() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT activity_type, COUNT(*) FROM activities WHERE date >= date('now','-30 days') GROUP BY activity_type";
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String type = cursor.getString(0);
            int count = cursor.getInt(1);
            counts.put(type, count);
        }

        cursor.close();
        return counts;
    }


}
