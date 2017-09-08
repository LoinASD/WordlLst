package io.cyanlab.loinasd.wordllst.model;

import java.util.ArrayList;

final class Line {

    private ArrayList<Word> prime = new ArrayList<>();
    private ArrayList<Word> translate = new ArrayList<>();
    private Lang[] langs = new Lang[2];
    private Wordlist wordlist;

    public Line(Wordlist wordlist, ArrayList<Word> prime, ArrayList<Word> translate, Lang[] langs) {
        this.prime = prime;
        this.translate = translate;
        this.langs = langs;
        this.wordlist = wordlist;
    }

    public Line(){}

    public ArrayList<Word> getPrime() {
        return prime;
    }

    public ArrayList<Word> getTranslate() {
        return translate;
    }

    public Lang[] getLangs() {
        return langs;
    }

    public Lang getPrimeLang(){
        return langs[0];
    }

    public Lang getTranslationLang(){
        return langs[1];
    }

    public Wordlist getWordlist(){
        return this.wordlist;
    }

    public void setWordlist (Wordlist wordlist){
        this.wordlist = wordlist;
    }

    public void setPrime(ArrayList<Word> words){
        this.prime = words;
    }

    public void setTranslate(ArrayList<Word> words){
        this.translate = words;
    }
}
