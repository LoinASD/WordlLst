package io.cyanlab.loinasd.wordllst.controller.pdf;

/**
 * Created by loinasd on 30.09.17.
 */

public class DictionaryParams {

    private boolean filter = false;
    private boolean flateDecode = false;
    private int length = 0;
    private boolean cryptDecode = false;

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public boolean isFlateDecode() {
        return flateDecode;
    }

    public void setFlateDecode(boolean flateDecode) {
        this.flateDecode = flateDecode;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isCryptDecode() {
        return cryptDecode;
    }

    public void setCryptDecode(boolean cryptDecode) {
        this.cryptDecode = cryptDecode;
    }

}
