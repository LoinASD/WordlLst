package io.cyanlab.loinasd.wordllst.model;


import android.support.annotation.NonNull;


class Word {

    private final String word;
    private Line line;

    public Word(@NonNull String word, @NonNull Line line) {
        this.word = word;
        this.line = line;
    }

    public Word(@NonNull String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public Line getLine(){
        return this.line;
    }


}
