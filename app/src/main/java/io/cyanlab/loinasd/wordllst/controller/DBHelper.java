package io.cyanlab.loinasd.wordllst.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.controller.pdf.PDFParser;
import io.cyanlab.loinasd.wordllst.controller.pdf.TextExtractor;
import io.cyanlab.loinasd.wordllst.model.Facade;
import io.cyanlab.loinasd.wordllst.view.WLView;


public class DBHelper extends SQLiteOpenHelper {
    public DBHelper (Context context){
        super(context,"Hs",null,1);
    }

    private Facade facade = Facade.getFacade();
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WordLists ("
                + "wlId text"+ ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void loadAllWLs(SQLiteDatabase db) {
        LazyPars.loadWls(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        //LazyPars.loadWls(db);
    }

    public void saveWl(String s, SQLiteDatabase db, WLView wlView){

    }

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
            } finally {
                getWritableDatabase().endTransaction();
            }
            if (successful) {
                return true;
            } else return false;
        } else return false;

    }


    public Cursor getData(String wlName) {
        return this.getWritableDatabase().query(wlName, null, null, null, null, null, null);
    }

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
