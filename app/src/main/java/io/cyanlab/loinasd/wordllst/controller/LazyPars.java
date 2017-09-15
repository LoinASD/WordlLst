package io.cyanlab.loinasd.wordllst.controller;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Scanner;

import io.cyanlab.loinasd.wordllst.model.Facade;

class LazyPars {

    public void createWl (int i, SQLiteDatabase database){
        try {
            Facade facade = Facade.getFacade();
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
                    k = "Work and Jobs";
                    break;
                }
                default:{
                    sc = new Scanner(StandardWlLibrary.Houses);
                    k = "Houses";
                    break;
                }
            }
            while (sc.hasNextLine()) {
                ContentValues cv = new ContentValues();
                String s = sc.nextLine();
                cv.put("wlId",k);
                cv.put("prim",s.substring(0,s.indexOf(" \t")));
                cv.put("trans",s.substring(s.indexOf(" \t")+2));
                database.insert("MbWordList",null,cv);
            }
            switch (i){
                case (0):{
                    return new Wordlist("Character", lines);
                }
                case (1):{
                    return new Wordlist("Work and Jobs",lines);
                }
                default:{
                    return new Wordlist("Houses", lines);
                }
            }

        } catch (Exception e){
            return new Wordlist("blet",null);
        }



    }


}
