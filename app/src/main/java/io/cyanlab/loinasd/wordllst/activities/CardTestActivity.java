package io.cyanlab.loinasd.wordllst.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;

/**
 * Created by Анатолий on 25.12.2017.
 */

public class CardTestActivity extends AppCompatActivity implements View.OnClickListener {

    String wlName;
    String primary;
    Cursor data;
    ArrayList<Integer> stack = new ArrayList<>();
    boolean isChecked;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_test);
        wlName = getIntent().getStringExtra("Name");
        data = DBHelper.getDBHelper(this).getData(wlName);
        findViewById(R.id.card).setOnClickListener(this);
        loadLine();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case (R.id.card): {
                if (!isChecked) {
                    ((TextView) findViewById(R.id.card)).setText(primary);
                    isChecked = true;
                } else {
                    loadLine();
                }
            }
        }
    }

    private void loadLine() {
        isChecked = false;
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
            if (!stack.contains(id)) {
                if (stack.size() == data.getCount()) {
                    stack.remove(0);
                }
                stack.add(id);
                primary = data.getString(data.getColumnIndex("prim"));
                ((TextView) findViewById(R.id.transTV)).setText(data.getString(data.getColumnIndex("trans")) + " (" + primary.split(",").length + ")");
                ((TextView) findViewById(R.id.card)).setText("Turn Over");
            } else loadLine();
        } else loadLine();
    }
}
