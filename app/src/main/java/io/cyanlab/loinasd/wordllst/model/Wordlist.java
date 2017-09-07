package io.cyanlab.loinasd.wordllst.model;

import java.io.Serializable;
import java.util.ArrayList;


class Wordlist {

    private String name;
    private ArrayList<Line> lines = new ArrayList<>();

    public Wordlist(String name) {
        this.name = name;
    }

    public Wordlist(ArrayList<Line> lines) {
        this.lines = lines;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Line> getlines() {
        return lines;
    }

    public void setlines(ArrayList<Line> lines) {
        this.lines = lines;
    }

    public void addLine(Line line) {
        lines.add(line);
    }

    public void removeLine(Line line) {
        lines.remove(line);
    }
}
