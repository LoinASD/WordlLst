package io.cyanlab.loinasd.wordllst.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.R;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper (Context context){
        super(context,"Hs",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WordLists ("
                + "wlId text"+ ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    }

    //-------Returns Array of Strings with saved WL names-------//

    public String[] loadWlsNames() {
        Cursor cursor = this.getWritableDatabase().query("Wordlists", null, null, null, null, null, null);
        cursor.moveToFirst();
        int colId = cursor.getColumnIndex("wlId");
        String[] Names = new String[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            Names[i] = cursor.getString(colId);
            cursor.moveToNext();
        }
        return Names;
    }

    //-------Saves Edited Wordlist-------//

    public void saveWL(String wordlistName, ListView wlView) {
        SQLiteDatabase database = this.getWritableDatabase();
        for (int i = 0; i < wlView.getChildCount(); i++) {
            EditText primET = (EditText) (wlView.getChildAt(i)).findViewById(R.id.primeTV);
            EditText transET = (EditText) (wlView.getChildAt(i)).findViewById(R.id.translateTV);
            TextView idTV = (TextView) wlView.getChildAt(i).findViewById(R.id.idPlace);
            int id = Integer.parseInt(idTV.getText().toString());
            ContentValues cv = new ContentValues();
            cv.put("prim", primET.getText().toString());
            cv.put("trans", transET.getText().toString());
            cv.put("_id", id);
            database.replace(wordlistName, null, cv);
        }
    }

    //-------Saves new Wordlist. If successful, returns true-------//

    public boolean saveNewWL(String wLName, ArrayList<String> prim, ArrayList<String> trans) {
        if (prim.size() == trans.size()) {
            boolean successful = false;
            this.getWritableDatabase().beginTransaction();
            try {
                SQLiteDatabase database = this.getWritableDatabase();
                database.execSQL("create table " + wLName + " ("
                        + "_id integer primary key autoincrement,"
                        + "prim text,"
                        + "trans text" + ");");

                ContentValues values = new ContentValues();
                values.put("wlId", wLName);
                database.insert("WordLists", null, values);

                values.clear();

                for (int i = 0; i < prim.size(); i++) {
                    values.put("prim", prim.get(i).replaceAll("/", ", "));
                    values.put("trans", trans.get(i).replaceAll("/", ", "));
                    database.insert(wLName, null, values);
                }
                successful = true;
                this.getWritableDatabase().setTransactionSuccessful();
            } catch (SQLiteException e) {
                return false;
            } finally {
                getWritableDatabase().endTransaction();
            }
            if (successful) {
                return true;
            } else return false;
        } else return false;

    }

    //-------Returns Cursor From WL Table-------//
    public Cursor getData(String wlName) {
        return this.getWritableDatabase().query(wlName, null, null, null, null, null, null);
    }

    //-------Removes Wordlist from DB-------//

    public void deleteWL(String wlName) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + wlName);
        database.delete("Wordlists", "wlId = ?", new String[]{wlName});
        database.execSQL("VACUUM");
    }

    //-------Suppose it clears out the DB-------//

    public void clearDB() {
        SQLiteDatabase database = getWritableDatabase();
        String[] names = loadWlsNames();
        for (int i = 0; i < names.length; i++) {
            database.execSQL("DROP TABLE IF EXISTS " + names[i]);
        }
        database.execSQL("DROP TABLE IF EXISTS Wordlists");
        database.execSQL("VACUUM");
        onCreate(database);
    }
}
