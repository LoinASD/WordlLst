package io.cyanlab.loinasd.wordllst.controller;

import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public interface ListEditor {

    public void editList(WordList list);

    public void editLine(Node node);

    public interface Listener{

        void changeLine(Node node);

        void removeLine(Node node);

        void copyLine(Node node);

        void pasteLine(Node node);

        void cutLine(Node node);

        void changeList(WordList list, String newName);

        void deleteList(WordList list);
    }


}
