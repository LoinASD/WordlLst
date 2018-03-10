package io.cyanlab.loinasd.wordllst.controller.pdf;

import java.util.ArrayList;

/**
 * Created by Lev on 01.03.2018.
 */

public class WordList {

    private ArrayList<Node> prim;
    private ArrayList<Node> trans;

    protected WordList(ArrayList<Node> prim, ArrayList<Node> trans) {
        this.prim = prim;
        this.trans = trans;
    }

}
