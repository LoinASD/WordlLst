package io.cyanlab.loinasd.wordllst.model;


import android.support.annotation.NonNull;

import java.util.ArrayList;

class Word {

    private final String word;
    private final Lang lang;

    private ArrayList<String> translation = new ArrayList<>();
    private ArrayList<String> synonyms = new ArrayList<>();

    public Word(@NonNull String word, @NonNull Lang lang) {
        this.word = word;
        this.lang = lang;
    }

    public String getWord() {
        return word;
    }

    public Lang getLang() {
        return lang;
    }

    public ArrayList<String> getTranslation() {
        return translation;
    }

    public void setTranslation(ArrayList<String> translation) {
        this.translation = translation;
    }

    public ArrayList<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(ArrayList<String> synonyms) {
        this.synonyms = synonyms;
    }
}
