package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.util.ArrayList;

public class CharConverter {

    private ArrayList<Range> ranges = new ArrayList<>();

    public CharConverter() {

        ranges.add(new Range(0x023A, 0x023E, 0x0410));
        ranges.add(new Range(0x0240, 0x0242, 0x0416));
        ranges.add(new Range(0x0244, 0x0245, 0x041A));
        ranges.add(new Range(0x0247, 0x024D, 0x041D));
        ranges.add(new Range(0x025A, 0x0273, 0x0430));
        ranges.add(new Range(0x0275, 0x0276, 0x044B));
        ranges.add(new Range(0x0278, 0x0279, 0x044E));


    }

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

}

