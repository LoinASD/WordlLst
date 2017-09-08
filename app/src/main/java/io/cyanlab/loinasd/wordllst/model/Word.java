package io.cyanlab.loinasd.wordllst.model;


import android.support.annotation.NonNull;


class Word {

    private String word;
    private Lang lang;
    private Line line;

    public Word(@NonNull String word, @NonNull Lang lang, @NonNull Line line) {
        this.word = word;
        this.lang = lang;
        this.line = line;
    }

    public Word(Lang lang){
        this.lang = lang;
    }

    public void setLine(Line line){
        this.line = line;
    }

    public void setWord(String word){
        this.word = word;
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
