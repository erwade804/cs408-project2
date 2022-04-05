package edu.jsu.mcis.cs408.project2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "myNewDatabase.db";
    private static final String TABLE_OF_WORDS = "words";

    public static final String ROW = "arg0";
    public static final String COLUMN = "arg1";
    public static final String BOX = "arg2";
    public static final String DIRECTION = "arg3";
    public static final String WORD = "arg4";
    public static final String CLUE = "arg5";

    public DatabaseHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "+TABLE_OF_WORDS+" (arg0 text, arg1 text, arg2 text, arg3 text, arg4 text, arg5 text)";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OF_WORDS);
        onCreate(db);
    }

    public void addWordToStoredWords(Word word) {

        ContentValues values = new ContentValues();
        values.put(ROW, Integer.toString(word.getRow()));
        values.put(COLUMN, Integer.toString(word.getColumn()));
        values.put(BOX, Integer.toString(word.getBox()));
        values.put(CLUE, word.getClue());
        values.put(WORD, word.getWord());
        values.put(DIRECTION, word.getDirection().toString());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_OF_WORDS, null, values);
        db.close();

    }


    private Word createWord(String row, String col, String box, String dir, String word, String clue){
        String[] args = new String[6];
        args[0] = row;
        args[1] = col;
        args[2] = box;
        args[3] = dir;
        args[4] = word;
        args[5] = clue;
        return new Word(args);
    }

    public ArrayList<String> getAllWords() {

        String query = "SELECT * FROM " + TABLE_OF_WORDS;

        ArrayList<String> wordKeys = new ArrayList<String>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            do {
                String wordStr = cursor.getString(4) + " " + cursor.getString(2);
                if(!wordKeys.contains(wordStr)) {
                    wordKeys.add(wordStr);
                }
            }
            while ( cursor.moveToNext() );
        }

        db.close();
        return wordKeys;

    }

    private ArrayList<Word> getAllWordsAsList() {

        String query = "SELECT * FROM " + TABLE_OF_WORDS;

        ArrayList<Word> allContacts = new ArrayList<Word>();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            do {

            }
            while ( cursor.moveToNext() );
        }

        db.close();
        return allContacts;

    }

}