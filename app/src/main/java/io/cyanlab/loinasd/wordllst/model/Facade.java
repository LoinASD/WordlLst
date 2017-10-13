package io.cyanlab.loinasd.wordllst.model;

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

        if (this.getWlNum(s) == -1){

            this.wordlists.add(new Wordlist(s));

            return this.getWlNum(s);

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
        ArrayList<Word> prims = new ArrayList<>();

        for (String s : prim) {
            prims.add( new Word(s, line));
        }

        line.setPrime(prims);

        ArrayList<Word> transls = new ArrayList<>();

        for (String s : trans) {
            transls.add(new Word(s,line));
        }

        line.setTranslate(transls);

        line.setWordlist(wordlists.get(wordListNum));

        wordlists.get(wordListNum).getLines().add(line);

    }

    public int getWlsCount(){

        return wordlists.size();

    }

    public int getWlNum(String name){

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

    public String getWlName(int wordlistNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return null;
        }
        else{
            return wordlists.get(wordlistNum).getName();
        }
    }

    public int getLinesCount(int wordlistNum){
        if ((wordlistNum > wordlists.size()-1)||(wordlistNum<0)){
            return -1;
        }
        else{
            return wordlists.get(wordlistNum).getLines().size();
        }
    }

    public ArrayList<String> getPrim(int wordlistNum, final int lineNum){
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

    public ArrayList<String> getTrans(int wordlistNum, final int lineNum){

        if ((wordlistNum > wordlists.size() - 1)||(wordlistNum < 0)){
            return null;
        } else{

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

    public int addNewWL(String name, ArrayList<String> node1, ArrayList<String> node2) {

        if (this.getWlNum(name) == -1){

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

            this.wordlists.add(wl);

            return this.getWlNum(name);

        }
        else{

            return -1;

        }
    }


}
