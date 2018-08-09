package io.cyanlab.loinasd.wordllst.view;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;
import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.activities.MainActivity;
import io.cyanlab.loinasd.wordllst.activities.ShowFragment;
import io.cyanlab.loinasd.wordllst.controller.ListEditor;
import io.cyanlab.loinasd.wordllst.controller.database.DataProvider;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class BottomSheetManager implements ListEditor {

    private View bottomSheet;

    private Listener listener;

    private RecyclerView main;
    private View blurView;

    private Node expandedNode;

    private WordList expandedList;
    public Node bufferedNode;

    private View.OnClickListener editLineListener;
    private View.OnClickListener editListListener;

    public BottomSheetManager(Listener listener, View bottomSheetView){

        this.listener = listener;

        bottomSheet = bottomSheetView;

        blurView = ((View) bottomSheet.getParent()).findViewById(R.id.blur_view);

        main = ((View) bottomSheet.getParent()).findViewById(R.id.recycler_lines);

        setListeners();

        bottomSheet.setTranslationY(bottomSheet.getHeight());
    }

    @Override
    public void editLine(Node node) {

        expandBottomSheet(node);
    }

    @Override
    public void editList(WordList list) {

        expandBottomSheet(list);
    }

    private void setListeners(){
        View.OnClickListener bottomSheetListener = view -> {

            switch (view.getId()) {

                case R.id.copy_line: {

                    bufferedNode = expandedNode;

                    listener.copyLine(bufferedNode);

                    closeBottomSheet();
                    break;
                }
                case R.id.cut_line: {

                    bufferedNode = expandedNode;

                    closeBottomSheet();

                    listener.cutLine(bufferedNode);

                    break;
                }
                case R.id.paste_line: {

                    if (bufferedNode == null)
                        break;

                    ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(bufferedNode.primText);
                    ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(bufferedNode.transText);

                    expandedNode.primText = bufferedNode.primText;
                    expandedNode.transText = bufferedNode.transText;

                    listener.pasteLine(expandedNode);

                    break;
                }
                case R.id.edit_line: {
                    changeState(true);
                }
            }

        };

        bottomSheet.findViewById(R.id.copy_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.cut_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.paste_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.edit_line).setOnClickListener(bottomSheetListener);

        editLineListener = view -> {
            switch (view.getId()){

                case R.id.bre_cancel_edition:{

                    changeState(false);
                    break;
                }
                case R.id.bre_save_line:{

                    expandedNode.primText = ((EditText) bottomSheet.findViewById(R.id.bre_prim)).getText().toString();
                    expandedNode.transText = ((EditText) bottomSheet.findViewById(R.id.bre_trans)).getText().toString();

                    closeBottomSheet();

                    listener.changeLine(expandedNode);

                    break;

                }case R.id.bre_delete_line:{

                    listener.removeLine(expandedNode);

                    closeBottomSheet();

                    break;
                }
            }
        };

        editListListener = view -> {
            switch (view.getId()){
                case R.id.bre_cancel_edition:{
                    closeBottomSheet();
                    break;
                }
                case R.id.bre_save_line:{

                    final String newName = ((EditText) bottomSheet.findViewById(R.id.bre_prim)).getText().toString();

                    closeBottomSheet();

                    listener.changeList(expandedList, newName);

                    break;

                }case R.id.bre_delete_line:{

                    listener.deleteList(expandedList);

                    closeBottomSheet();

                    break;
                }
            }
        };
    }

    private void closeBottomSheet(){

        InputMethodManager manager = ((InputMethodManager)bottomSheet.getContext().getSystemService(Context.INPUT_METHOD_SERVICE));

        manager.hideSoftInputFromWindow(bottomSheet.getWindowToken(), 0);

        blurView.setAlpha(1f);
        blurView.animate().alpha(0f).setDuration(195).start();

        bottomSheet.animate().translationY(bottomSheet.getHeight()).setDuration(195).setInterpolator(new AccelerateInterpolator()).withEndAction(() -> {

            blurView.setVisibility(View.INVISIBLE);

            bottomSheet.setVisibility(View.GONE);

            changeState(false);

            main.setLayoutFrozen(false);
            main.setClickable(true);

        }).start();


    }

    public void expandBottomSheet(Node node){

        expandedNode = node;

        bottomSheet.findViewById(R.id.bre_trans).setVisibility(View.VISIBLE);

        ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(node.primText);
        ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(node.transText);

        View toolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);

        toolbar.findViewById(R.id.bre_save_line).setOnClickListener(editLineListener);
        toolbar.findViewById(R.id.bre_delete_line).setOnClickListener(editLineListener);
        toolbar.findViewById(R.id.bre_cancel_edition).setOnClickListener(editLineListener);

        openBottomSheet();

    }

    public void expandBottomSheet(WordList list){
        
		((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(list.name);
        
		expandedList = list;
        
		bottomSheet.findViewById(R.id.bre_trans).setVisibility(View.GONE);

        View toolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);
		
        toolbar.findViewById(R.id.bre_save_line).setOnClickListener(editListListener);
        toolbar.findViewById(R.id.bre_delete_line).setOnClickListener(editListListener);
        toolbar.findViewById(R.id.bre_cancel_edition).setOnClickListener(editListListener);

        openBottomSheet();

        changeState(true);

    }

    private void openBottomSheet(){

        main.setLayoutFrozen(true);
        main.setClickable(false);

        blurView.setVisibility(View.VISIBLE);
        blurView.setAlpha(0f);
        blurView.animate().alpha(1f).setDuration(225).start();

        bottomSheet.setVisibility(View.VISIBLE);

        blurView.setOnClickListener(view -> closeBottomSheet());

        bottomSheet.findViewById(R.id.bre_prim).refreshDrawableState();

        bottomSheet.findViewById(R.id.bre_trans).refreshDrawableState();

        bottomSheet.refreshDrawableState();

        bottomSheet.setTranslationY(bottomSheet.getHeight());

        bottomSheet.animate().translationY(0).setDuration(225).setInterpolator(new DecelerateInterpolator()).start();
    }

    private void changeState(final boolean isEditing){

        final View hidingToolbar, appearingToolbar;

        int newHeight;
        int oldHeight;

        if (isEditing){

            hidingToolbar = bottomSheet.findViewById(R.id.ble_toolbar);
            appearingToolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);

            newHeight = blurView.getHeight() - ((int) bottomSheet.getContext().getResources().getDimension(R.dimen.app_bar_height));

            oldHeight = bottomSheet.getHeight();


        }else {

            if (expandedNode != null) {

                ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(expandedNode.primText);
                ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(expandedNode.transText);
            }

            if(bottomSheet.findViewById(R.id.bre_trans).getVisibility() == View.GONE) {

                bottomSheet.findViewById(R.id.bre_trans).setVisibility(View.VISIBLE);
            }

            hidingToolbar = bottomSheet.findViewById(R.id.bre_edit_toolbar);
            appearingToolbar = bottomSheet.findViewById(R.id.ble_toolbar);

            oldHeight = bottomSheet.getHeight();
            newHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

        }

        bottomSheet.findViewById(R.id.bre_prim).setEnabled(isEditing);
        bottomSheet.findViewById(R.id.bre_trans).setEnabled(isEditing);

        bottomSheet.animate().translationY(oldHeight).setDuration(225).withEndAction(() -> {

            bottomSheet.getLayoutParams().height = newHeight;

            bottomSheet.setTranslationY(bottomSheet.getHeight());

            bottomSheet.animate().translationY(0).setDuration(225).setInterpolator(new DecelerateInterpolator()).start();

            hidingToolbar.setVisibility(View.GONE);
            appearingToolbar.setVisibility(View.VISIBLE);
            appearingToolbar.setAlpha(0);
            appearingToolbar.animate().alpha(1).setDuration(100).setListener(null).start();

            View edit = bottomSheet.findViewById(R.id.bre_prim);

            if (isEditing && edit.requestFocus()){
                InputMethodManager manager = ((InputMethodManager)bottomSheet.getContext().getSystemService(Context.INPUT_METHOD_SERVICE));

                manager.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
            };

        }).setInterpolator(new AccelerateDecelerateInterpolator()).start();

    }

    public int getState(){
        return BottomSheetBehavior.from(bottomSheet).getState();
    }

}
