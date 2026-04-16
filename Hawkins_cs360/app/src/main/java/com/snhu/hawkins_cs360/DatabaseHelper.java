package com.snhu.hawkins_cs360;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SimpleWeight.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_GOALS = "goals";
    public static final String TABLE_WEIGHTS = "weights";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";

    // USERS Table - column names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    // GOALS Table - column names
    private static final String KEY_GOAL_WEIGHT = "goal_weight";

    // WEIGHTS Table - column names
    public static final String KEY_DATE = "date";
    public static final String KEY_WEIGHT = "weight";

    // Table Create Statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE "
            + TABLE_USERS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USERNAME + " TEXT UNIQUE,"
            + KEY_PASSWORD + " TEXT" + ")";

    private static final String CREATE_TABLE_GOALS = "CREATE TABLE "
            + TABLE_GOALS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER UNIQUE,"
            + KEY_GOAL_WEIGHT + " REAL" + ")";

    private static final String CREATE_TABLE_WEIGHTS = "CREATE TABLE "
            + TABLE_WEIGHTS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_DATE + " TEXT,"
            + KEY_WEIGHT + " REAL" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_GOALS);
        db.execSQL(CREATE_TABLE_WEIGHTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHTS);
        // create new tables
        onCreate(db);
    }

    /**
     * Users
     */
    public long registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);

        // insert row
        long user_id = db.insert(TABLE_USERS, null, values);
        return user_id;
    }

    public long authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + KEY_USERNAME + " = ? AND " + KEY_PASSWORD + " = ?";

        Cursor c = db.rawQuery(selectQuery, new String[]{username, password});
        long userId = -1;
        if (c != null && c.moveToFirst()) {
            int idIndex = c.getColumnIndex(KEY_ID);
            if (idIndex != -1) {
                userId = c.getLong(idIndex);
            }
            c.close();
        }
        return userId;
    }

    /**
     * Goals
     */
    public void setGoalWeight(long userId, float goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userId);
        values.put(KEY_GOAL_WEIGHT, goalWeight);

        // Try updating first
        int rows = db.update(TABLE_GOALS, values, KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        // If not updated, insert
        if (rows == 0) {
            db.insert(TABLE_GOALS, null, values);
        }
    }

    public float getGoalWeight(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_GOALS + " WHERE " + KEY_USER_ID + " = ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});

        float goal = -1;
        if (c != null && c.moveToFirst()) {
            int index = c.getColumnIndex(KEY_GOAL_WEIGHT);
            if (index != -1) {
                goal = c.getFloat(index);
            }
            c.close();
        }
        return goal;
    }

    /**
     * Weights CRUD
     */
    public long addWeightRecord(long userId, String date, float weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, userId);
        values.put(KEY_DATE, date);
        values.put(KEY_WEIGHT, weight);

        // insert row
        return db.insert(TABLE_WEIGHTS, null, values);
    }

    public Cursor getWeightsForUser(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_WEIGHTS + " WHERE " + KEY_USER_ID + " = ?";
        return db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
    }

    public int updateWeightRecord(long recordId, float newWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_WEIGHT, newWeight);

        // update row
        return db.update(TABLE_WEIGHTS, values, KEY_ID + " = ?", new String[]{String.valueOf(recordId)});
    }

    public void deleteWeightRecord(long recordId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEIGHTS, KEY_ID + " = ?", new String[]{String.valueOf(recordId)});
    }
}
