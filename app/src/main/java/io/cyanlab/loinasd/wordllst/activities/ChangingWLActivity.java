package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (getIntent().getStringExtra("Action")) {
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
                findViewById(R.id.delLineBut).setOnClickListener(this);
                findViewById(R.id.clearBut).setOnClickListener(this);
                //findViewById(R.id.addBut).setOnClickListener(this);
                break;
            }
            case ("Change list"): {
                setContentView(R.layout.activity_change_list);
                wlName = getIntent().getStringExtra("Name");
                ((EditText) findViewById(R.id.reqest_del)).setText(wlName);
                findViewById(R.id.save_list).setOnClickListener(this);
                findViewById(R.id.delBut).setOnClickListener(this);

                final Activity activity = this;
                final GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {

                        Intent data = new Intent();

                        data.putExtra("Action", "Delete");

                        activity.setResult(RESULT_OK, data);

                        activity.finish();
                        return super.onDoubleTap(e);
                    }
                });

                ((ImageButton) findViewById(R.id.delBut)).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        return detector.onTouchEvent(motionEvent);
                    }
                });

                findViewById(R.id.cancelBut).setOnClickListener(this);
                isChangingList = true;
                break;

            }
            case ("Add"): {
                setContentView(R.layout.activity_add_wl);
                findViewById(R.id.saveBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK,
                                new Intent().putExtra("Name", ((EditText) findViewById(R.id.namePlace)).getText().toString()));
                        finish();
                    }
                });
                findViewById(R.id.cancelBut).setOnClickListener(this);
                break;
            }
            case ("AddLine"): {
                setContentView(io.cyanlab.loinasd.wordllst.R.layout.activity_changing_line);

                isAdding = true;
                wlName = getIntent().getStringExtra("Name");

                findViewById(R.id.primET).refreshDrawableState();
                findViewById(R.id.transET).refreshDrawableState();
                findViewById(R.id.saveBut).setOnClickListener(this);
                findViewById(R.id.delLineBut).setOnClickListener(this);
                ((ImageButton) findViewById(R.id.delLineBut)).setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                findViewById(R.id.clearBut).setOnClickListener(this);
                //findViewById(R.id.addBut).setOnClickListener(this);
                break;
            }
        }

        //-----------------------------------------//


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.saveBut): {
                try {
                    if (!isAdding) {
                        node.setPrimText(((EditText) findViewById(R.id.primET)).getText().toString());
                        node.setTransText(((EditText) findViewById(R.id.transET)).getText().toString());

                    } else {
                        node = new Node();
                        node.setWlName(wlName);
                        node.setWeight(ShowFragment.RIGHT_ANSWERS_TO_COMPLETE);
                        node.setPrimText(((EditText) findViewById(R.id.primET)).getText().toString());
                        node.setTransText(((EditText) findViewById(R.id.transET)).getText().toString());
                    }

                    Thread save = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            if (isAdding) {
                                WordList list = NavActivity.database.listDao().getWordlist(wlName);
                                list.currentWeight += ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                                list.maxWeight += ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                                NavActivity.database.listDao().updateList(list);
                                NavActivity.database.nodeDao().insertNode(node);
                            } else
                                NavActivity.database.nodeDao().updateNode(node);
                        }
                    });

                    save.join();
                    save.start();

                    setResult(RESULT_OK,
                            new Intent().putExtra("Name", wlName));
                } catch (SQLiteException e) {
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            /*case (R.id.addBut): {
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
            }*/
            case (R.id.delBut): {
                if (!isChangingList) {
                    setResult(RESULT_OK);
                } else {
                    Toast.makeText(this, "Double-tap this button to delete " + wlName, Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            }
            case (R.id.delLineBut): {
                if (!isAdding) {
                    Thread delete = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NavActivity.database.nodeDao().deleteNode(node);
                            NavActivity.database.nodeDao().deleteNode(node);
                            WordList list = NavActivity.database.listDao().getWordlist(wlName);
                            list.maxWeight -= ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                            list.currentWeight -= node.getWeight();
                            NavActivity.database.listDao().updateList(list);
                        }
                    });

                    try {
                        delete.start();
                        delete.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                setResult(RESULT_OK,
                        new Intent().putExtra("Name", wlName));
                break;
            }
            case (R.id.clearBut): {
                ((EditText) findViewById(R.id.primET)).setText("");
                ((EditText) findViewById(R.id.transET)).setText("");
                return;
            }
            case (R.id.cancelBut): {
                setResult(RESULT_CANCELED);
                break;
            }
            case R.id.save_list: {
                Thread update = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        WordList list = NavActivity.database.listDao().getWordlist(wlName);

                        List<Node> nodes = NavActivity.database.nodeDao().getNodes(list.getWlName());


                        NavActivity.database.listDao().updateList(list);
                    }
                });
                try {
                    update.start();
                    update.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setResult(RESULT_OK);
            }

        }
        finish();
    }

}
