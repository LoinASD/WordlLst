package io.cyanlab.loinasd.wordllst.view;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.activities.MainActivity;
import io.cyanlab.loinasd.wordllst.activities.ShowFragment;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;

public class BottomSheetManager {

    private View bottomSheet;

    private Context context;
    private ShowFragment fragment;

    private RecyclerView main;
    private View blurView;

    public void setExpandedNode(Node expandedNode) {
        this.expandedNode = expandedNode;
    }

    private Node expandedNode;
    public Node bufferedNode;

    private View.OnClickListener bottomSheetListener;

    public BottomSheetManager(Context context, ShowFragment fragment, View bottomSheetView){

        this.context = context;
        this.fragment = fragment;

        bottomSheet = bottomSheetView;

        blurView = ((View) bottomSheet.getParent()).findViewById(R.id.blur_view);

        blurView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeBottomSheet();
            }
        });

        main = ((View) bottomSheet.getParent()).findViewById(R.id.scrollView);

        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED && bottomSheet.findViewById(R.id.bre_edit_toolbar).getVisibility() == View.VISIBLE){
                    editLine(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (BottomSheetBehavior.from(bottomSheet).getState() == BottomSheetBehavior.STATE_DRAGGING) {
                    closeBottomSheet();
                }
            }
        });
    }

    public void closeBottomSheet(){

        if (bottomSheet.findViewById(R.id.bre_edit_toolbar).getVisibility() == View.VISIBLE){
            editLine(false);
        }


        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        blurView.setVisibility(View.INVISIBLE);
        main.setLayoutFrozen(false);
        main.setClickable(true);
    }

    public void openBottomSheet(String prim, String trans){
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);

        main.setLayoutFrozen(true);
        main.setClickable(false);

        blurView.setVisibility(View.VISIBLE);

        ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(prim);
        ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(trans);

        bottomSheet.findViewById(R.id.bre_prim).refreshDrawableState();

        bottomSheet.findViewById(R.id.bre_trans).refreshDrawableState();

        bottomSheet.refreshDrawableState();

        bottomSheetListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    switch (view.getId()){
                        case R.id.copy_line:{
                            bufferedNode = expandedNode;
                            Toast.makeText(context, "Line successfully copied", Toast.LENGTH_SHORT).show();
                            closeBottomSheet();
                            break;
                        }
                        case R.id.cut_line:{

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
                        case R.id.paste_line:{

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
                        case R.id.edit_line:{
                            editLine(true);
                        }
                    }

                    fragment.adapterLoadData();


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        bottomSheet.findViewById(R.id.copy_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.cut_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.paste_line).setOnClickListener(bottomSheetListener);
        bottomSheet.findViewById(R.id.edit_line).setOnClickListener(bottomSheetListener);
    }

    private void editLine(final boolean isEditing){

        final View toolbar1, toolbar2;



        if (isEditing){
            toolbar1 = bottomSheet.findViewById(R.id.ble_toolbar);
            toolbar2 = bottomSheet.findViewById(R.id.bre_edit_toolbar);
            //bottomSheet.getLayoutParams().height = getView().getHeight();

            View.OnClickListener editListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        switch (view.getId()){
                            case R.id.bre_cancel_edition:{
                                editLine(false);
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

            toolbar2.findViewById(R.id.bre_save_line).setOnClickListener(editListener);
            toolbar2.findViewById(R.id.bre_delete_line).setOnClickListener(editListener);
            toolbar2.findViewById(R.id.bre_cancel_edition).setOnClickListener(editListener);

        }else {
            ((EditText) bottomSheet.findViewById(R.id.bre_prim)).setText(expandedNode.getPrimText());
            ((EditText) bottomSheet.findViewById(R.id.bre_trans)).setText(expandedNode.getTransText());
            toolbar1 = bottomSheet.findViewById(R.id.bre_edit_toolbar);
            toolbar2 = bottomSheet.findViewById(R.id.ble_toolbar);
            //bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        bottomSheet.findViewById(R.id.bre_prim).setEnabled(isEditing);
        bottomSheet.findViewById(R.id.bre_trans).setEnabled(isEditing);




        toolbar1.animate().alpha(0).setDuration(100).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {

                bottomSheet.getLayoutParams().height = isEditing ? blurView.getHeight() : ViewGroup.LayoutParams.WRAP_CONTENT;

                toolbar1.setVisibility(View.GONE);
                toolbar2.setVisibility(View.VISIBLE);
                toolbar2.setAlpha(0);
                toolbar2.animate().alpha(1).setDuration(100).setListener(null).start();

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

    }

    public int getState(){
        return BottomSheetBehavior.from(bottomSheet).getState();
    }

}
