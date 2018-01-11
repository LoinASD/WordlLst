package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.ref.WeakReference;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;
import io.cyanlab.loinasd.wordllst.controller.pdf.PDFParser;
import io.cyanlab.loinasd.wordllst.controller.pdf.TextExtractor;

import static io.cyanlab.loinasd.wordllst.activities.MainActivity.REQUEST_CODE_FM;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ShowFragment.onListSelectedListener{

    static final int SHOW_WL = 1, SHOW_TEST = 2, SHOW_LISTS = 3, SHOW_LINES = 4;

    static final String MODE_LISTS = "Lists";
    static final String MODE_LINES = "Lines";

    static final int HANDLE_MESSAGE_PARSED = 1;
    static final int HANDLE_MESSAGE_EXTRACTED = 2;
    static final int HANDLE_MESSAGE_NOT_EXTRACTED = 4;
    static final int HANDLE_MESSAGE_LOAD_LIST = 3;

    Thread parser, extractor;
    DBHelper dbHelper;
    android.support.v4.app.Fragment lists;
    android.support.v4.app.Fragment lines;
    LinearLayout fab_tab;
    LinearLayout progBut;
    public static StaticHandler h;

    private boolean isFabExpanded;


    public static String LIST_NAME;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        h = new StaticHandler(this);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHelper = new DBHelper(this);


        Bundle data = new Bundle();
        data.putInt("MODE", SHOW_WL);
        lists = new ShowFragment();
        lists.setArguments(data);

        Bundle dataLines = new Bundle();
        dataLines.putInt("MODE", SHOW_LINES);
        lines = new ShowFragment();
        lines.setArguments(dataLines);


        fab_tab = (LinearLayout)findViewById(R.id.fab_tab);
        progBut = (LinearLayout)findViewById(R.id.PB);
        progBut.setVisibility(View.INVISIBLE);
        final AppCompatActivity activity = this;

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation scaleAnimation;
                /*Animation mainAnimation = AnimationUtils.loadAnimation(activity,R.anim.fab_main_hide);
                view.startAnimation(mainAnimation);*/
                if (!isFabExpanded){

                    scaleAnimation = AnimationUtils.loadAnimation(activity,R.anim.fab_show);
                    fab_tab.findViewById(R.id.fab_other_test).setVisibility(View.VISIBLE);
                    fab_tab.findViewById(R.id.fab_card_test).setVisibility(View.VISIBLE);

                }else {
                    scaleAnimation = AnimationUtils.loadAnimation(activity,R.anim.fab_hide);
                    fab_tab.findViewById(R.id.fab_other_test).setVisibility(View.INVISIBLE);
                    fab_tab.findViewById(R.id.fab_card_test).setVisibility(View.INVISIBLE);
                }

                activity.findViewById(R.id.fab_card_test).startAnimation(scaleAnimation);
                scaleAnimation.setStartOffset(100);
                activity.findViewById(R.id.fab_other_test).startAnimation(scaleAnimation);
                isFabExpanded = !isFabExpanded;
            }
        });

        fab_tab.findViewById(R.id.fab_card_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent testWl = new Intent(getBaseContext(), CardTestActivity.class);
                testWl.putExtra("Name", LIST_NAME);
                startActivity(testWl);
            }
        });
        fab_tab.findViewById(R.id.fab_other_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent testWl = new Intent(getBaseContext(), DnDTestActivity.class);
                testWl.putExtra("Name", LIST_NAME);
                startActivity(testWl);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_wl_show);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, lists, MODE_LISTS).commit();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(lists.isHidden()) {
            loadLists();
        }else {
                super.onBackPressed();
        }
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
        if (id == R.id.clear_database) {
            dbHelper.clearDB();
            // getWLsAsButtons(scroll, dbHelper);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        final Activity act = this;
        int id = item.getItemId();

        Intent show;

        switch (id) {
            case R.id.nav_add:
                Intent fileManager = new Intent(act, FileManagerActivity.class);
                startActivityForResult(fileManager, REQUEST_CODE_FM);
                setResult(RESULT_OK, fileManager);
                break;
            case R.id.nav_test:
                show = new Intent(act, ShowFragment.class);
                show.putExtra("action", SHOW_TEST);
                startActivity(show);
                break;

            case  R.id.nav_create:
                break;

            case R.id.nav_settings:
                break;

            case R.id.nav_wl_show:

                loadLists();


                break;

            default: break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FM) {
            if (resultCode == RESULT_OK) {
                final String file = data.getStringExtra("file");
                startParser(file);
                progBut.setVisibility(View.VISIBLE);
                ((TextView)progBut.findViewById(R.id.pbText)).setText("Parsing...");
                findViewById(R.id.fragment).setVisibility(View.INVISIBLE);
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

    @Override
    protected void onDestroy() {

        if (h != null)
            h.removeCallbacksAndMessages(null);

        super.onDestroy();
    }

    public void loadLists(){
        if (getSupportFragmentManager().findFragmentByTag(MODE_LISTS).isHidden()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (getSupportFragmentManager().findFragmentByTag(MODE_LISTS) != null) {
                transaction.show(lists);
            }else {
                transaction.add(R.id.fragment, lists, MODE_LISTS);
            }
            if (getSupportFragmentManager().findFragmentByTag(MODE_LINES)!=null){
                transaction.hide(lines);
            }
            getSupportFragmentManager().popBackStack();
            transaction.commit();
        }
    }

    public void loadLines(){

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getSupportFragmentManager().findFragmentByTag(MODE_LINES) == null) {
            transaction.add(R.id.fragment, lines, MODE_LINES);
        }else
            transaction.show(lines);
        if (getSupportFragmentManager().findFragmentByTag(MODE_LISTS) != null){
            transaction.hide(lists);
        }
        transaction.commit();

    }

    public void showFabTab(){
        fab_tab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onListSelected(String name) {
        LIST_NAME = name;
        loadLines();
    }


    public static class StaticHandler extends Handler {
        WeakReference<NavActivity> wrActivity;
        volatile boolean parser, extractor;
        volatile String wlName = null;
        private StaticHandler(NavActivity activity) {
            wrActivity = new WeakReference<>(activity);
        }


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NavActivity activity = wrActivity.get();
            if (activity == null) return;
            if (msg.what == HANDLE_MESSAGE_PARSED) {
                parser = true;
                ((TextView)activity.findViewById(R.id.pbText)).setText("Extrackting Text...");
            }
            if (msg.what == HANDLE_MESSAGE_EXTRACTED) {
                extractor = true;
                wlName = msg.getData().getString("wlName");

                activity.findViewById(R.id.fragment).setVisibility(View.VISIBLE);
                activity.progBut.setVisibility(View.INVISIBLE);
            }
            if (msg.what == HANDLE_MESSAGE_NOT_EXTRACTED) {
                parser = true;
                extractor = true;
            }

            if (parser && extractor) {
                LIST_NAME = wlName;
                activity.loadLines();
            }
        }
    }



    /* static final int REQUEST_CODE_FM = 1;
    static final int REQUEST_CODE_CHANGE = 2;
    static final int REQUEST_CODE_DELETEWL = 3;
    static final int HANDLE_MESSAGE_PARSED = 1;
    static final int HANDLE_MESSAGE_EXTRACTED = 2;
    static final int HANDLE_MESSAGE_NOT_EXTRACTED = 4;
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
    public static MainActivity.StaticHandler h;
    Thread parser, extractor;
    SimpleCursorAdapter cursorAdapter;
    MainActivity.MyCursorLoader loader;
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

        h = new MainActivity.StaticHandler(this);

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
        if (id == R.id.begin_card_test) {
            Intent testWl = new Intent(getBaseContext(), CardTestActivity.class);
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

   /* private void updateLine(String wlName) {
        //TODO: this method for update lines in real time
        pb.setVisibility(ProgressBar.GONE);
        wlView.setVisibility(View.VISIBLE);
        scroll.removeAllViews();


        if (wlName != null) {
            load(wlName);
        }
        getWLsAsButtons(scroll, dbHelper);
        scroll.setVisibility(View.VISIBLE);
    }

    public void load(String wlName) {

        if (loader == null) {
            loader = new MainActivity.MyCursorLoader(this, wlName, dbHelper);
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

    }

    //-------Activity LiveCycle-------//

    @Override
    protected void onDestroy() {
        dbHelper.close();
        if (h != null)
            h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }*/
}
