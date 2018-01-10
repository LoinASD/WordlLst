package io.cyanlab.loinasd.wordllst.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import io.cyanlab.loinasd.wordllst.R;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_TEST;

import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_WL;

public class ShowActivity extends AppCompatActivity{

    ListView main;

    private final int MODE = getIntent().getIntExtra("action", 0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        main = (ListView) findViewById(R.id.show_list);

        if (MODE == 0) showNothing();

        switch (MODE){
            case SHOW_WL:
                showWL();
                break;

            case SHOW_TEST:
                showTest();
                break;

            default: break;
        }
        if (MODE == SHOW_WL) {

        }
    }

    private void showWL() {


        View.OnClickListener wlListenner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
        main.setOnClickListener(wlListenner);

    }

    private void showTest() {
        View.OnClickListener testListenner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
        main.setOnClickListener(testListenner);

    }

    private void showNothing() {

        finish();
    }
}
