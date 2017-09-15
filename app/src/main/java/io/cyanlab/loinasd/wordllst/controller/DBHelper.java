package io.cyanlab.loinasd.wordllst.controller;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.cyanlab.loinasd.wordllst.model.Facade;


public class DBHelper extends SQLiteOpenHelper {
    public DBHelper (Context context){
        super(context,"Hs",null,1);
    }

    private Facade facade = Facade.getFacade();
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WordLists ("
                + "wlId text"+ ");");
        for (int i=0;i<3;i++){
            LazyPars.createWl(i,db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void onOpen(SQLiteDatabase db) {
        LazyPars.loadWls(db);}
}
