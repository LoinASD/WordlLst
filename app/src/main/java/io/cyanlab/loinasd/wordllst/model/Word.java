package io.cyanlab.loinasd.wordllst.model;


import android.support.annotation.NonNull;


class Word {

    private final String word;
    private final Lang lang;
    private final Line line;

    public Word(@NonNull String word, @NonNull Lang lang, @NonNull Line line) {
        this.word = word;
        this.lang = lang;
        this.line = line;
    }

    public String getWord() {
        return word;
    }

    public Lang getLang() {
        return lang;
    }
    public Line getLine(){
        return this.line;
    }


}
