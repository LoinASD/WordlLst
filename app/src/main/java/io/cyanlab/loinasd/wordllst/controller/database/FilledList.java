package io.cyanlab.loinasd.wordllst.controller.database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

/**
 * Created by Анатолий on 13.03.2018.
 */

public class FilledList {

    @Embedded
    public WordList wordList;

    @Relation(parentColumn = "wlName", entityColumn = "nodeWLName")
    public List<Node> nodes;
}
