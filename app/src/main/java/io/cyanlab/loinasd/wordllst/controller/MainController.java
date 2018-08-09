package io.cyanlab.loinasd.wordllst.controller;

import android.support.annotation.AnyThread;

import io.cyanlab.loinasd.wordllst.controller.pdf.ListEditorHolder;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class MainController implements ListsHolder.Listener, PDFListLoader.Listener, ListEditorHolder.Listener{

    ListsHolder listsHolder;
    LinesHolder linesHolder;
    FragmentsController fragmentsController;
    ListEditorHolder editorHolder;

    public MainController(ListsHolder p0, LinesHolder p1, FragmentsController p2, ListEditorHolder p3){

        listsHolder = p0;
        linesHolder = p1;
        fragmentsController = p2;
        editorHolder = p3;

        listsHolder.setListener(this);
        editorHolder.setListener(this);
    }

    public void editList(WordList list){

        editorHolder.getEditor().editList(list);
    }

    @Override
    public void onListSelected(WordList list) {

        linesHolder.loadLines(list);
        fragmentsController.showLines(list);
    }

    @Override
    public void onListRemoved(WordList list) {

        fragmentsController.hideLines();

        listsHolder.removeList(list);
    }

    public void loadFromPDF(PDFListLoader loader, String file){

        loader.setListener(this);
        fragmentsController.onPDFLoadingStarted();

        loader.loadPDFList(file);
    }

    @Override
    @AnyThread
    public void onListLoaded(WordList list) {

        fragmentsController.onPDFLoadingFinished();

        listsHolder.addList(list);

        linesHolder.loadLines(list);

        fragmentsController.showLines(list);
    }

    @Override
    public void onError(String what) {

        fragmentsController.onPDFError(what);
    }

    @Override
    public void onLineChanged(WordList list) {

        listsHolder.updateList(list);
    }

    @Override
    public void onLineAdded(WordList list) {

        listsHolder.updateList(list);
    }

    @Override
    public void onLineRemoved(WordList list) {

        listsHolder.updateList(list);
    }

    @Override
    public void onListChanged(WordList list) {

        listsHolder.updateList(list);
        fragmentsController.onListChanged(list);
    }
}






