package io.cyanlab.loinasd.wordllst.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Observable;

public interface Model{

    int addWordlist(String s);

    int getWordlistsNum();

    int getWordlistNumByName(String name);

    String getWordlistNameByNum(int wordlistNum);

    int getWordlistLinesCountByNum(int wordlistNum);

    ArrayList<String> getPrimByLineNum(int wordlistNum, int lineNum);

    ArrayList<String> getTransByLineNum(int wordlistNum, int lineNum);

}
