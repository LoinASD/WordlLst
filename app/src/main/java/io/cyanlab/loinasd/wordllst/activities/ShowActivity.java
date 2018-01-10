package io.cyanlab.loinasd.wordllst.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;

import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_LISTS;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_TEST;

import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_WL;

public class ShowActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String PRIM_COLUMN_NAME = "prim";
    private static final String TRANS_COLUMN_NAME = "trans";

    android.widget.SimpleCursorAdapter cursorAdapter;
    MyCursorLoader loader;
    ListView main;
    DBHelper dbHelper;

    private int MODE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        main = (ListView) findViewById(R.id.show_list);
        dbHelper = DBHelper.getDBHelper(this);

        setAdapter(R.layout.wordlist_name_line);
        MODE = getIntent().getIntExtra("action", 0);

        if (MODE == 0) showNothing();

        switch (MODE){
            case SHOW_WL:
                loadLists();
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

    //-----Loading List------//

    public void loadWl(String wlName) {

        setAdapter(R.layout.simple_line);
        if (loader == null) {
            loader = new MyCursorLoader(this, wlName, dbHelper);
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            loader.changeWlName(wlName);
            loader.setWordlist(true);
        }

        getSupportLoaderManager().getLoader(0).forceLoad();

        main.setOnItemClickListener(null);
    }

    public void loadLists(){
        setAdapter(R.layout.wordlist_name_line);
        if (loader == null) {
            loader = new MyCursorLoader(this, "", dbHelper);
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            loader.setWordlist(false);
        }

        getSupportLoaderManager().getLoader(0).forceLoad();

        main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String wlName = new String(((TextView)((LinearLayout)view).findViewById(R.id.name_place)).getText().toString());
                loadWl(wlName);
            }
        });
    }


    //-----Adapter-----//

    void setAdapter(int layout) {

        switch (layout){

            case R.layout.simple_line: {
                String[] from = {"_id", PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
                int[] to = {R.id.idPlace, R.id.primeTV, R.id.translateTV};
                cursorAdapter = new SimpleCursorAdapter(this, layout, null, from, to, 0);
                break;
            }

            case R.layout.wordlist_name_line: {
                String[] from = {"wlId"};
                int[] to = {R.id.name_place};
                cursorAdapter = new SimpleCursorAdapter(this, layout, null, from, to, 0);
                break;
            }
        }
        main.setAdapter(cursorAdapter);


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

        boolean isWordlist;
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

        public void setWordlist(boolean isWordlist){
            this.isWordlist = isWordlist;
        }

        @Override
        public Cursor loadInBackground() {

            if (isWordlist){
                return dbHelper.getData(wlName);
            }else{
                Cursor data = dbHelper.getLists();
                return data;
            }

        }
    }

    @Override
    protected void onDestroy() {

        dbHelper.close();

        super.onDestroy();
    }
}
