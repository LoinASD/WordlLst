package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
    static final int REQUEST_CODE_CHANGE = 2;
    static final int REQUEST_CODE_DELETEWL = 3;
    static final int HANDLE_MESSAGE_PARSED = 1;
    static final int HANDLE_MESSAGE_EXTRACTED = 2;
    static final int HANDLE_MESSAGE_NOT_EXTRACTED = 4;
    static final String PRIM_COLUMN_NAME = "prim";
    static final String TRANS_COLUMN_NAME = "trans";


    ListView wlView;
    LinearLayout scroll;
    DBHelper dbHelper;
    SQLiteDatabase database;
    ProgressBar pbx;
    LinearLayout pb;
    TextView pbText;
    public static StaticHandler h;
    Thread parser, extractor;
    SimpleCursorAdapter cursorAdapter;
    MyCursorLoader loader;
    boolean isDeletable;
    boolean isAddable;
    LayoutInflater inflater;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //---------------------------------------------//
        wlView = (ListView) findViewById(R.id.scrollView);
        scroll = (LinearLayout)findViewById(R.id.scroll);
        pb = (LinearLayout) findViewById(R.id.PB);
        pb.setVisibility(ProgressBar.INVISIBLE);
        pbText = (TextView) findViewById(R.id.pbText);
        inflater = getLayoutInflater();

        h = new StaticHandler(this);

        dbHelper = DBHelper.getDBHelper(this);
        database = dbHelper.getWritableDatabase();

        setAdapter(R.layout.simple_line);

        isDeletable = false;
        isAddable = true;


        getWLsAsButtons(scroll, dbHelper);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        registerForContextMenu(wlView);

        wlView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                int lineId = Integer.parseInt(((Cursor) wlView.getItemAtPosition(position)).getString(((Cursor) wlView.getItemAtPosition(position)).getColumnIndex("_id")));
                Intent changeLine = new Intent(getBaseContext(), ChangingWLActivity.class);
                changeLine.putExtra("ID", lineId);
                changeLine.putExtra("Name", loader.wlName);
                changeLine.putExtra("Action", "Change");
                startActivityForResult(changeLine, REQUEST_CODE_CHANGE);
                setResult(RESULT_OK, changeLine);

                return false;
            }
        });

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

        if (id == R.id.deleteWl) {

            Intent deleteWL = new Intent(getBaseContext(), ChangingWLActivity.class);
            deleteWL.putExtra("Action", "Delete");
            deleteWL.putExtra("Name", loader.wlName);
            startActivityForResult(deleteWL, REQUEST_CODE_DELETEWL);
            setResult(RESULT_OK, deleteWL);

        }

        if (id == R.id.clear_database) {
            isDeletable = false;
            isAddable = true;
            dbHelper.clearDB();
            getWLsAsButtons(scroll, dbHelper);
            wlView.setVisibility(View.GONE);
            loader.wlName = null;
        }

        if (id == R.id.begin_dnd_test) {
            Intent testWl = new Intent(getBaseContext(), DnDTestActivity.class);
            testWl.putExtra("Name", loader.wlName);
            startActivity(testWl);
        }

        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.deleteWl).setVisible(isDeletable);
        menu.findItem(R.id.begin_dnd_test).setVisible(isDeletable);
        return super.onPrepareOptionsMenu(menu);
    }

    //-------FileManager-------//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FM) {
            if (resultCode == RESULT_OK) {
                pb.setVisibility(ProgressBar.VISIBLE);
                pbText.setText("Parsing PDF...");
                wlView.setVisibility(View.GONE);
                scroll.setVisibility(View.GONE);
                final String file = data.getStringExtra("file");
                startParser(file);
            } else {
                scroll.removeAllViews();
                getWLsAsButtons(scroll, dbHelper);
                loadWl(data.getStringExtra("Name"));
            }
        }
        if (requestCode == REQUEST_CODE_CHANGE) {
            if (resultCode == RESULT_OK) {
                loadWl(data.getStringExtra("Name"));
            }
        }

        if (requestCode == REQUEST_CODE_DELETEWL) {
            if (resultCode == RESULT_OK) {
                isDeletable = false;
                dbHelper.deleteWL(data.getStringExtra("Name"));
                scroll.removeAllViews();
                getWLsAsButtons(scroll, dbHelper);
                loader.wlName = null;
                wlView.setVisibility(View.GONE);
                invalidateOptionsMenu();
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
            if (msg.what == HANDLE_MESSAGE_PARSED) {
                parser = true;
                activity.pbText.setText("Extrackting Text...");
            }
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

        String[] from = {"_id", PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
        int[] to = {R.id.idPlace, R.id.primeTV, R.id.translateTV};
        cursorAdapter = new SimpleCursorAdapter(this, layout, null, from, to, 0);
        wlView.setAdapter(cursorAdapter);


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
        isAddable = true;
        invalidateOptionsMenu();

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
        ImageButton im = new ImageButton(this);
        im.setImageResource(android.R.drawable.ic_menu_add);
        final Activity act = this;
        im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeletable = false;
                isAddable = false;
                Intent fileManager = new Intent(act, FileManagerActivity.class);
                startActivityForResult(fileManager, REQUEST_CODE_FM);
                setResult(RESULT_OK, fileManager);
            }
        });
        linearLayout.addView(im);
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
