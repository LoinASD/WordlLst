package io.cyanlab.loinasd.wordllst.activities;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;

/**
 * Created by Анатолий on 11.12.2017.
 */

public class DnDTestActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    String wlName;
    String[] real;
    String[] fake;
    String[] shuffled;
    String translation;
    Cursor data;
    ListView listView;
    int realChecked;
    int curLineNum;
    int basicBG = R.color.colorButtons;
    ArrayList<Integer> stack = new ArrayList<>();

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
        } else {
            v.setBackgroundColor(getResources().getColor(R.color.colorTrue));
            if (++realChecked == real.length) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < listView.getChildCount(); i++) {
                            listView.getChildAt(i).setBackgroundColor(getResources().getColor(basicBG));
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
        setContentView(R.layout.activity_drag_n_drop_test);
        wlName = getIntent().getStringExtra("Name");
        data = new DBHelper(this).getData(wlName, 0);
        listView = ((ListView) findViewById(R.id.prims));
        listView.setOnItemClickListener(this);
        shuffled = new String[8];
        loadLine(false, -1, 0);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.test_line, shuffled));

    }

    private void loadLine(boolean isFake, int prevNum, int num) {
        try {
            Random r = new Random();
            int lineNum = r.nextInt(data.getCount() - 1);
            data.moveToFirst();
            int id = -1;
            int idIndex = data.getColumnIndex("_id");
            int positionIndex = data.getColumnIndex("position");
            do {
                if (lineNum == data.getInt(positionIndex)) {
                    id = data.getInt(idIndex);
                    break;
                }
            } while (data.moveToNext());
            if ((id != -1)) {
                if (!isFake) {
                    if (!stack.contains(id)) {
                        real = data.getString(data.getColumnIndex("prim")).split(",");
                        realChecked = 0;
                        curLineNum = id;
                        if (stack.size() == data.getCount()) {
                            stack.remove(0);
                        }
                        stack.add(curLineNum);
                        translation = data.getString(data.getColumnIndex("trans"));
                        ((TextView) findViewById(R.id.transTV)).setText(translation);
                        fake = new String[8 - real.length];
                        for (int i = 0; i < fake.length; i++) {
                            loadLine(true, id, i);
                        }
                        shuffle();
                    } else {
                        loadLine(false, prevNum, num);
                        return;
                    }
                } else if (id != prevNum) {
                    String[] buf = data.getString(data.getColumnIndex("prim")).split(",");
                    if (buf.length > 1) {
                        fake[num] = buf[r.nextInt(buf.length - 1)];
                    } else {
                        fake[num] = buf[0];
                    }
                } else {
                    loadLine(true, curLineNum, num);
                }
            } else {
                loadLine(false, curLineNum, 0);
            }
        } catch (Exception e) {
            Toast.makeText(this, "You must have at least 8 different English words for test", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void shuffle() {
        for (int i = 0; i < 8; i++) {
            shuffled[i] = null;
        }

        Random r = new Random();
        for (int j = 0; j < real.length; j++) {
            int num = r.nextInt(8);
            while (shuffled[num] != null) {
                num = r.nextInt(8);
            }
            shuffled[num] = real[j];
        }
        for (int j = 0; j < fake.length; j++) {
            int num = r.nextInt(8);
            while (shuffled[num] != null) {
                num = r.nextInt(8);
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
