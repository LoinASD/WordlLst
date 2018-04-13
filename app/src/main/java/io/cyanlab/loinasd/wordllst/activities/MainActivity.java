package io.cyanlab.loinasd.wordllst.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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

public class MainActivity extends AppCompatActivity
        implements  ShowFragment.onListSelectedListener{

    static final int SHOW_WL = 1, SHOW_TEST = 2, SHOW_LINES = 4;

    static final String MODE_LISTS = "Lists";
    static final String MODE_LINES = "Lines";


    static final int REQUEST_CODE_FM = 0;
    static final int REQUEST_CODE_ADD = 3;
    static final int REQUEST_CODE_CHANGE = 5;
    static final int REQUEST_CODE_CHANGE_WL = 4;

    public static String WL_NAME = "wlName";


    static final int HANDLE_MESSAGE_PARSED = 1;
    public static final int HANDLE_MESSAGE_EXTRACTED = 2;
    public static final int HANDLE_MESSAGE_NOT_EXTRACTED = 4;
    static final int HANDLE_MESSAGE_DELETED = 5;
    public static final int HANDLE_MESSAGE_EXISTS = 6;


    Thread parser, extractor;

    public android.support.v4.app.Fragment lists;
    public android.support.v4.app.Fragment lines;
    public LinearLayout progBarLayout, testBar;

    public static StaticHandler h;

    public static String LIST_NAME;

    public static LocalDatabase database;

    public View toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);

        h = new StaticHandler(this);
        setContentView(R.layout.activity_main);



        Bundle data = new Bundle();
        data.putInt("MODE", SHOW_WL);
        lists = new ShowFragment();
        lists.setArguments(data);

        Bundle dataLines = new Bundle();
        dataLines.putInt("MODE", SHOW_LINES);
        lines = new ShowFragment();
        lines.setArguments(dataLines);


        progBarLayout = findViewById(R.id.PB);
        progBarLayout.setVisibility(View.INVISIBLE);

        Thread loadDB = new Thread(new Runnable() {
            @Override
            public void run() {
                if (database == null)
                    database = Room.databaseBuilder(getApplicationContext(), LocalDatabase.class, "base").build();
            }
        });

        loadDB.run();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment, lists, MODE_LISTS).commit();

    }


    @Override
    public void onBackPressed() {
        if (lists.isHidden()) {

            if (((ShowFragment) lines).bsManager.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                hideLines();
            }else
                ((ShowFragment) lines).bsManager.closeBottomSheet();

        }else {
                super.onBackPressed();
        }
    }

    /*@SuppressWarnings("StatementWithEmptyBody")
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

            *//*case R.id.nav_settings:
                break;*//*

            case R.id.nav_wl_show:
                if (getSupportFragmentManager().findFragmentByTag(MODE_LINES)!=null){
                    hideLines();
                } else
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
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.add_list:{

                Intent fileManager = new Intent(this, FileManagerActivity.class);
                startActivityForResult(fileManager, REQUEST_CODE_FM);
                setResult(RESULT_OK, fileManager);

                break;

            }
        }

        return super.onOptionsItemSelected(item);
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

                if (data != null && data.getStringExtra("Action").equals("Delete")) {
                    LIST_NAME = null;
                    ((ShowFragment) lists).notifyAdapter();
                    ((ShowFragment) lines).notifyAdapter();
                    loadLists();
                } else if (data != null && data.getStringExtra("Action").equals("Change Name")){
                    LIST_NAME = data.getStringExtra("New name");
                    ((ShowFragment) lines).adapterLoadData();
                    ((ShowFragment) lists).notifyAdapter();
                    ((ShowFragment) lines).changeHeader();

                }
            }
        }

        if (requestCode == REQUEST_CODE_ADD && resultCode == RESULT_OK) {
            addList(data.getStringExtra("Name"));
            ((ShowFragment) lists).notifyAdapter();
            LIST_NAME = data.getStringExtra("Name");
            loadLines();
        }
        if (requestCode == REQUEST_CODE_CHANGE) {
            ((ShowFragment) lines).adapterLoadData();
        }
    }

    public void deleteList(String wlName){
        Intent changeList = new Intent(this, ChangingWLActivity.class).
                putExtra("Name", wlName).
                putExtra("Action", "Change list");
        startActivityForResult(changeList, REQUEST_CODE_CHANGE_WL);
    }

    public void addList(String wlName){
        final WordList list = new WordList();
        list.setWlName(wlName);
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

    private void hideLines(){
        final RecyclerView main = lines.getView().findViewById(R.id.scrollView);
        int duration = 250;

        ObjectAnimator animator = ObjectAnimator.ofFloat(main, View.ALPHA, 0f).setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                loadLists();
                lines.getView().findViewById(R.id.stats_holder).animate().alpha(1f).setDuration(100);
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

        ((AppBarLayout)lines.getView().findViewById(R.id.appbar)).setExpanded(false, true);
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

    @Override
    public void onListSelected(String name, View view) {

        LIST_NAME = name;
        final RecyclerView main = lists.getView().findViewById(R.id.scrollView);

        int delay = main.getChildCount() * 100;

        final int duration = 300;


        main.animate().alpha(0f).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                toolbar.animate().translationY(-100).setDuration(duration).start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                loadLines();
                toolbar.animate().translationY(0).setDuration(100).start();
                main.animate().alpha(1f).setDuration(100).setListener(null).start();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).setStartDelay(delay).start();


        //ObjectAnimator.ofFloat(view,View.SCALE_X,0f,1f).setDuration(700).start();








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

                ((ShowFragment) activity.lines).adapterLoadData();


                activity.findViewById(R.id.fragment).setVisibility(View.VISIBLE);
                activity.progBarLayout.setVisibility(View.INVISIBLE);


            }
        }
    }
}
