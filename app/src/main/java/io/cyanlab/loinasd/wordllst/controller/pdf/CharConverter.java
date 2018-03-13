package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.util.ArrayList;
class CharConverter {

    private ArrayList<Range> ranges = new ArrayList<>();

    public CharConverter() {}

    public char convert(int c) {
        int res;
        for (Range range : ranges) {

            if (c >= range.getBegin() && c <= range.getEnd()) {
                res = range.getNewRange() + (c - range.getBegin());
                return (char) res;
            }
        }
        return (char) c;
    }

    public void addNewRange(int c1, int c2, int c3) {
        ranges.add(new Range(c1, c2, c3));
    }
}

