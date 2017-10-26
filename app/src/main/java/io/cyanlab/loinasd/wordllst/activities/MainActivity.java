package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.ref.WeakReference;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.pdf.PDFParser;
import io.cyanlab.loinasd.wordllst.controller.pdf.TextExtractor;
import io.cyanlab.loinasd.wordllst.model.Facade;
import io.cyanlab.loinasd.wordllst.view.*;
import io.cyanlab.loinasd.wordllst.controller.*;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {


    static final int REQUEST_CODE_FM = 1;
    static final int HANDLE_MESSAGE_PARSED = 1;
    static final int HANDLE_MESSAGE_EXTRACTED = 2;
    static final String PRIM_COLUMN_NAME = "prim";
    static final String TRANS_COLUMN_NAME = "trans";
    static final int HANDLE_MESSAGE_LOADWL = 3;

    LayoutInflater wlInflater;
    ListView wlView;
    Facade facade;
    LinearLayout scroll;
    //ScrollView scrollView;
    DBHelper dbHelper;
    SQLiteDatabase database;
    ProgressBar pb;
    public static StaticHandler h;
    Thread parser, extractor;
    SimpleCursorAdapter cursorAdapter;
    MyCursorLoader loader;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------//
        wlView = (ListView) findViewById(R.id.scrollView);
        facade = Facade.getFacade();
        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        //wlView = new ListView(this);
        scroll = (LinearLayout)findViewById(R.id.scroll);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(ProgressBar.INVISIBLE);
        ;
        h = new StaticHandler(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,9);
        //scrollView.addView(wlView,lp);

        String[] from = {PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
        int[] to = {R.id.primeTV, R.id.translateTV};

        cursorAdapter = new SimpleCursorAdapter(this, R.layout.simple_line, null, from, to, 0);
        wlView.setAdapter(cursorAdapter);

        WLView.getWLsAsButtons(this, scroll, dbHelper);

        wlInflater = getLayoutInflater();
        final Activity act = this;
        wlInflater = getLayoutInflater();





        /*final View.OnClickListener change = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wlView.changeWlView();
            }
        };*/
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

        if (id == R.id.clear_database) {
            dbHelper.clearDB();
            scroll.removeAllViews();
            wlView.setVisibility(View.GONE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        if (requestCode == REQUEST_CODE_FM) {
            if (resultCode == RESULT_OK) {
                pb.setVisibility(ProgressBar.VISIBLE);
                wlView.setVisibility(View.GONE);
                scroll.setVisibility(View.GONE);
                final String file = data.getStringExtra("file");
                startParser(file);
            }
        }
    }
    
    public void startParser(final String file){

        final PipedOutputStream pout;
        final PipedInputStream pin;
        try {
            pout = new PipedOutputStream();
            pin = new PipedInputStream(pout);
            parser = new Thread(new Runnable() {
                @Override
                public void run() {
                    int parsed = PDFParser.parsePdf(file, pout);
                }
            });

            extractor = new Thread(new Runnable() {
                @Override
                public void run() {
                    int wlNum = TextExtractor.getNewExtractor().extract(pin, dbHelper);
                }
            });

            parser.start();
            extractor.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        /*Snackbar.make(wlView, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*//*

        }*/
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        if (h != null)
            h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void updateLine(String wlName) {
        //TODO: this method for update lines in real time
        pb.setVisibility(ProgressBar.GONE);
        wlView.setVisibility(View.VISIBLE);
        scroll.removeAllViews();
        WLView.getWLsAsButtons(this, scroll, dbHelper);
        scroll.setVisibility(View.VISIBLE);

        if (wlName != null) {
            loadWl(wlName);
        }
    }

    public void loadWl(String wlName) {

        if (loader == null) {
            loader = new MyCursorLoader(this, wlName, dbHelper);
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            loader.changeWlName(wlName);
        }

        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }



    public static class StaticHandler extends Handler {
        WeakReference<MainActivity> wrActivity;
        volatile boolean parser, extractor;
        volatile String wlName = null;
        private StaticHandler(MainActivity activity) {
            wrActivity = new WeakReference<>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = wrActivity.get();
            if (activity == null) return;
            if (msg.what == HANDLE_MESSAGE_LOADWL)
                activity.loadWl(msg.getData().getString("wlName"));
            if (msg.what == HANDLE_MESSAGE_PARSED) parser = true;
            if (msg.what == HANDLE_MESSAGE_EXTRACTED) {
                extractor = true;
                wlName = msg.getData().getString("wlName");
            }
            if (parser && extractor) {
                activity.updateLine(wlName);
            }
        }
    }

    static class MyCursorLoader extends android.support.v4.content.CursorLoader {

        DBHelper dbHelper;
        String wlName;

        public MyCursorLoader(Context context, String wlName, DBHelper dbHelper) {
            super(context);
            this.dbHelper = dbHelper;
            this.wlName = wlName;
        }

        public void changeWlName(String wlName) {
            this.wlName = wlName;
        }

        @Override
        public Cursor loadInBackground() {
            return dbHelper.getData(wlName);
        }
    }
}
