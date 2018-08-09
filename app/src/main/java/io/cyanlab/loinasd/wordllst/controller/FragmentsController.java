package io.cyanlab.loinasd.wordllst.controller;

import android.support.annotation.AnyThread;

import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public interface FragmentsController {

    public final static int STATE_LISTS = 0;
    public final static int STATE_LINES = 1;
    public final static int STATE_LOADING_PDF = 2;

    @AnyThread
    public void showLists();

    @AnyThread
    public void showLines(WordList list);

    public void hideLines();

    public void onListChanged(WordList list);

    public void onPDFLoadingStarted();

    @AnyThread
    public void onPDFLoadingFinished();

    @AnyThread
    public void onPDFError(String what);
}
