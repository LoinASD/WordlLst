package io.cyanlab.loinasd.wordllst.controller.pdf;

import io.cyanlab.loinasd.wordllst.controller.ListEditor;

public interface ListEditorHolder {

    public void setListener(Listener listener);

    public ListEditor getEditor();

    interface Listener{

        public void onLineChanged(WordList list);

        public void onLineAdded(WordList list);

        public void onLineRemoved(WordList list);

        public void onListChanged(WordList list);

        public void onListRemoved(WordList list);
    }
}
