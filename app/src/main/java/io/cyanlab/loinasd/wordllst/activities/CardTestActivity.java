package io.cyanlab.loinasd.wordllst.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.database.LocalDatabase;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;


/**
 * Created by Анатолий on 25.12.2017.
 */

public class CardTestActivity extends AppCompatActivity implements View.OnClickListener {

    String wlName;
    String primary;
    List<Node> data;
    WordList list;
    Node curNode;
    TextView card;
    ArrayList<Integer> stack = new ArrayList<>();
    boolean isChecked;
    private GestureDetector detector;
    private LocalDatabase db = MainActivity.database;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_test);

        card = findViewById(R.id.card);

        wlName = getIntent().getStringExtra("Name");

        Thread getData = new Thread(new Runnable() {
            @Override
            public void run() {
                data = db.nodeDao().getNodes(wlName);
                list = db.listDao().getWordlist(wlName);
            }
        });
        getData.start();
        try {
            getData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (data.size() < 6) {
            Toast.makeText(this, "You must have at least 6 lines to use Card Test", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            setTitle(wlName);
            card.setOnClickListener(this);
            final Activity activity = this;
            detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                    float sensivity = 75;

                    boolean isDone = false;

                    Animation animation = null;

                    if (e1.getX() - e2.getX() > sensivity) {
                        isDone = true;
                        animation = getFailureAnimation();

                    } else if (e2.getX() - e1.getX() > sensivity) {
                        isDone = true;
                        animation = getSuccessAnimation();
                    }

                    if (animation != null && isDone) {
                        animateCard(animation);
                    }

                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });
            card.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isChecked) {
                        detector.onTouchEvent(event);
                    }
                    return false;
                }
            });

            findViewById(R.id.failure).setOnClickListener(this);
            findViewById(R.id.success).setOnClickListener(this);

            loadLine();
        }

    }

    private void animateCard(Animation animation){

        Thread updater = new Thread(new Runnable() {
            @Override
            public void run() {
                db.listDao().updateList(list);
                db.nodeDao().updateNode(curNode);
            }
        });

        updater.start();

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadLine();
                card.setAlpha(0f);
                card.animate().alpha(1f).setDuration(300).start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        try {
            updater.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        card.startAnimation(animation);
    }

    private Animation getFailureAnimation(){
        if (curNode.getWeight() > ShowFragment.RIGHT_ANSWERS_TO_COMPLETE) {
            curNode.setWeight(curNode.getWeight() - 1);
            list.currentWeight--;
        }
        curNode.setWeight(curNode.getWeight() + 1);
        list.currentWeight++;
        return AnimationUtils.loadAnimation(this, R.anim.flip_out_rtl);
    }

    private Animation getSuccessAnimation(){
        if (curNode.getWeight() == 1) {
            curNode.setWeight(curNode.getWeight() + 1);
            list.currentWeight++;
        }
        curNode.setWeight(curNode.getWeight() - 1);
        list.currentWeight--;
        return AnimationUtils.loadAnimation(this, R.anim.flip_out_ltr);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.failure:{
                if (isChecked){
                    animateCard(getFailureAnimation());
                }
                break;
            }
            case R.id.success:{
                if (isChecked){
                    animateCard(getSuccessAnimation());
                }
                break;
            }
        }

        if (!isChecked) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(card, View.ROTATION_Y, 0f, 90f).setDuration(150);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    (card).animate().scaleX(0.3f).setDuration(150).start();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    card.setText(primary);
                    isChecked = true;

                    (card).animate().rotationY(-90f).setDuration(0).setInterpolator(new DecelerateInterpolator()).start();
                    (card).animate().rotationY(0f).scaleX(1f).setDuration(150).start();
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

    private void loadLine() {
        isChecked = false;
        Random r = new Random();
        int lineNum = r.nextInt(list.currentWeight + 1);
        int sumWeight = 0;
        int i = 0;
        curNode = null;
        do {
            if (lineNum < sumWeight + data.get(i).getWeight()) {
                curNode = data.get(i);
                break;
            }
            sumWeight += data.get(i++).getWeight();
        } while (i < data.size());
        if ((curNode != null)) {
            if (!stack.contains(data.indexOf(curNode))) {
                if (stack.size() == data.size() / 3) {
                    stack.remove(0);
                }
                stack.add(data.indexOf(curNode));
                primary = curNode.getPrimText();
                ((TextView) findViewById(R.id.transTV)).setText(curNode.getTransText() + " (" + primary.split(",").length + ")");
                 card.setText("Turn Over");
            } else loadLine();
        } else loadLine();
    }


}
