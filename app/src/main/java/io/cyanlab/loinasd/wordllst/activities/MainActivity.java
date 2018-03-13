package io.cyanlab.loinasd.wordllst.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;
import io.cyanlab.loinasd.wordllst.controller.pdf.Delegator;
import io.cyanlab.loinasd.wordllst.controller.pdf.PDFParser;

public class MainActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {


    static final int REQUEST_CODE_FM = 1;
    static final int REQUEST_CODE_CHANGE = 2;
    static final int REQUEST_CODE_DELETEWL = 3;

    static final String PRIM_COLUMN_NAME = "prim";
    static final String TRANS_COLUMN_NAME = "trans";

    public static final boolean DEBUG_MODE = true;


    ListView wlView;
    LinearLayout scroll;
    DBHelper dbHelper;
    SQLiteDatabase database;
    ProgressBar pbx;
    LinearLayout pb;
    TextView pbText;
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
//        pb.setVisibility(ProgressBar.INVISIBLE);
        pbText = (TextView) findViewById(R.id.pbText);
        inflater = getLayoutInflater();


        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        setAdapter(R.layout.simple_line);

        isDeletable = false;
        isAddable = true;


        //getWLsAsButtons(scroll, dbHelper);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        registerForContextMenu(wlView);

        wlView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                int lineId = Integer.parseInt(((Cursor) wlView.getItemAtPosition(position))
                        .getString(((Cursor) wlView.getItemAtPosition(position)).getColumnIndex("_id")));
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

       /* if (id == R.id.clear_database) {
            isDeletable = false;
            isAddable = true;
            dbHelper.clearDB();
           // getWLsAsButtons(scroll, dbHelper);
            wlView.setVisibility(View.GONE);
            loader.wlName = null;
        }

        if (id == R.id.begin_dnd_test) {
            Intent testWl = new Intent(getBaseContext(), DnDTestActivity.class);
            testWl.putExtra("Name", loader.wlName);
            startActivity(testWl);
        }
        if (id == R.id.begin_card_test) {
            Intent testWl = new Intent(getBaseContext(), CardTestActivity.class);
            testWl.putExtra("Name", loader.wlName);
            startActivity(testWl);
        }
*/
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.deleteWl).setVisible(isDeletable);
        //menu.findItem(R.id.begin_dnd_test).setVisible(isDeletable);
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
               // getWLsAsButtons(scroll, dbHelper);
                loadWl(data.getStringExtra("Name"));
            }
        }
        if (requestCode == REQUEST_CODE_CHANGE) {
            if (resultCode == RESULT_OK) {
                loadWl(data.getStringExtra("Name"));
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
                    long timeStart = System.currentTimeMillis();
                    new PDFParser().parsePdf(file, pout);
                    long timeStop = System.currentTimeMillis();

                    System.out.printf("Parser works %d ms", timeStop - timeStart);
                }
            });

            extractor = new Thread(new Runnable() {
                @Override
                public void run() {
                    long timeStart = System.currentTimeMillis();
                    new Delegator().extract(pin);
                    long timeStop = System.currentTimeMillis();

                    System.out.printf("Parser works %d ms", timeStop - timeStart);
                }
            });

            parser.start();
            extractor.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //-------MessageHandler-------//




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
            return dbHelper.getData(wlName, 0);
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
        //getWLsAsButtons(scroll, dbHelper);
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


    //-------Activity LiveCycle-------//

    @Override
    protected void onDestroy() {
        dbHelper.close();

        super.onDestroy();
    }
}
