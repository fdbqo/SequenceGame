package com.example.sequencegame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.sequencegame.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHandler manages all database operations for the Sequence Game.
 * It handles creating the database, adding scores, retrieving top scores,
 * and checking if a score qualifies as a high score.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sequenceGameDB";
    private static final String TABLE_SCORES = "scores";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SCORE = "score";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the scores table in the database when the database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCORES_TABLE = "CREATE TABLE " + TABLE_SCORES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_SCORE + " INTEGER" + ")";
        db.execSQL(CREATE_SCORES_TABLE);
    }

    /**
     * Upgrades the database schema if the version number changes.
     * Currently, it drops the existing table and creates a new one.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORES);
        onCreate(db);
    }

    /**
     * Adds a new score to the database.
     * @param score The Score object to be added to the database.
     */
    public void addScore(Score score) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, score.getName());
        values.put(KEY_SCORE, score.getScore());

        db.insert(TABLE_SCORES, null, values);
        db.close();
    }

    /**
     * Retrieves the top scores from the database.
     * @param limit The number of top scores to retrieve.
     * @return A list of Score objects representing the top scores.
     */
    public List<Score> getTopScores(int limit) {
        List<Score> scoreList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SCORES + " ORDER BY " + KEY_SCORE + " DESC LIMIT " + limit;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Iterate through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                Score score = new Score();
                score.setId(Integer.parseInt(cursor.getString(0)));
                score.setName(cursor.getString(1));
                score.setScore(Integer.parseInt(cursor.getString(2)));
                scoreList.add(score);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return scoreList;
    }

    /**
     * Checks if a given score qualifies as a high score.
     * @param score The score to check.
     * @return true if the score is among the top 10 scores, false otherwise.
     */
    public boolean isHighScore(int score) {
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE_SCORES + " WHERE " + KEY_SCORE + " > " + score;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count < 10; // Changed to 10 to match the new top scores limit
    }
}

