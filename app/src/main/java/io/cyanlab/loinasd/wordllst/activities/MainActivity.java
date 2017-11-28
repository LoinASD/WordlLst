package io.cyanlab.loinasd.wordllst.activities;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import io.cyanlab.loinasd.wordllst.controller.*;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {


    static final int REQUEST_CODE_FM = 1;
    static final int HANDLE_MESSAGE_PARSED = 1;
    static final int HANDLE_MESSAGE_EXTRACTED = 2;
    static final int HANDLE_MESSAGE_NOT_EXTRACTED = 4;
    static final String PRIM_COLUMN_NAME = "prim";
    static final String TRANS_COLUMN_NAME = "trans";


    ListView wlView;
    LinearLayout scroll;
    DBHelper dbHelper;
    SQLiteDatabase database;
    ProgressBar pb;
    public static StaticHandler h;
    Thread parser, extractor;
    SimpleCursorAdapter cursorAdapter;
    MyCursorLoader loader;
    boolean isBeingChanged;
    boolean isDeletable;
    boolean isChangable;
    boolean isAddable;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------//
        wlView = (ListView) findViewById(R.id.scrollView);
        scroll = (LinearLayout)findViewById(R.id.scroll);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(ProgressBar.INVISIBLE);


        h = new StaticHandler(this);

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        setAdapter(R.layout.simple_line);
        isBeingChanged = false;
        isDeletable = false;
        isChangable = false;
        isAddable = true;

        getWLsAsButtons(scroll, dbHelper);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

    }

    //-------Options menu-------//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.addWL) {
            isDeletable = false;
            isChangable = false;
            isAddable = false;
            Intent fileManager = new Intent(this, FileManagerActivity.class);
            startActivityForResult(fileManager, REQUEST_CODE_FM);
            setResult(RESULT_OK, fileManager);
            return true;
        }

        if (id == R.id.clear_database) {
            isDeletable = false;
            isChangable = false;
            isAddable = true;
            dbHelper.clearDB();
            scroll.removeAllViews();
            wlView.setVisibility(View.GONE);
            loader.wlName = null;
        }

        if (id == R.id.changeWL) {
            if ((!isBeingChanged) && (loader != null)) {
                isBeingChanged = !isBeingChanged;
                setAdapter(R.layout.editable_line);
                wlView.setVisibility(View.INVISIBLE);
                pb.setVisibility(ProgressBar.VISIBLE);
                loadWl(loader.wlName);
                pb.setVisibility(ProgressBar.GONE);
                wlView.setVisibility(View.VISIBLE);
                isDeletable = false;
                isAddable = false;
                scroll.setVisibility(View.INVISIBLE);

            } else {
                wlView.setVisibility(View.INVISIBLE);
                pb.setVisibility(ProgressBar.VISIBLE);
                dbHelper.saveWL(loader.wlName, wlView);
                setAdapter(R.layout.simple_line);
                loadWl(loader.wlName);
                pb.setVisibility(ProgressBar.GONE);
                wlView.setVisibility(View.VISIBLE);
                isBeingChanged = !isBeingChanged;
                scroll.setVisibility(View.VISIBLE);
            }
        }

        if (id == R.id.deleteWl) {
            scroll.removeAllViews();
            dbHelper.deleteWL(loader.wlName);
            loader.wlName = null;
            wlView.setVisibility(View.GONE);
            getWLsAsButtons(scroll, dbHelper);
            isDeletable = false;
            isChangable = false;
            isAddable = true;
        }

        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.deleteWl).setVisible(isDeletable);
        menu.findItem(R.id.changeWL).setVisible(isChangable);
        menu.findItem(R.id.addWL).setVisible(isAddable);
        if (isBeingChanged) {
            menu.findItem(R.id.changeWL).setIcon(android.R.drawable.ic_menu_save);
        } else {
            menu.findItem(R.id.changeWL).setIcon(android.R.drawable.ic_menu_edit);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //-------FileManager-------//

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

    //-------Parser & Extractor-------//
    
    public void startParser(final String file){

        final PipedOutputStream pout;
        final PipedInputStream pin;
        try {
            pout = new PipedOutputStream();
            pin = new PipedInputStream(pout);
            parser = new Thread(new Runnable() {
                @Override
                public void run() {
                    new PDFParser().parsePdf(file, pout);
                }
            });

            extractor = new Thread(new Runnable() {
                @Override
                public void run() {
                    new TextExtractor().extract(pin, dbHelper);
                }
            });

            parser.start();
            extractor.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //-------MessageHandler-------//

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
            if (msg.what == HANDLE_MESSAGE_PARSED) parser = true;
            if (msg.what == HANDLE_MESSAGE_EXTRACTED) {
                extractor = true;
                wlName = msg.getData().getString("wlName");
            }
            if (msg.what == HANDLE_MESSAGE_NOT_EXTRACTED) {
                parser = true;
                extractor = true;
            }

            if (parser && extractor) {
                activity.updateLine(wlName);
            }
        }
    }


    //-------Adapter-------//

    void setAdapter(int layout) {
        String[] from;
        int[] to;
        switch (layout) {
            case (R.layout.simple_line): {
                String[] from1 = {PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
                from = from1;
                int[] to1 = {R.id.primeTV, R.id.translateTV};
                to = to1;
                break;
            }
            case (R.layout.editable_line): {
                String[] from1 = {"_id", PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
                from = from1;
                int[] to1 = {R.id.idPlace, R.id.primeTV, R.id.translateTV};
                to = to1;
                break;
            }
            default: {
                String[] from1 = {PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
                from = from1;
                int[] to1 = {R.id.primeTV, R.id.translateTV};
                to = to1;
            }
        }

        cursorAdapter = new SimpleCursorAdapter(this, layout, null, from, to, 0);
        wlView.setAdapter(cursorAdapter);
        wlView.setClickable(false);
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

    //-------Wordlist Load-------//

    private void updateLine(String wlName) {
        //TODO: this method for update lines in real time
        pb.setVisibility(ProgressBar.GONE);
        wlView.setVisibility(View.VISIBLE);
        scroll.removeAllViews();


        if (wlName != null) {
            loadWl(wlName);
        }
        getWLsAsButtons(scroll, dbHelper);
        scroll.setVisibility(View.VISIBLE);
    }

    public void loadWl(String wlName) {

        if (loader == null) {
            loader = new MyCursorLoader(this, wlName, dbHelper);
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            loader.changeWlName(wlName);
        }

        isDeletable = true;
        isChangable = true;
        isAddable = true;
        if (!isBeingChanged) {
            invalidateOptionsMenu();
        }

        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    //-------Wordlists As Buttons-------//

    void getWLsAsButtons(LinearLayout linearLayout, DBHelper dbHelper) {


        final String[] wlNames = dbHelper.loadWlsNames();

        for (int i = 0; i < wlNames.length; i++) {
            Button button = new Button(this);
            final int k = i;
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateLine(wlNames[k]);

                }
            };
            button.setText(wlNames[i]);
            button.setOnClickListener(onClickListener);
            linearLayout.addView(button);
        }
    }

    //-------Activity LiveCircle-------//

    @Override
    protected void onDestroy() {
        dbHelper.close();
        if (h != null)
            h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
