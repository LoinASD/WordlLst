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
}
