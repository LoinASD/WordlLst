package io.cyanlab.loinasd.wordllst.controller;

import java.util.List;

import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public interface ListsHolder{

    public void updateList(WordList list);

    public void updateLists();

    public void removeList(WordList list);

    public void addList(WordList list);

    public void setListener(Listener listener);

    public Listener getListener();

    public List<WordList> getLists();

    interface Listener {

        public void onListSelected(WordList list);
    }

}
