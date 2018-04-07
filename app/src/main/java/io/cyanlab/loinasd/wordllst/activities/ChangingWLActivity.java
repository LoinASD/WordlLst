package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class ChangingWLActivity extends AppCompatActivity implements View.OnClickListener {

    int lineID;
    String wlName;
    boolean isAdding;
    boolean isChangingList;
    Node node;
    Vibrator vibrator;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*switch (getIntent().getStringExtra("Action")) {
            case ("Change"): {
                setContentView(io.cyanlab.loinasd.wordllst.R.layout.activity_changing_line);

                isAdding = false;
                node = (Node) getIntent().getSerializableExtra("Node");
                wlName = node.getWlName();
                ((EditText) findViewById(R.id.primET)).setText(node.getPrimText());
                ((EditText) findViewById(R.id.transET)).setText(node.getTransText());
                findViewById(R.id.primET).refreshDrawableState();
                findViewById(R.id.transET).refreshDrawableState();
                findViewById(R.id.saveBut).setOnClickListener(this);
                findViewById(R.id.clearBut).setOnClickListener(this);

                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

                final Activity activity = this;
                final GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {


                        Thread delete = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.database.nodeDao().deleteNode(node);
                                MainActivity.database.nodeDao().deleteNode(node);
                                WordList list = MainActivity.database.listDao().getWordlist(wlName);
                                list.maxWeight -= ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                                list.currentWeight -= node.getWeight();
                                MainActivity.database.listDao().updateList(list);
                            }
                        });

                        try {
                            delete.start();
                            delete.join();

                            Toast.makeText(activity, "Line has been successfully deleted", Toast.LENGTH_SHORT).show();

                        } catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }

                        activity.finish();

                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {

                        Toast.makeText(activity, "Double-tap this button to delete this line", Toast.LENGTH_SHORT).show();

                        activity.findViewById(R.id.delLineBut).animate().rotation(7).setInterpolator(AnimationUtils.loadInterpolator(activity,R.anim.cycle_7)).setDuration(200).start();

                        vibrator.vibrate(150);

                        return false;
                    }
                });

                (findViewById(R.id.delLineBut)).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return detector.onTouchEvent(motionEvent);
                    }
                });

                break;
            }
            case ("Change list"): {
                setContentView(R.layout.activity_change_list);
                wlName = getIntent().getStringExtra("Name");
                ((EditText) findViewById(R.id.reqest_del)).setText(wlName);
                findViewById(R.id.reqest_del).refreshDrawableState();
                findViewById(R.id.save_list).setOnClickListener(this);
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


                final Activity activity = this;
                final GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {

                        Intent data = new Intent();

                        data.putExtra("Action", "Delete");

                        Thread deleteWL = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.database.nodeDao().deleteNodes(wlName);
                                MainActivity.database.listDao().deleteList(wlName);
                            }
                        });
                        deleteWL.start();
                        try {
                            deleteWL.join();
                            Toast.makeText(activity, "Wordlist " + wlName + " has been successfully deleted", Toast.LENGTH_SHORT).show();
                        } catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }

                        activity.setResult(RESULT_OK, data);

                        activity.finish();

                        return true;
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {

                        Toast.makeText(activity, "Double-tap this button to delete " + wlName, Toast.LENGTH_SHORT).show();

                        activity.findViewById(R.id.delBut).animate().rotation(10).setInterpolator(AnimationUtils.loadInterpolator(activity,R.anim.cycle_7)).setDuration(200).start();

                        vibrator.vibrate(150);

                        return false;
                    }
                });

                (findViewById(R.id.delBut)).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return detector.onTouchEvent(motionEvent);
                    }
                });

                isChangingList = true;
                break;

            }
            case ("Add"): {
                setContentView(R.layout.activity_add_wl);
                final Activity activity = this;
                vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                findViewById(R.id.saveBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((EditText) findViewById(R.id.namePlace)).getText().toString().equals("")){
                            v.animate().rotation(10).setInterpolator(AnimationUtils.loadInterpolator(activity,R.anim.cycle_7)).setDuration(200).start();
                            vibrator.vibrate(150);
                            Toast.makeText(ChangingWLActivity.this, "Don't leave a name field empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        setResult(RESULT_OK,
                                new Intent().putExtra("Name", ((EditText) findViewById(R.id.namePlace)).getText().toString().replaceAll(" ", "_")));
                        finish();
                    }
                });

                break;
            }
            case ("AddLine"): {
                setContentView(io.cyanlab.loinasd.wordllst.R.layout.activity_changing_line);

                isAdding = true;
                wlName = getIntent().getStringExtra("Name");

                findViewById(R.id.primET).refreshDrawableState();
                findViewById(R.id.transET).refreshDrawableState();
                findViewById(R.id.saveBut).setOnClickListener(this);
                findViewById(R.id.delLineBut).setVisibility(View.GONE);
                findViewById(R.id.clearBut).setOnClickListener(this);

                break;
            }
        }*/

        //-----------------------------------------//


    }


    @Override
    public void onClick(View v) {
        /*switch (v.getId()) {
            case (R.id.saveBut): {

                try {
                    if (isAdding) {
                        node = new Node();
                        node.setWlName(wlName);
                        node.setWeight(ShowFragment.RIGHT_ANSWERS_TO_COMPLETE);
                    }
                    node.setPrimText(((EditText) findViewById(R.id.primET)).getText().toString());
                    node.setTransText(((EditText) findViewById(R.id.transET)).getText().toString());
                    Thread save = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (isAdding) {
                                WordList list = MainActivity.database.listDao().getWordlist(wlName);
                                list.currentWeight += ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                                list.maxWeight += ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                                MainActivity.database.listDao().updateList(list);
                                MainActivity.database.nodeDao().insertNode(node);
                            } else
                                MainActivity.database.nodeDao().updateNode(node);
                        }
                    });

                    save.join();
                    save.start();

                    setResult(RESULT_OK,
                            new Intent().putExtra("Name", wlName));
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            *//*case (R.id.addBut): {
                if (!isAdding) {
                    isAdding = true;
                    ((ImageButton) findViewById(R.id.delLineBut)).setImageResource(android.R.drawable.arrow_down_float);
                    ((ImageButton) findViewById(R.id.saveBut)).setImageResource(android.R.drawable.arrow_up_float);
                    ((ImageButton) findViewById(R.id.addBut)).setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                } else {
                    isAdding = false;
                    ((ImageButton) findViewById(R.id.delLineBut)).setImageResource(android.R.drawable.ic_menu_delete);
                    ((ImageButton) findViewById(R.id.saveBut)).setImageResource(android.R.drawable.ic_menu_save);
                    ((ImageButton) findViewById(R.id.addBut)).setImageResource(android.R.drawable.ic_menu_add);
                }
                return;
            }*//*
            case (R.id.delBut): {
                setResult(RESULT_OK);
                break;
            }
            case (R.id.delLineBut): {
                setResult(RESULT_CANCELED);
                finish();
                break;
            }
            case (R.id.clearBut): {
                ((EditText) findViewById(R.id.primET)).setText("");
                ((EditText) findViewById(R.id.transET)).setText("");
                return;
            }
            case R.id.save_list: {
                Thread update = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            MainActivity.database.beginTransaction();

                            String newName = ((EditText)findViewById(R.id.reqest_del)).getText().toString();

                            WordList list = MainActivity.database.listDao().getWordlist(wlName);

                            List<Node> nodes = MainActivity.database.nodeDao().getNodes(list.getWlName());

                            for (Node node: nodes){
                                node.setWlName(newName);
                                MainActivity.database.nodeDao().updateNode(node);
                            }

                            list.setWlName(newName);
                            MainActivity.database.listDao().updateList(list);

                            MainActivity.database.setTransactionSuccessful();

                            Intent data = new Intent();

                            data.putExtra("New name", newName);

                            data.putExtra("Action", "Change Name");

                            setResult(RESULT_OK, data);

                        } finally {
                            MainActivity.database.endTransaction();
                        }
                    }
                });
                try {
                    update.start();
                    update.join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }*/
        finish();
    }

}
