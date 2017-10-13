package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.pdf.PDFParser;
import io.cyanlab.loinasd.wordllst.controller.pdf.TextExtractor;
import io.cyanlab.loinasd.wordllst.model.Facade;
import io.cyanlab.loinasd.wordllst.view.*;
import io.cyanlab.loinasd.wordllst.controller.*;

public class MainActivity extends AppCompatActivity {


    static final int REQUEST_CODE_FM = 1;
    static final int HANDLE_MESSAGE_PARSED = 1;
    LayoutInflater wlInflater;
    WLView wlView;
    Facade facade;
    LinearLayout scroll;
    ScrollView scrollView;
    DBHelper dbHelper;
    SQLiteDatabase database;
    ProgressBar pb;
    Handler h;
    Thread parser, extractor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------//
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        facade = Facade.getFacade();
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        wlView = new WLView(this);
        scroll = (LinearLayout)findViewById(R.id.scroll);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(ProgressBar.INVISIBLE);
        h = new StaticHandler(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,9);
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

        //-----------Other------------------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        });*/
        //------------------------------//

        //-------MY----------------//
        wlInflater = getLayoutInflater();
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //TODO restore InstanceState
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO save InstanceState
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.addWL) {
            Intent fileManager = new Intent(this, FileManagerActivity.class);
            startActivityForResult(fileManager, REQUEST_CODE_FM);
            setResult(RESULT_OK, fileManager);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        if (requestCode == REQUEST_CODE_FM) {
            if (resultCode == RESULT_OK) {
                pb.setVisibility(ProgressBar.VISIBLE);
                final String file = data.getStringExtra("file");
                System.out.println(file);
                parser = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startParser(file);
                    }
                });
                parser.start();
            }
        }
    }
    
    public void startParser(String file){

        ByteArrayOutputStream oS = new ByteArrayOutputStream();
        int parsed = PDFParser.parsePdf(file, oS);
        final ByteArrayInputStream iS = new ByteArrayInputStream(oS.toByteArray());

        extractor = new Thread(new Runnable() {
            @Override
            public void run() {
                TextExtractor.getExtractor().extract(iS);
            }
        });
        extractor.start();

        if (parsed == 1) {
            Message m = new Message();
            //m.setD;
            h.sendEmptyMessage(HANDLE_MESSAGE_PARSED);
            /*Snackbar.make(wlView, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/

        }
    }

    @Override
    protected void onDestroy() {
        if (h != null)
            h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void updateLine(){
        //TODO: this method for update lines in real time
        pb.setVisibility(ProgressBar.GONE);
        scroll.removeAllViews();
        for(int i = 0; i<facade.getWordlistsNum();i++){
            WLView.getWordlistAsButton(i,this,wlView,wlInflater,scroll);
        }
    }

    static class StaticHandler extends Handler {
        WeakReference<MainActivity> wrActivity;

        public StaticHandler(MainActivity activity) {
            wrActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = wrActivity.get();
            if (activity == null) return;
            if (msg.what == HANDLE_MESSAGE_PARSED)
                activity.updateLine();
        }
    }
}
