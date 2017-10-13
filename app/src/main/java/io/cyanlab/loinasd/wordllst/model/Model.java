package io.cyanlab.loinasd.wordllst.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Observable;

public interface Model{

    int addWordlist(String s);

    int getWlsCount();

    int getWlNum(String name);

    String getWlName(int wordlistNum);

    int getLinesCount(int wordlistNum);

    ArrayList<String> getPrim(int wordlistNum, int lineNum);

    ArrayList<String> getTrans(int wordlistNum, int lineNum);

}
