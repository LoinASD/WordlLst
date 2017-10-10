package io.cyanlab.loinasd.wordllst.controller.pdf;

public class Range {

    private final int begin;
    private final int end;
    private final int dif;
    private final int newRange;
    private final int endNewRange;

    Range (final int begin, final int end, final int newRange){
        this.begin = begin;
        this.end = end;
        this.newRange = newRange;
        this.dif = end - begin;
        this.endNewRange = newRange + dif;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public int getDif() {
        return dif;
    }

    public int getNewRange() {
        return newRange;
    }

    public int getendNewRange() {
        return endNewRange;
    }

    public int[] getRangeArray() {
        int[] arr = new int[dif];
        for (int i = 0; i < dif; i++) {
            arr[i] = begin + i;
        }
        return arr;
    }
}

