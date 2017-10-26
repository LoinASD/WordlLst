package io.cyanlab.loinasd.wordllst.controller;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.EditText;
import android.widget.LinearLayout;

import io.cyanlab.loinasd.wordllst.model.Facade;
import io.cyanlab.loinasd.wordllst.view.WLView;

import java.util.ArrayList;


class LazyPars {

    public static void loadWls (SQLiteDatabase database){
        Facade facade = Facade.getFacade();
        Cursor c = database.query("WordLists",null,null, null, null, null, null);
        if(c.moveToFirst()){
            do{
                String f = c.getString(c.getColumnIndex("wlId"));
                Cursor cursor = database.query(c.getString(c.getColumnIndex("wlId")),null,null, null, null, null, null);
                facade.addWordlist(f);
                int wlNum = facade.getWlNum(f);
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
        int wlNum = facade.getWlNum(wordlistName);
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

    public static void saveNewWordlist(int wLNum, SQLiteDatabase database) {
        Facade facade = Facade.getFacade();
        String wLName = facade.getWlName(wLNum);
        database.execSQL("create table " + wLName + " ("
                + "_id integer primary key autoincrement,"
                + "prim text,"
                + "trans text" + ");");

        ContentValues values = new ContentValues();
        values.put("wlId", wLName);
        database.insert("WordLists", null, values);

        values.clear();
        for (int i = 0; i < facade.getLinesCount(wLNum); i++) {
            String k = "";
            for (int j = 0; j < facade.getPrim(wLNum, i).size(); j++) {
                String s = facade.getPrim(wLNum, i).get(j);
                k += s;
                if (j != facade.getPrim(wLNum, i).size() - 1) {
                    k += "/";
                }
            }
            values.put("prim", k);
            k = "";

            for (int j = 0; j < facade.getTrans(wLNum, i).size(); j++) {
                String s = facade.getTrans(wLNum, i).get(j);
                k += s;
                if (j != facade.getTrans(wLNum, i).size() - 1) {
                    k += "/";
                }
            }
            values.put("trans", k);
            database.insert(wLName, null, values);
        }

    }

}
