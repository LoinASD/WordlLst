package io.cyanlab.loinasd.wordllst.view;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.activities.MainActivity;
import io.cyanlab.loinasd.wordllst.activities.ShowFragment;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class BottomSheetManager {

    private View bottomSheet;

    private MainActivity context;
    private ShowFragment fragment;

    private RecyclerView main;
    private View blurView;

    private Node expandedNode;

    private WordList expandedList;
    public Node bufferedNode;

    private View.OnClickListener editLineListener;
    private View.OnClickListener editListListener;

    public BottomSheetManager(Context context, ShowFragment fragment, View bottomSheetView){

        this.context = (MainActivity)context;
        this.fragment = fragment;

        bottomSheet = bottomSheetView;

        blurView = ((View) bottomSheet.getParent()).findViewById(R.id.blur_view);

        main = ((View) bottomSheet.getParent()).findViewById(R.id.scrollView);

        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {


            boolean hasBeenDragged;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED && bottomSheet.findViewById(R.id.bre_edit_toolbar).getVisibility() == View.VISIBLE){
                    closeBottomSheet();
                }
                if (hasBeenDragged && newState == BottomSheetBehavior.STATE_EXPANDED){
                    hasBeenDragged = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (BottomSheetBehavior.from(bottomSheet).getState() == BottomSheetBehavior.STATE_DRAGGING) {
                    hasBeenDragged = true;
                }
                if (hasBeenDragged && BottomSheetBehavior.from(bottomSheet).getState() == BottomSheetBehavior.STATE_SETTLING){
                    hasBeenDragged = false;
                    closeBottomSheet();
                }
            }
        });

        setListeners();
    }

    private void setListeners(){
        View.OnClickListener bottomSheetListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    switch (view.getId()) {
                        case R.id.copy_line: {
                            bufferedNode = expandedNode;
                            Toast.makeText(context, "Line successfully copied", Toast.LENGTH_SHORT).show();
                            closeBottomSheet();
                            break;
                        }
                        case R.id.cut_line: {

                            bufferedNode = expandedNode;

                            Thread deleteNode = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.database.nodeDao().deleteNode(expandedNode);
                                }
                            });

                            deleteNode.start();
                            deleteNode.join();

                            closeBottomSheet();
                            Toast.makeText(context, "Line successfully cut", Toast.LENGTH_SHORT).show();

                            break;
                        }
                        case R.id.paste_line: {

                            if (bufferedNode != null) {
                                ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(bufferedNode.getPrimText());
                                ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(bufferedNode.getTransText());

                                expandedNode.setPrimText(bufferedNode.getPrimText());
                                expandedNode.setTransText(bufferedNode.getTransText());

                                Thread updateNode = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.database.nodeDao().updateNode(expandedNode);
                                    }
                                });
                                updateNode.start();
                                Toast.makeText(context, "Line successfully pasted", Toast.LENGTH_SHORT).show();
                                updateNode.join();
                                break;
                            }
                        }
                        case R.id.edit_line: {
                            changeState(true);
                        }
                    }

                    fragment.adapterLoadData();


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };

        bottomSheet.findViewById(R.id.copy_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.cut_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.paste_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.edit_line).setOnClickListener(bottomSheetListener);

        editLineListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    switch (view.getId()){
                        case R.id.bre_cancel_edition:{
                            changeState(false);
                            break;
                        }
                        case R.id.bre_save_line:{
                            expandedNode.setPrimText(((EditText) bottomSheet.findViewById(R.id.bre_prim)).getText().toString());
                            expandedNode.setTransText(((EditText) bottomSheet.findViewById(R.id.bre_trans)).getText().toString());

                            Thread updateNode = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.database.nodeDao().updateNode(expandedNode);
                                }
                            });
                            updateNode.start();

                            updateNode.join();

                            closeBottomSheet();
                            Toast.makeText(context, "Line successfully saved", Toast.LENGTH_SHORT).show();

                            fragment.adapterLoadData();
                            break;
                        }case R.id.bre_delete_line:{

                            Thread deleteNode = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.database.nodeDao().deleteNode(expandedNode);
                                }
                            });

                            deleteNode.start();
                            deleteNode.join();



                            closeBottomSheet();

                            Toast.makeText(context, "Line successfully deleted", Toast.LENGTH_SHORT).show();
                            fragment.adapterLoadData();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        editListListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    switch (view.getId()){
                        case R.id.bre_cancel_edition:{
                            closeBottomSheet();
                            break;
                        }
                        case R.id.bre_save_line:{

                            final String newName = ((EditText) bottomSheet.findViewById(R.id.bre_prim)).getText().toString();

                            Thread updateList = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        MainActivity.database.beginTransaction();

                                        List<Node> nodes = MainActivity.database.nodeDao().getNodes(expandedList.getWlName());

                                        for (Node node: nodes){
                                            node.setWlName(newName);
                                            MainActivity.database.nodeDao().updateNode(node);
                                        }

                                        expandedList.setWlName(newName);
                                        MainActivity.database.listDao().updateList(expandedList);

                                        MainActivity.database.setTransactionSuccessful();
                                        MainActivity.LIST_NAME = newName;

                                    } finally {
                                        MainActivity.database.endTransaction();
                                    }
                                }
                            });
                            updateList.start();

                            updateList.join();

                            closeBottomSheet();
                            Toast.makeText(context, "List name successfully changed", Toast.LENGTH_SHORT).show();

                            ((ShowFragment)context.lists).notifyAdapter();
                            fragment.adapterLoadData();
                            fragment.changeHeader();

                            break;

                        }case R.id.bre_delete_line:{

                            final String wlName = expandedList.getWlName();

                            Thread deleteWL = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MainActivity.database.nodeDao().deleteNodes(wlName);
                                    MainActivity.database.listDao().deleteList(expandedList);
                                }
                            });
                            deleteWL.start();
                            try {
                                deleteWL.join();
                                Toast.makeText(context, "Wordlist " + wlName + " has been successfully deleted", Toast.LENGTH_SHORT).show();
                            } catch (InterruptedException exception) {
                                exception.printStackTrace();
                            }

                            MainActivity.LIST_NAME = null;
                            ((ShowFragment) context.lists).notifyAdapter();
                            fragment.notifyAdapter();
                            context.loadLists();

                            closeBottomSheet();

                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void closeBottomSheet(){

        if (bottomSheet.findViewById(R.id.bre_edit_toolbar).getVisibility() == View.VISIBLE){
            changeState(false);
        }

        fragment.getView().findViewById(R.id.tests_button).setClickable(true);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        blurView.setVisibility(View.INVISIBLE);
        main.setLayoutFrozen(false);
        main.setClickable(true);
    }

    public void expandBottomSheet(Node node){

        expandedNode = node;
        ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(node.getPrimText());
        ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(node.getTransText());

        View toolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);
        toolbar.findViewById(R.id.bre_save_line).setOnClickListener(editLineListener);
        toolbar.findViewById(R.id.bre_delete_line).setOnClickListener(editLineListener);
        toolbar.findViewById(R.id.bre_cancel_edition).setOnClickListener(editLineListener);

        openBottomSheet();

    }

    public void expandBottomSheet(WordList list){
        ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(list.getWlName());
        expandedList = list;
        bottomSheet.findViewById(R.id.bre_trans).setVisibility(View.GONE);

        View toolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);
        toolbar.findViewById(R.id.bre_save_line).setOnClickListener(editListListener);
        toolbar.findViewById(R.id.bre_delete_line).setOnClickListener(editListListener);
        toolbar.findViewById(R.id.bre_cancel_edition).setOnClickListener(editListListener);

        bottomSheet.getLayoutParams().height = blurView.getHeight();

        changeState(true);

        openBottomSheet();



    }

    private void openBottomSheet(){
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        main.setLayoutFrozen(true);
        main.setClickable(false);

        blurView.setVisibility(View.VISIBLE);

        fragment.getView().findViewById(R.id.tests_button).setClickable(false);

        blurView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeBottomSheet();
            }
        });

        bottomSheet.findViewById(R.id.bre_prim).refreshDrawableState();

        bottomSheet.findViewById(R.id.bre_trans).refreshDrawableState();

        bottomSheet.refreshDrawableState();

        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void changeState(final boolean isEditing){

        final View hidingToolbar, appearingToolbar;

        if (isEditing){
            hidingToolbar = bottomSheet.findViewById(R.id.ble_toolbar);
            appearingToolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);
            //bottomSheet.getLayoutParams().height = getView().getHeight();

        }else {
            if (expandedNode != null) {
                ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(expandedNode.getPrimText());
                ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(expandedNode.getTransText());
            }
            if(bottomSheet.findViewById(R.id.bre_trans).getVisibility() == View.GONE) {
                bottomSheet.findViewById(R.id.bre_trans).setVisibility(View.VISIBLE);
            }
            hidingToolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);
            appearingToolbar = bottomSheet.findViewById(R.id.ble_toolbar);
            //bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        bottomSheet.findViewById(R.id.bre_prim).setEnabled(isEditing);
        bottomSheet.findViewById(R.id.bre_trans).setEnabled(isEditing);


        hidingToolbar.animate().alpha(0).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                bottomSheet.getLayoutParams().height = isEditing ? blurView.getHeight() : ViewGroup.LayoutParams.WRAP_CONTENT;

                hidingToolbar.setVisibility(View.GONE);
                appearingToolbar.setVisibility(View.VISIBLE);
                appearingToolbar.setAlpha(0);
                appearingToolbar.animate().alpha(1).setDuration(100).setListener(null).start();

            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        }).start();

    }

    public int getState(){
        return BottomSheetBehavior.from(bottomSheet).getState();
    }

}
