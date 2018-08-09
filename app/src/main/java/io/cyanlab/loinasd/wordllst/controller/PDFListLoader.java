package io.cyanlab.loinasd.wordllst.controller;

import android.support.annotation.AnyThread;

import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public interface PDFListLoader{

    public void loadPDFList(String file);

    public void setListener(Listener listener);

    interface Listener{

        @AnyThread
        public void onListLoaded(WordList list);

        public void onError(String what);
    }
}