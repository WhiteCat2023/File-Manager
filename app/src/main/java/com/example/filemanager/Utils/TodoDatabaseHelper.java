package com.example.filemanager.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todoList.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_TODO = "todo";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TASK_TITLE = "task_title";
    private static final String COLUMN_TASK_DESCRIPTION = "task_description";
    private static final String COLUMN_COMPLETE = "isComplete"; // 0 = incomplete, 1 = complete
    private static final String COLUMN_START_DATE = "startDate";
    private static final String COLUMN_END_DATE = "endDate";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_TODO + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TASK_TITLE + " TEXT, " +
                    COLUMN_TASK_DESCRIPTION + " TEXT, " +
                    COLUMN_COMPLETE + " INTEGER, " +
                    COLUMN_START_DATE + " TEXT, " +
                    COLUMN_END_DATE + " TEXT);";

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }

    // Method to add a task locally
    // Method to add a task locally
    public void addTask(ToDoListItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, item.getTaskName());
        values.put(COLUMN_TASK_DESCRIPTION, item.getDescription());
        values.put(COLUMN_COMPLETE, item.isComplete() ? 1 : 0);
        values.put(COLUMN_START_DATE, item.getStartDate());
        values.put(COLUMN_END_DATE, item.getEndDate());

        db.insert(TABLE_TODO, null, values);
        db.close();
    }

    // Method to get all tasks
    public List<ToDoListItem> getAllTasks() {
        List<ToDoListItem> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TODO, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ToDoListItem task = new ToDoListItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETE)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATE))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return taskList;
    }

    // Method to delete a task
    public boolean deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TODO, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected > 0;
    }
}