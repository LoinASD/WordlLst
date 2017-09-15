package io.cyanlab.loinasd.wordllst.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Observable;


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

    public int getWordlistsNum(){
        return wordlists.size();
    }

    public int getWordlistNumByName(String name){

        int i =0;

        while((i<wordlists.size())&&!(wordlists.get(i).getName().equals(name))){
            i++;
        }

        if ((i<wordlists.size())&&(wordlists.size()!= 0)&&(wordlists.get(i).getName().equals(name))){
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
        words = new ArrayList<Word>();
        for (String s: trans
             ) {
            words.add(new Word(s,this.getLang(1),line));
        }
        line.setTranslate(words);
        line.setWordlist(wordlists.get(wordListNum));
        wordlists.get(wordListNum).getLines().add(line);
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
