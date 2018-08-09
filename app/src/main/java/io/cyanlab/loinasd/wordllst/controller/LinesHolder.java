package io.cyanlab.loinasd.wordllst.controller;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public interface LinesHolder{

    public void updateLines();

    public void updateLine(Node node);

    public void loadLines(WordList list);


}
