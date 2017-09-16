package io.cyanlab.loinasd.wordllst.controller;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.widget.EditText;
import android.widget.LinearLayout;

import io.cyanlab.loinasd.wordllst.model.Facade;
import io.cyanlab.loinasd.wordllst.view.WLView;

import java.util.ArrayList;
import java.util.Scanner;


class LazyPars {

    public static void createWl (int i, SQLiteDatabase database){

        Scanner sc;
        String k;

        switch (i) {

            case(0): {
                sc = new Scanner(StandardWlLibrary.Character);
                k = "Character";
                break;
            }

            case (1):{
                sc = new Scanner(StandardWlLibrary.WorkAndJobs);
                k = "WorkAndJobs";
                break;
            }

            default:{
                sc = new Scanner(StandardWlLibrary.Houses);
                k = "Houses";
                break;
            }
        }

        database.execSQL("create table "+ k +" ("
                + "id integer primary key autoincrement,"
                + "prim text,"
                + "trans text" + ");");

        ContentValues kek = new ContentValues();
        kek.put("wlId",k);
        database.insert("WordLists",null,kek);
        while (sc.hasNextLine()) {
            ContentValues cv = new ContentValues();
            String s = sc.nextLine();
            cv.put("prim",s.substring(0,s.indexOf(" \t")));
            cv.put("trans",s.substring(s.indexOf(" \t")+2));
            database.insert(k,null,cv);
        }


    }

    public static void loadWls (SQLiteDatabase database){
        Facade facade = Facade.getFacade();
        Cursor c = database.query("WordLists",null,null, null, null, null, null);
        if(c.moveToFirst()){
            do{
                String f = c.getString(c.getColumnIndex("wlId"));
                Cursor cursor = database.query(c.getString(c.getColumnIndex("wlId")),null,null, null, null, null, null);
                facade.addWordlist(f);
                int wlNum = facade.getWordlistNumByName(f);
                ArrayList<String> prim = new ArrayList<String>();
                ArrayList<String> trans = new ArrayList<String>();
                if (cursor.moveToFirst()){
                    do {
                        prim.clear();
                        trans.clear();
                        String s = cursor.getString(cursor.getColumnIndex("prim"));
                        int ind = s.indexOf("/");
                        int prevInd = 0;
                        while ((ind != -1) &&(s.charAt(ind) == '/')) {
                            prim.add(s.substring(prevInd, ind));
                            prevInd = ind+1;
                            s = s.replaceFirst("/","=");
                            ind = s.indexOf("/");
                            if(ind == -1){
                                break;
                            }
                        }
                        prim.add(s.substring(prevInd));

                        s = cursor.getString(cursor.getColumnIndex("trans"));
                        ind = s.lastIndexOf("/");
                        if(ind != -1){
                            s = s.substring(0,ind);
                        }
                        trans.add(s);
                        facade.addLine(wlNum,prim,trans);
                    }while (cursor.moveToNext());
                }
                cursor.close();

            }while(c.moveToNext());
        }
        c.close();
    }

    public static void loadWl (String wordlistName,SQLiteDatabase database){
        Facade facade = Facade.getFacade();
        Cursor cursor = database.query(wordlistName,null,null, null, null, null, null);
        int wlNum = facade.getWordlistNumByName(wordlistName);
        facade.clearLines(wlNum);
        ArrayList<String> prim = new ArrayList<String>();
        ArrayList<String> trans = new ArrayList<String>();
        if (cursor.moveToFirst()){
            do {
                prim.clear();
                trans.clear();
                String s = cursor.getString(cursor.getColumnIndex("prim"));
                int ind = s.indexOf("/");
                int prevInd = 0;
                while ((ind != -1) &&(s.charAt(ind) == '/')) {
                    prim.add(s.substring(prevInd, ind));
                    prevInd = ind+1;
                    s = s.replaceFirst("/","=");
                    ind = s.indexOf("/");
                    if(ind == -1){
                        break;
                    }
                }
                prim.add(s.substring(prevInd));

                s = cursor.getString(cursor.getColumnIndex("trans"));
                ind = s.lastIndexOf("/");
                if(ind != -1){
                    s = s.substring(0,ind);
                }
                trans.add(s);
                facade.addLine(wlNum,prim,trans);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    public static void saveWL (String wordlistName, SQLiteDatabase database, WLView wlView){
        Cursor cursor = database.query(wordlistName,null,null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0;i < wlView.getChildCount();i++){
            if (cursor.moveToNext()){
                EditText primET = (EditText)((LinearLayout)wlView.getChildAt(i)).getChildAt(0);
                boolean f = false;
                ContentValues cv = new ContentValues();
                if(!(cursor.getString(cursor.getColumnIndex("prim")).equals(primET.getText().toString()))){
                    cv = new ContentValues();
                    cv.put("prim",primET.getText().toString());
                    f = true;
                }
                EditText transET = (EditText)((LinearLayout)wlView.getChildAt(i)).getChildAt(1);
                if(!(cursor.getString(cursor.getColumnIndex("trans")).equals(transET.getText().toString()))){
                    cv.put("trans",transET.getText().toString());
                    f = true;
                }
                if (f){
                    cv.put("id",i+1);
                    database.replace(wordlistName,null,cv);
                }
            }

        }
        cursor.close();
    }

}
