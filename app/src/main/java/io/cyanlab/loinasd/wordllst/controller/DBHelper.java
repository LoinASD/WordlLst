package io.cyanlab.loinasd.wordllst.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    public void loadAllWordlists(SQLiteDatabase db) {
        //LazyPars.loadWls(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db){
        //LazyPars.loadWls(db);
    }

    public void saveWl(String s, SQLiteDatabase db, WLView wlView){
        LazyPars.saveWL(s,db,wlView);
        LazyPars.loadWl(s,db);
    }
}
