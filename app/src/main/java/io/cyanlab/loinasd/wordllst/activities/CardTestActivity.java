package io.cyanlab.loinasd.wordllst.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.database.LocalDatabase;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;


/**
 * Created by Анатолий on 25.12.2017.
 */

public class CardTestActivity extends AppCompatActivity implements View.OnClickListener {

    String wlName;
    String primary;
    List<Node> data;
    int id;
    int cur_weight;
    ArrayList<Integer> stack = new ArrayList<>();
    boolean isChecked;
    private GestureDetector detector;
    private LocalDatabase db = NavActivity.database;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_test);
        wlName = getIntent().getStringExtra("Name");

        data = db.nodeDao().getNodes(wlName);
        findViewById(R.id.card).setOnClickListener(this);
        final Activity activity = this;
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                float sensivity = 100;

                boolean isDone = false;

                Animation animation = AnimationUtils.loadAnimation(activity, R.anim.flip_out_ltr);

                if (e1.getX() - e2.getX() > sensivity) {
                    if (cur_weight > ShowFragment.RIGHT_ANSWERS_TO_COMPLETE) {
                        cur_weight--;
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("weight", ++cur_weight);
                    dbHelper.getWritableDatabase().update(wlName, contentValues, "_id = " + id, null);
                    isDone = true;
                    animation = AnimationUtils.loadAnimation(activity, R.anim.flip_out_rtl);

                } else if (e2.getX() - e1.getX() > sensivity) {
                    if (cur_weight == 1) {
                        cur_weight++;
                    }
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("weight", --cur_weight);
                    dbHelper.getWritableDatabase().update(wlName, contentValues, "_id = " + id, null);
                    isDone = true;
                    animation = AnimationUtils.loadAnimation(activity, R.anim.flip_out_ltr);
                }

                if (isDone) {


                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            loadLine();
                            findViewById(R.id.card).setAlpha(0f);
                            findViewById(R.id.card).animate().alpha(1f).setDuration(300).start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    findViewById(R.id.card).startAnimation(animation);
                }

                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        findViewById(R.id.card).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isChecked) {
                    detector.onTouchEvent(event);
                }
                return false;
            }
        });
        loadLine();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case (R.id.card): {
                if (!isChecked) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(findViewById(R.id.card), View.ROTATION_Y, 0f, 90f).setDuration(200);
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            findViewById(R.id.card).animate().scaleX(0.6f).setDuration(200).start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ((TextView) findViewById(R.id.card)).setText(primary);
                            isChecked = true;

                            findViewById(R.id.card).animate().rotationY(-90f).setDuration(0).start();
                            findViewById(R.id.card).animate().rotationY(0f).scaleX(1f).setDuration(200).start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();


                }
            }
        }
    }

    private void loadLine() {
        /*isChecked = false;
        Random r = new Random();
        int lineNum = r.nextInt(dbHelper.countWeight(wlName) + 1);
        data.moveToFirst();
        int idIndex = data.getColumnIndex("_id");
        int weightIndex = data.getColumnIndex("weight");
        int sumWeight = 0;
        do {
            if (lineNum < sumWeight + data.getInt(weightIndex)) {
                id = data.getInt(idIndex);
                cur_weight = data.getInt(weightIndex);
                break;
            }
            sumWeight += data.getInt(weightIndex);
        } while (data.moveToNext());
        if ((id != -1)) {
            if (!stack.contains(id)) {
                if (stack.size() == data.getCount()) {
                    stack.remove(0);
                }
                stack.add(id);
                primary = data.getString(data.getColumnIndex("prim"));
                ((TextView) findViewById(R.id.transTV)).setText(data.getString(data.getColumnIndex("trans")) + " (" + primary.split(",").length + ")");
                ((TextView) findViewById(R.id.card)).setText("Turn Over");
            } else loadLine();
        } else loadLine();*/
    }


}
