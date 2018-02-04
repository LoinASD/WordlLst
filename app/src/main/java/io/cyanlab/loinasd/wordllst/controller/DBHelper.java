package io.cyanlab.loinasd.wordllst.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.activities.NavActivity;
import io.cyanlab.loinasd.wordllst.activities.ShowFragment;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context,"Hs",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WordLists ("
                + "_id integer primary key autoincrement," +
                "wlName text" +
                ");");

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
        int colId = cursor.getColumnIndex("wlName");
        String[] Names = new String[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            Names[i] = cursor.getString(colId);
            cursor.moveToNext();
        }
        return Names;
    }

    //-------Saves Edited Wordlist-------//

    public void saveNewWLRow(String wlName, String prim, String trans) {
        getWritableDatabase().beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("prim", prim);
            cv.put("trans", trans);
            cv.put("weight", ShowFragment.RIGHT_ANSWERS_TO_COMPLETE);

            getWritableDatabase().insert(wlName, null, cv);
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    public void saveWLRow(String wordlistName, View view) {
        SQLiteDatabase database = this.getWritableDatabase();
        EditText primET = (EditText) (view).findViewById(R.id.primeTV);
        EditText transET = (EditText) (view).findViewById(R.id.translateTV);
        TextView idTV = (TextView) view.findViewById(R.id.idPlace);
        int id = Integer.parseInt(idTV.getText().toString());
        ContentValues cv = new ContentValues();
        cv.put("prim", primET.getText().toString());
        cv.put("trans", transET.getText().toString());
        cv.put("_id", id);
        database.update(wordlistName, cv, "_id = " + idTV.getText().toString(), null);
    }

    public void saveWLRow(String wlName, int lineId, String prim, String trans) throws SQLiteException {
        ContentValues cv = new ContentValues();
        cv.put("prim", prim);
        cv.put("trans", trans);
        cv.put("_id", lineId);
        getWritableDatabase().update(wlName, cv, "_id = " + lineId, null);
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
                values.put("wlName", wLName);
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

    public boolean saveNewWL(String wLName) {
        try {
            SQLiteDatabase database = this.getWritableDatabase();
            database.execSQL("create table " + wLName + " ("
                    + "_id integer primary key autoincrement,"
                    + "prim text,"
                    + "trans text,"
                    + "weight integer" + ");");

            ContentValues values = new ContentValues();
            values.put("wlName", wLName);
            database.insert("WordLists", null, values);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    //-------Returns Cursor From WL Table-------//

    public int countWeight(String name) {

        Cursor data = getData(name, 0);

        int weightIndex = data.getColumnIndex("weight");
        int weight = 0;
        if (data.moveToFirst()) {
            do {
                weight += data.getInt(weightIndex);

            } while (data.moveToNext());
        }

        return weight;
    }

    public Cursor getData(String wlName, int pos) {

        Cursor cursor;

        if (pos == 0) {
            cursor = getWritableDatabase().query(wlName, null, null, null, null, null, null);
        } else
            cursor = getWritableDatabase().query(wlName, null, "_id < " + (pos + 40), null, null, null, null);


        if (cursor.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put("prim", "");
            cv.put("trans", "");
            getWritableDatabase().insert(wlName, null, cv);
        }
        return cursor;
    }

    public Cursor getRow(String wlName, int id) {
        return this.getWritableDatabase().query(wlName, null, "_id = " + id, null, null, null, null);
    }

    @Nullable
    public Cursor getLists(){
        try {
            return getReadableDatabase().query("Wordlists",null,null,null,null,null,null);
        } catch (SQLiteException e) {
            return null;
        }
    }

    //-------Removes Wordlist from DB-------//

    public void deleteWL(String wlName) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + wlName);

        String[] args = {wlName};
        int id = 0;
        Cursor cursor = getLists();
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(cursor.getColumnIndex("wlName")).equals(wlName))
                    id = cursor.getInt(cursor.getColumnIndex("_id"));
            } while (cursor.moveToNext());
        }
        database.delete("Wordlists", "_id = " + id, null);

    }

    public void deleteLine(String wlName, int lineId) {
        getWritableDatabase().delete(wlName, "_id = " + lineId, null);
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

    @Override
    public synchronized void close() {
        super.close();
    }
}
