package io.cyanlab.loinasd.wordllst.model;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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


    // Первый список и методы для работы с ним

    private ArrayList<Wordlist> wordlists = new ArrayList<>();

    public int addWordlist(String s){

        if (this.getWordlistNumByName(s) == -1){

            this.wordlists.add(new Wordlist(s));

            return this.getWordlistNumByName(s);

        }
        else{

            return -1;

        }

    }

    public void clearLines(int wordlistNum){
        Wordlist wordlist = this.wordlists.get(wordlistNum);
        wordlist.getLines().clear();
    }

    public void addLine(int wordListNum, ArrayList<String> prim, ArrayList<String> trans){
        Line line = new Line();
        ArrayList<Word> words= new ArrayList<Word>();
        for (String s : prim) {
            words.add( new Word(s, line));
        }
        line.setPrime(words);
        words = new ArrayList<Word>();
        for (String s : trans) {
            words.add(new Word(s,line));
        }
        line.setTranslate(words);
        line.setWordlist(wordlists.get(wordListNum));
        wordlists.get(wordListNum).getLines().add(line);
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
            return new ArrayList<String>(){
                @Override
                public String get(int index) {
                    return words.get(index).getWord();
                }

                @Override
                public int size() {
                    return words.size();
                }
            };

        }
    }

    public ArrayList<String> getTransByLineNum(int wordlistNum,final int lineNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return null;
        }
        else{
            final ArrayList<Word> words = wordlists.get(wordlistNum).getLines().get(lineNum).getTranslate();
            return new ArrayList<String>(){
                @Override
                public String get(int index) {
                    return words.get(index).getWord();
                }

                @Override
                public int size() {
                    return words.size();
                }
            };

        }
    }

    public void addNewWL(String name, ArrayList<String> node1, ArrayList<String> node2) {
        Wordlist wl = new Wordlist(name);
        Line l;
        if (node1.size() == node2.size()) {
            for (int i = 0; i < node1.size(); i++) {
                ArrayList<String> p = new ArrayList<>(Arrays.asList(node1.get(i).split("[,/]")));
                ArrayList<String> t = new ArrayList<>(Arrays.asList(node2.get(i).split("[,/]")));
                l = new Line(wl, p, t);
                wl.addLine(l);
            }
        }
        wordlists.add(wl);
    }


}
