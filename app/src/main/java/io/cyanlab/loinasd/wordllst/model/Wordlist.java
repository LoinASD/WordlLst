package io.cyanlab.loinasd.wordllst.model;

import java.io.Serializable;
import java.util.ArrayList;


class Wordlist {

    private String name;

    private ArrayList<Line> lines = new ArrayList<>();

    public Wordlist(String name, ArrayList<Line> lines) {
        this.lines = lines;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void addLine(Line line) {
        lines.add(line);
    }
}
