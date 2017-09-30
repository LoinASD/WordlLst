package io.cyanlab.loinasd.wordllst.controller;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.model.Facade;
import io.cyanlab.loinasd.wordllst.view.*;
import io.cyanlab.loinasd.wordllst.controller.*;

public class MainActivity extends Activity {


    LayoutInflater wlInflater;
    WLView wlView;
    Facade facade;
    LinearLayout scroll;
    Button button;
    ScrollView scrollView;
    DBHelper dbHelper;
    SQLiteDatabase database;
    ToggleButton toggleButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------//
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        facade = Facade.getFacade();
        toggleButton = (ToggleButton)findViewById(R.id.toggleButton);
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        wlView = new WLView(this);
        scroll = (LinearLayout)findViewById(R.id.scroll);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,9);
        wlView.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(wlView, lp);
        wlInflater = getLayoutInflater();
        final Activity act = this;
        final View.OnClickListener change = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wlView.changeWlView();
            }
        };
        //----------------------------------------------//

        View.OnClickListener setEditable = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View id = wlView.getChildAt(0).findViewById(R.id.primeTV);
                if(id != null) {
                    switch (wlView.getChildAt(0).findViewById(R.id.primeTV).getLayoutParams().height) {
                        case (LinearLayout.LayoutParams.MATCH_PARENT): {
                            break;
                        }
                        case (LinearLayout.LayoutParams.WRAP_CONTENT): {
                            wlView.changeWlView();
                            break;
                        }
                    }
                    wlView.removeAllViews();
                    wlView.getEditableWordlist(wlInflater);
                    wlView.setOnClickListener(null);
                } else{
                    dbHelper.saveWl(wlView.getWordlistName(),database,wlView);
                    wlView.removeAllViews();
                    wlView.getWordlistAsList(facade.getWordlistNumByName(wlView.getWordlistName()),wlInflater);
                    wlView.setOnClickListener(change);
                }
            }
        };


        toggleButton.setOnClickListener(setEditable);
        wlView.setOnClickListener(change);

        for(int i = 0; i<facade.getWordlistsNum();i++){
            WLView.getWordlistAsButton(i,this,wlView,wlInflater,scroll);
        }



    }


/*
        //-----------Other------------------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
//
//            }
//        });
//
//        //-------------------------------//
//
//        //-------MY----------------//
//        wlInflater = getLayoutInflater();
//        ll = (LinearLayout) findViewById(R.id.ll);
//
//        //----------------------------------//
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        if (id == R.id.addWL) {
//            createNewWordList();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//    }
//   */
//    }
//



}
