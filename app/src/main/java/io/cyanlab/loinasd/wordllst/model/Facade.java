package io.cyanlab.loinasd.wordllst.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Observable;

import io.cyanlab.loinasd.wordllst.controller.LazyPars;

public final class Facade extends Observable implements Model{

    private Facade(){}

    private static Facade instance;

    public static Facade getFacade(){
        if(instance == null){
            instance = new Facade();
        }
        return instance;
    }

    private static ArrayList<Wordlist> wordlists = new ArrayList<>();

    public Lang getLang(int langNum){
        switch(langNum){
            case(0):{
                return Lang.EN;
            }
            case(1):{
                return Lang.RU;
            }
            default:{
                return Lang.EN;
            }
        }
    }

    public void loadWordLists(SQLiteDatabase sqLiteDatabase){
        Cursor c = sqLiteDatabase.query("MbWordList",null,null, null, null, null, null);
        if (c.moveToFirst()){
            do{
                String f = c.getString(c.getColumnIndex("wlId"));
                ArrayList<Line> lines = new ArrayList<Line>();
                while (c.getString(c.getColumnIndex("wlId")).equals(f)) {
                    Line line = new Line();
                    String s = c.getString(c.getColumnIndex("prim"));
                    int ind = s.indexOf("/");
                    int prevInd = 0;
                    while ((ind != -1) &&(s.charAt(ind) == '/')) {
                        Word word = new Word(s.substring(prevInd, ind), Lang.EN, line);
                        prevInd = ind+1;
                        s = s.replaceFirst("/","=");
                        ind = s.indexOf("/");
                        line.getPrime().add(word);
                        if(ind == -1){
                            break;
                        }
                    }

                    s = c.getString(c.getColumnIndex("trans"));
                    ind = s.lastIndexOf("/");
                    Word word = new Word(s.substring(0,ind), Lang.RU, line);
                    line.getTranslate().add(word);
                    lines.add(line);
                    if(c.moveToNext()){}
                    else{break;};
                }
                c.moveToPrevious();
                Wordlist wordlist = new Wordlist(c.getString(c.getColumnIndex("wlId")),lines);
                this.wordlists.add(wordlist);
            }while(c.moveToNext());
        }
    }

    public void addWordlist(SQLiteDatabase sqLiteDatabase){
        LazyPars lazyPars = new LazyPars();
        ContentValues cv = new ContentValues();
        Wordlist wordlist;
        for (int i =0;i<3;i++){
            wordlist = lazyPars.createWl(i);
            for (int j =0; j< wordlist.getLines().size();j++){
                String k = new String();
                for (int x = 0;x<wordlist.getLines().get(j).getPrime().size();x++) {
                     String s = wordlist.getLines().get(j).getPrime().get(x).getWord();
                     k += s+"/";
                }
                cv.put("wlId",wordlist.getName());
                cv.put("prim",k);
                k = "";
                for (int x = 0;x<wordlist.getLines().get(j).getTranslate().size();x++) {
                    String s = wordlist.getLines().get(j).getTranslate().get(x).getWord();
                    k += s+"/";
                }
                cv.put("trans",k);
                sqLiteDatabase.insert("MbWordList",null,cv);
            }
        }
    }

    public int getWordlistsNum(){
        return wordlists.size();
    }

    public int getWordlistNumByName(String name){

        int i =0;

        while((i<wordlists.size())&&!(wordlists.get(i).getName().equals(name))){
            i++;
        }

        if (wordlists.get(i).getName().equals(name)){
            return i;
        }
        else{
            return -1;
        }
    }

    public String getWordlistNameByNum(int wordlistNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return null;
        }
        else{
            return wordlists.get(wordlistNum).getName();
        }
    }

    public int getWordlistLinesCountByNum(int wordlistNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return -1;
        }
        else{
            return wordlists.get(wordlistNum).getLines().size();
        }
    }

    public ArrayList<String> getPrimByLineNum(int wordlistNum, final int lineNum){
        if ((wordlistNum >= wordlists.size())||(wordlistNum<0)){
            return null;
        }
        else{
            final ArrayList<Word> words = wordlists.get(wordlistNum).getLines().get(lineNum).getPrime();
            ArrayList<String> wordsStr= new ArrayList<String>(){
                @Override
                public String get(int index) {
                    return words.get(index).getWord();
                }

                @Override
                public int size() {
                    return words.size();
                }
            };
            return wordsStr;
        }
    }

    public ArrayList<String> getTransByLineNum(int wordlistNum,final int lineNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return null;
        }
        else{
            final ArrayList<Word> words = wordlists.get(wordlistNum).getLines().get(lineNum).getTranslate();
            ArrayList<String> wordsStr= new ArrayList<String>(){
                @Override
                public String get(int index) {
                    return words.get(index).getWord();
                }

                @Override
                public int size() {
                    return words.size();
                }
            };
            return wordsStr;
        }
    }

    public void addLine(int wordListNum,ArrayList<String> prim, ArrayList<String> trans){
        Line line = new Line();
        ArrayList<Word> words= new ArrayList<Word>();
        for (String s:prim
             ) {
            words.add(new Word(s,this.getLang(0),line));
        }
        line.setPrime(words);
        words.clear();
        for (String s: trans
             ) {
            words.add(new Word(s,this.getLang(1),line));
        }
        line.setTranslate(words);
        line.setWordlist(wordlists.get(wordListNum));
        wordlists.get(wordListNum);
    }

    public int addWordlist(String s){

        if (this.getWordlistNumByName(s) == -1){

            this.wordlists.add(new Wordlist(s));

            return this.getWordlistNumByName(s);

        }
        else{

            return -1;

        }

    }


}
