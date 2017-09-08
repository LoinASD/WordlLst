package io.cyanlab.loinasd.wordllst.model;

import java.util.ArrayList;
import java.util.Observable;

public interface Model{

    public void addWordlist();

    public int getWordlistsNum();

    public int getWordlistNumByName(String name);

    public String getWordlistNameByNum(int wordlistNum);

    public int getWordlistLinesNumByNum(int wordlistNum);

    public ArrayList<String> getPrimByLineNum(int wordlistNum, int lineNum);

    public ArrayList<String> getTransByLineNum(int wordlistNum, int lineNum);

}
