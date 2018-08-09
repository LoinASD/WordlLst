package io.cyanlab.loinasd.wordllst.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
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

    boolean[] viewStack;

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        boolean isFake = true;
        for (String aReal : real) {
            if (((TextView) v).getText().equals(aReal)) {
                isFake = false;
                break;
            }
        }
        if (isFake) {
            v.setBackgroundColor(getResources().getColor(R.color.colorFalse));
            viewStack[position] = true;
        } else {
            v.setBackgroundColor(getResources().getColor(R.color.colorTrue));
            viewStack[position] = true;
            if (++realChecked == real.length) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < OPTIONS_NUMBER; i++) {
                            viewStack[i] = false;
                        }

                        loadLine(false, curLineNum, 0);
                        ((LinesAdapter) listView.getAdapter()).notifyDataSetChanged();
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
                //data = MainActivity.database.nodeDao().getNodes(name);
            }
        });
        try {
            loadData.start();
            loadData.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        viewStack = new boolean[OPTIONS_NUMBER];

        listView = findViewById(R.id.prims);
        listView.setOnItemClickListener(this);
        shuffled = new String[OPTIONS_NUMBER];
        loadLine(false, -1, 0);
        listView.setAdapter(new LinesAdapter(this, R.layout.test_line, shuffled));

    }

    private void loadLine(boolean isFake, int prevNum, int num) {
        try {
            Random r = new Random();
            int lineNum = r.nextInt(data.size() - 1);
            if (!isFake) {
                if (!stack.contains(lineNum)) {
                    real = data.get(lineNum).primText.split(",");
                    realChecked = 0;
                    curLineNum = lineNum;
                    if (stack.size() == data.size()) {
                        stack.remove(0);
                    }
                    stack.add(curLineNum);
                    translation = data.get(lineNum).transText;
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
                String[] buf = data.get(lineNum).primText.split(",");
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

    private class LinesAdapter extends ArrayAdapter{


        public LinesAdapter(@NonNull Context context, int resource, @NonNull Object[] objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View v = convertView == null ? View.inflate(getContext(), R.layout.test_line, null) : convertView;

            boolean isFake = true;
            for (String aReal : real) {
                if (shuffled[position].equals(aReal)) {
                    isFake = false;
                    break;
                }
            }

            v.setBackgroundColor(getResources().getColor(viewStack[position] ? (!isFake ? R.color.colorTrue : R.color.colorFalse) : basicBG));

            return super.getView(position, convertView, parent);
        }
    }

    @Override
    protected void onDestroy() {
        listView.setAdapter(null);
        super.onDestroy();
    }
}
