package io.cyanlab.loinasd.wordllst.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ListFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.ref.WeakReference;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.database.LocalDatabase;
import io.cyanlab.loinasd.wordllst.controller.pdf.Delegator;
import io.cyanlab.loinasd.wordllst.controller.pdf.PDFParser;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

import static io.cyanlab.loinasd.wordllst.activities.MainActivity.REQUEST_CODE_FM;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ShowFragment.onListSelectedListener{

    static final int SHOW_WL = 1, SHOW_TEST = 2, SHOW_LINES = 4;

    static final String MODE_LISTS = "Lists";
    static final String MODE_LINES = "Lines";

    public static String WL_NAME = "wlName";

    static final int REQUEST_CODE_ADD = 3;
    static final int HANDLE_MESSAGE_PARSED = 1;
    public static final int HANDLE_MESSAGE_EXTRACTED = 2;
    public static final int HANDLE_MESSAGE_NOT_EXTRACTED = 4;
    static final int HANDLE_MESSAGE_DELETED = 5;
    public static final int HANDLE_MESSAGE_EXISTS = 6;
    static final int REQUEST_CODE_CHANGE = 5;
    static final int REQUEST_CODE_CHANGE_WL = 4;

    Thread parser, extractor;
    android.support.v4.app.Fragment lists;
    android.support.v4.app.Fragment lines;
    LinearLayout progBarLayout, testBar;
    public static StaticHandler h;

    private boolean isDeletable;

    private boolean isFabExpanded;

    public static String LIST_NAME;

    public static LocalDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        h = new StaticHandler(this);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Bundle data = new Bundle();
        data.putInt("MODE", SHOW_WL);
        lists = new ShowFragment();
        lists.setArguments(data);

        Bundle dataLines = new Bundle();
        dataLines.putInt("MODE", SHOW_LINES);
        lines = new ShowFragment();
        lines.setArguments(dataLines);



        //fab_tab = findViewById(R.id.fab_tab);
        progBarLayout = findViewById(R.id.PB);
        progBarLayout.setVisibility(View.INVISIBLE);
        final AppCompatActivity activity = this;

        /*final FloatingActionButton fab = findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation scaleAnimation1;
                Animation scaleAnimation2;
                *//*Animation mainAnimation = AnimationUtils.loadAnimation(activity,R.anim.fab_main_hide);
                view.startAnimation(mainAnimation);*//*
                int vis;
                int img;
                View fab1;
                View fab2;
                View sideFab = fab_tab.findViewById(R.id.fab_add_line);
                if (!isFabExpanded){

                    scaleAnimation1 = AnimationUtils.loadAnimation(activity,R.anim.fab_show);
                    scaleAnimation2 = AnimationUtils.loadAnimation(activity,R.anim.fab_show);
                    fab2 = fab_tab.findViewById(R.id.fab_other_test);
                    fab1 = fab_tab.findViewById(R.id.fab_card_test);
                    vis = View.VISIBLE;
                    img = android.R.drawable.ic_menu_close_clear_cancel;
                    sideFab.startAnimation(scaleAnimation1);

                } else {
                    scaleAnimation1 = AnimationUtils.loadAnimation(activity,R.anim.fab_hide);
                    scaleAnimation2 = AnimationUtils.loadAnimation(activity,R.anim.fab_hide);
                    fab2 = fab_tab.findViewById(R.id.fab_card_test);
                    fab1 = fab_tab.findViewById(R.id.fab_other_test);
                    vis = View.INVISIBLE;
                    img = R.drawable.ic_tests_24dp;
                    sideFab.startAnimation(scaleAnimation2);
                }
                sideFab.setVisibility(vis);
                fab1.startAnimation(scaleAnimation1);
                fab1.setVisibility(vis);
                scaleAnimation2.setStartOffset(100);
                fab2.startAnimation(scaleAnimation2);
                fab2.setVisibility(vis);
                fab.setImageResource(img);
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
        fab_tab.findViewById(R.id.fab_add_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addLine = new Intent(getBaseContext(), ChangingWLActivity.class);
                addLine.putExtra("Name", LIST_NAME);
                addLine.putExtra("Action", "AddLine");
                startActivityForResult(addLine, REQUEST_CODE_CHANGE);
            }
        });*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Thread loadDB = new Thread(new Runnable() {
            @Override
            public void run() {
                if (database == null)
                    database = Room.databaseBuilder(getApplicationContext(), LocalDatabase.class, "base").build();
            }
        });

        loadDB.run();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_wl_show);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment, lists, MODE_LISTS).commit();


        testBar = findViewById(R.id.bbar_include);
        //-----------testBar------------------------
        //testBar = findViewById(R.id.bottom_bar);
        View.OnClickListener barListenner = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println(view.getId());
                switch (view.getId()) {
                    case R.id.cardTest: {
                        Intent testWl = new Intent(getBaseContext(), CardTestActivity.class);
                        testWl.putExtra("Name", LIST_NAME);
                        startActivity(testWl);
                        break;
                    }
                    case R.id.addLineButton: {
                        Intent addLine = new Intent(getBaseContext(), ChangingWLActivity.class);
                        addLine.putExtra("Name", LIST_NAME);
                        addLine.putExtra("Action", "AddLine");
                        startActivityForResult(addLine, REQUEST_CODE_CHANGE);
                        break;
                    }
                    case R.id.dndTest: {
                        Intent testWl = new Intent(getBaseContext(), DnDTestActivity.class);
                        testWl.putExtra("Name", LIST_NAME);
                        startActivity(testWl);
                        break;
                    }
                }
            }
        };
        testBar.findViewById(R.id.cardTest).setOnClickListener(barListenner);
        testBar.findViewById(R.id.dndTest).setOnClickListener(barListenner);
        testBar.findViewById(R.id.addLineButton).setOnClickListener(barListenner);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (lists.isHidden()) {

            final ListView main = lines.getView().findViewById(R.id.scrollView);
            int duration = 500;

            ObjectAnimator animator = ObjectAnimator.ofFloat(main, View.ALPHA, 0f).setDuration(duration);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    loadLists();
                    main.animate().alpha(1).setDuration(100).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();

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
        if (id == R.id.item_about) {
            return true;
        }
        if (id == R.id.deleteWl) {
            Intent delWL = new Intent(this, ChangingWLActivity.class);
            delWL.putExtra("Action", "Delete");
            delWL.putExtra("Name", LIST_NAME);
            startActivityForResult(delWL, REQUEST_CODE_CHANGE_WL);
            setResult(RESULT_OK, delWL);
            ((ShowFragment) lists).setState(ShowFragment.NEEDS_UPD);
            // getWLsAsButtons(scroll, dbHelper);
        }

        if (id == R.id.clear_db) {


            loadLists();
            isDeletable = false;

            ((ShowFragment) lines).setState(ShowFragment.NEEDS_UPD);
            ((ShowFragment) lists).setState(ShowFragment.NEEDS_UPD);
        }
        invalidateOptionsMenu();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.deleteWl).setVisible(isDeletable);
        return super.onPrepareOptionsMenu(menu);
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

            case  R.id.nav_create:

            case R.id.addNewWL:
                Intent addWL = new Intent(this, ChangingWLActivity.class);
                addWL.putExtra("Action", "Add");
                startActivityForResult(addWL, REQUEST_CODE_ADD);
                setResult(RESULT_OK, addWL);
                break;

            case R.id.nav_settings:
                break;

            case R.id.nav_wl_show:
                loadLists();
                break;

            case R.id.nav_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                break;

            default: break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE_FM) {
            if (resultCode == RESULT_OK) {
                final String file = data.getStringExtra("file");
                startParser(file);
                progBarLayout.setVisibility(View.VISIBLE);
                ((TextView) progBarLayout.findViewById(R.id.pbText)).setText("Parsing...");
                findViewById(R.id.fragment).setVisibility(View.INVISIBLE);
            }
        }
        if (requestCode == REQUEST_CODE_CHANGE_WL) {
            if (resultCode == RESULT_OK) {

                if (data.getStringExtra("Action").equals("Delete")) {
                    Thread deleteWL = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NavActivity.database.nodeDao().deleteNodes(LIST_NAME);
                            NavActivity.database.listDao().deleteList(LIST_NAME);
                            h.sendEmptyMessage(HANDLE_MESSAGE_DELETED);
                        }
                    });
                    deleteWL.start();
                    try {


                        deleteWL.join();
                        LIST_NAME = null;
                        ((ShowFragment) lists).notifyAdapter();
                        ((ShowFragment) lines).notifyAdapter();
                        loadLists();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    loadLists();
                } else {
                    ((ShowFragment) lists).adapterLoadData();
                    ((ShowFragment) lines).changeHeader();
                }
            }
        }

        if (requestCode == REQUEST_CODE_ADD && resultCode == RESULT_OK) {
            final WordList list = new WordList();
            list.setWlName(data.getStringExtra("Name"));
            list.maxWeight = 0;
            list.currentWeight = 0;
            Thread addList = new Thread(new Runnable() {
                @Override
                public void run() {
                    database.listDao().insertList(list);
                }
            });
            try {
                addList.start();
                addList.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ((ShowFragment) lists).notifyAdapter();
        }
        if (requestCode == REQUEST_CODE_CHANGE) {
            ((ShowFragment) lines).adapterLoadData();
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
                    new Delegator().extract(pin);
                }
            });
            parser.setPriority(Thread.MAX_PRIORITY);
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

        isDeletable = false;
        invalidateOptionsMenu();

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
            transaction.commitAllowingStateLoss();


        }



    }

    public void loadLines(){

        isDeletable = true;
        invalidateOptionsMenu();


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

    public void setBarVisibility(final int visibility) {

        if (visibility == View.VISIBLE) {
            testBar.setVisibility(View.VISIBLE);
            testBar.setScaleY(0);
            testBar.setTranslationY(100);
        }
        testBar.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                testBar.setVisibility(visibility);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).scaleY(visibility == View.VISIBLE ? 1 : 0).translationY(visibility == View.VISIBLE ? 0 : 100).setDuration(200).setStartDelay(visibility == View.VISIBLE ? 0 : 100).start();


    }

    @Override
    public void onListSelected(String name, View view) {

        LIST_NAME = name;
        final ListView main = lists.getView().findViewById(R.id.scrollView);

        int i = 0;
        int pos = main.getChildCount();
        int diff = 0;
        int delay = main.getChildCount() * 125;

        int duration = 500;
        while (i != pos){
            if (main.getChildAt(i) != view) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(main.getChildAt(i),View.ALPHA,1f,0f);
                animator.setStartDelay((i - diff) * 125);
                animator.setDuration(250);
                animator.start();
            }else {
                diff = 1;
            }
            i++;
        }



        ObjectAnimator animator = ObjectAnimator.ofFloat(main, View.ALPHA, 0f).setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                loadLines();
                main.animate().alpha(1f).setDuration(100).start();
                for (int i = 0; i < main.getChildCount(); i++) {
                    main.getChildAt(i).setAlpha(1f);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setStartDelay(delay);
        animator.start();


        //ObjectAnimator.ofFloat(view,View.SCALE_X,0f,1f).setDuration(700).start();








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
                wlName = msg.getData().getString(WL_NAME);


            }
            if (msg.what == HANDLE_MESSAGE_EXISTS) {
                parser = false;
                extractor = false;

                Toast.makeText(activity, "Wordlist with equal name already exists", Toast.LENGTH_SHORT).show();

                activity.findViewById(R.id.fragment).setVisibility(View.VISIBLE);
                activity.progBarLayout.setVisibility(View.INVISIBLE);
            }

            if (msg.what == HANDLE_MESSAGE_NOT_EXTRACTED) {
                parser = false;
                extractor = false;

                Toast.makeText(activity, "No dictionary found", Toast.LENGTH_SHORT).show();

                activity.findViewById(R.id.fragment).setVisibility(View.VISIBLE);
                activity.progBarLayout.setVisibility(View.INVISIBLE);
            }

            if (parser && extractor) {

                parser = false;
                extractor = false;

                LIST_NAME = wlName;

                Toast.makeText(activity, "Wordlist " + LIST_NAME + " successfully extracted", Toast.LENGTH_LONG).show();
                activity.loadLines();


                activity.findViewById(R.id.fragment).setVisibility(View.VISIBLE);
                activity.progBarLayout.setVisibility(View.INVISIBLE);
            }
        }
    }



    /* static final int REQUEST_CODE_FM = 1;
    static final int REQUEST_CODE_CHANGE = 2;
    static final int REQUEST_CODE_CHANGE_WL = 3;
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
            startActivityForResult(deleteWL, REQUEST_CODE_CHANGE_WL);
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
                    new Delegator().extract(pin, dbHelper);
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
