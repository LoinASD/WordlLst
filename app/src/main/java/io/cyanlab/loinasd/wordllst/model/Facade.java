package io.cyanlab.loinasd.wordllst.model;

import java.util.ArrayList;
import java.util.Observable;

public final class Facade extends Observable implements Model{

private Facade(){}

    private ArrayList<Wordlist> wordlists;

    public void addWordlist(){
        LazyPars lz = new LazyPars();
        wordlists.add(lz.createWl());
    }

    public int getWordlistsNum(){
        return this.wordlists.size();
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

    public int getWordlistLinesNumByNum(int wordlistNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return -1;
        }
        else{
            return wordlists.get(wordlistNum).getLines().size();
        }
    }

    public ArrayList<String> getPrimByLineNum(int wordlistNum, final int lineNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return null;
        }
        else{
            final ArrayList<Word> words = wordlists.get(wordlistNum).getLines().get(lineNum).getPrime();
            ArrayList<String> wordsStr= new ArrayList<String>(){
                @Override
                public String get(int index) {
                    return words.get(lineNum).getWord();
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
                    return words.get(lineNum).getWord();
                }

                @Override
                public int size() {
                    return words.size();
                }
            };
            return wordsStr;
        }
    }

}
