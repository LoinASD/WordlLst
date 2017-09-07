package io.cyanlab.loinasd.wordllst.model;

import java.util.ArrayList;

final class Line {

    private ArrayList<Word> prime = new ArrayList<>();
    private ArrayList<Word> translate = new ArrayList<>();
    private Lang[] langs = new Lang[2];

    public Line(ArrayList<Word> prime, ArrayList<Word> translate, Lang[] langs) {
        this.prime = prime;
        this.translate = translate;
        this.langs = langs;
    }

    public ArrayList<Word> getPrime() {
        return prime;
    }

    public void setPrime(ArrayList<Word> prime) {
        this.prime = prime;
    }

    public ArrayList<Word> getTranslate() {
        return translate;
    }

    public void setTranslate(ArrayList<Word> translate) {
        this.translate = translate;
    }

    public Lang[] getLangs() {
        return langs;
    }

    public void setLangs(Lang[] langs) {
        this.langs = langs;
    }
}
