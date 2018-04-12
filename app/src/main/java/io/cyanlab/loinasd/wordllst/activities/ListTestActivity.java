package io.cyanlab.loinasd.wordllst.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;

/**
 * Created by Анатолий on 11.12.2017.
 */

public class ListTestActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    String wlName;
    String[] real;
    String[] fake;
    String[] shuffled;
    String translation;
    List<Node> data;
    ListView listView;
    int realChecked;
    int curLineNum;
    int basicBG = R.color.colorWhiteLowAlpha;
    ArrayList<Integer> stack = new ArrayList<>();

    final static int OPTIONS_NUMBER = 8;

    ArrayList<View> viewStack;

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        boolean isFake = true;
        for (int i = 0; i < real.length; i++) {
            if (((TextView) v).getText().equals(real[i])) {
                isFake = false;
                break;
            }
        }
        if (isFake) {
            v.setBackgroundColor(getResources().getColor(R.color.colorFalse));
            viewStack.add(v);
        } else {
            v.setBackgroundColor(getResources().getColor(R.color.colorTrue));
            viewStack.add(v);
            if (++realChecked == real.length) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (View view : viewStack) {
                            view.setBackgroundColor(getResources().getColor(basicBG));
                        }

                        loadLine(false, curLineNum, 0);
                        ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                    }
                }, 500);

            }

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_test);
        wlName = getIntent().getStringExtra("Name");
        Thread loadData = new Thread(new Runnable() {
            @Override
            public void run() {
                data = MainActivity.database.nodeDao().getNodes(wlName);
            }
        });
        try {
            loadData.start();
            loadData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        viewStack = new ArrayList<>();

        listView = findViewById(R.id.prims);
        listView.setOnItemClickListener(this);
        shuffled = new String[OPTIONS_NUMBER];
        loadLine(false, -1, 0);
        listView.setAdapter(new ArrayAdapter<>(this, R.layout.test_line, shuffled));

    }

    private void loadLine(boolean isFake, int prevNum, int num) {
        try {
            Random r = new Random();
            int lineNum = r.nextInt(data.size() - 1);
            if (!isFake) {
                if (!stack.contains(lineNum)) {
                    real = data.get(lineNum).getPrimText().split(",");
                    realChecked = 0;
                    curLineNum = lineNum;
                    if (stack.size() == data.size()) {
                        stack.remove(0);
                    }
                    stack.add(curLineNum);
                    translation = data.get(lineNum).getTransText();
                    ((TextView) findViewById(R.id.transTV)).setText(translation);
                    fake = new String[OPTIONS_NUMBER - real.length];
                    for (int i = 0; i < fake.length; i++) {
                        loadLine(true, lineNum, i);
                    }
                    shuffle();
                } else {
                    loadLine(false, prevNum, num);
                    return;
                }
            } else if (lineNum != prevNum) {
                String[] buf = data.get(lineNum).getPrimText().split(",");
                if (buf.length > 1) {
                    fake[num] = buf[r.nextInt(buf.length - 1)];
                } else {
                    fake[num] = buf[0];
                }
            } else {
                loadLine(true, curLineNum, num);
            }
        } catch (Exception e) {
            Toast.makeText(this, "You must have at least 8 different English words for test", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void shuffle() {
        for (int i = 0; i < OPTIONS_NUMBER; i++) {
            shuffled[i] = null;
        }

        Random r = new Random();
        for (int j = 0; j < real.length; j++) {
            int num = r.nextInt(OPTIONS_NUMBER);
            while (shuffled[num] != null) {
                num = r.nextInt(OPTIONS_NUMBER);
            }
            shuffled[num] = real[j];
        }
        for (int j = 0; j < fake.length; j++) {
            int num = r.nextInt(OPTIONS_NUMBER);
            while (shuffled[num] != null) {
                num = r.nextInt(OPTIONS_NUMBER);
            }
            shuffled[num] = fake[j];
        }

    }

    @Override
    protected void onDestroy() {
        listView.setAdapter(null);
        super.onDestroy();
    }
}
