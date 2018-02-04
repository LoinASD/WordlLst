package io.cyanlab.loinasd.wordllst.activities;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;

import static io.cyanlab.loinasd.wordllst.activities.NavActivity.LIST_NAME;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_LINES;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_TEST;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.REQUEST_CODE_CHANGE;

import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_WL;

public class ShowFragment extends android.support.v4.app.Fragment {

    private static final String PRIM_COLUMN_NAME = "prim";
    private static final String TRANS_COLUMN_NAME = "trans";
    private int STATE;

    public static final int RIGHT_ANSWERS_TO_COMPLETE = 3;

    public static final int NEEDS_UPD = 2;
    public static final int DONT_NEEDS_UPD = 1;

    onListSelectedListener listener;
    onStateChangedListener stateListener;

    SimpleCursorAdapter cursorAdapter;
    ListView main;
    static DBHelper dbHelper;
    private MyCallBack callBack;

    private int MODE;

    @Override
    public void setArguments(Bundle args) {

        MODE = args.getInt("MODE");

        STATE = NEEDS_UPD;

        super.setArguments(args);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_nav,null);
        main = (ListView) v.findViewById(R.id.scrollView);
        callBack = new MyCallBack();
        switch (MODE) {

            case SHOW_LINES:
                setAdapter(R.layout.simple_line);
                (getActivity()).getLoaderManager().initLoader(1, null, callBack);
                getActivity().getLoaderManager().getLoader(1).forceLoad();

                main.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        int lineId = Integer.parseInt(((Cursor) main.getItemAtPosition(position))
                                .getString(((Cursor) main.getItemAtPosition(position)).getColumnIndex("_id")));
                        Intent changeLine = new Intent(getContext(), ChangingWLActivity.class);
                        changeLine.putExtra("ID", lineId).putExtra("Name", LIST_NAME).putExtra("Action", "Change");
                        startActivityForResult(changeLine, REQUEST_CODE_CHANGE);

                        setState(NEEDS_UPD);

                        return false;
                    }
                });

                main.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState != SCROLL_STATE_IDLE && MODE == SHOW_LINES) {
                            getActivity().getLoaderManager().getLoader(1).forceLoad();
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    }
                });

                break;
            case SHOW_WL:
                setAdapter(R.layout.lists_line);
                (getActivity()).getLoaderManager().initLoader(0, null, callBack);
                getActivity().getLoaderManager().getLoader(0).forceLoad();

                main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String wlName = ((TextView) view.findViewById(R.id.name_line)).getText().toString();
                        listener.onListSelected(wlName, view);
                    }
                });

                main.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        loadProgress();
                    }
                });

                try {
                    listener = (onListSelectedListener) getActivity();
                    stateListener = (onStateChangedListener) getActivity();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }

                break;

        }
        dbHelper = ((NavActivity) getActivity()).dbHelper;

        return v;
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

    }

    //-----Loading List------//

    public void load() {


    }

    public void loadLists(){



    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden){
            main.scheduleLayoutAnimation();
            switch (MODE){
                case SHOW_WL:
                    getActivity().getLoaderManager().getLoader(0).forceLoad();
                    break;

                case SHOW_TEST:
                    showTest();
                    break;
                case SHOW_LINES:
                    getActivity().getLoaderManager().getLoader(1).forceLoad();
                    break;

                default: break;
            }

        }
        if (MODE == SHOW_LINES) {
            if (hidden) {
                getActivity().findViewById(R.id.fab_tab).setVisibility(View.INVISIBLE);
            }else {
                getActivity().findViewById(R.id.fab_tab).setVisibility(View.VISIBLE);
            }
        }
        STATE = NEEDS_UPD;
    }


    @Override
    public void onResume() {

        if (STATE == NEEDS_UPD && !isHidden()) {
            switch (MODE) {
                case SHOW_WL:
                    getActivity().getLoaderManager().getLoader(0).forceLoad();
                    break;

                case SHOW_TEST:
                    showTest();
                    break;
                case SHOW_LINES:
                    ((NavActivity) getActivity()).showFabTab();
                    getActivity().getLoaderManager().getLoader(1).forceLoad();
                    STATE = DONT_NEEDS_UPD;
                    break;

                default:
                    break;
            }

        }


        if (MODE == SHOW_LINES) {
            getActivity().findViewById(R.id.fab_tab).setVisibility(View.VISIBLE);
        }


        super.onResume();

    }

    public void loadProgress() {
        for (int i = 0; i < main.getChildCount(); i++) {
            int progress = dbHelper.countWeight(((TextView) main.getChildAt(i).findViewById(R.id.name_line)).getText().toString());
            int max = dbHelper.getData(((TextView) main.getChildAt(i).findViewById(R.id.name_line)).getText().toString(), 0).getCount() * RIGHT_ANSWERS_TO_COMPLETE;
            ((ProgressBar) main.getChildAt(i).findViewById(R.id.progressBar2)).setProgress(max - progress);
            ((ProgressBar) main.getChildAt(i).findViewById(R.id.progressBar2)).setMax(max);
            ((TextView) main.getChildAt(i).findViewById(R.id.percents)).setText(((max - progress) * 100 / max) + "%");
        }
    }


    //-----Adapter-----//

    void setAdapter(int layout) {

        if (cursorAdapter == null) {
            switch (layout){

                case R.layout.simple_line: {
                    String[] from = {PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
                    int[] to = {R.id.primeTV, R.id.translateTV};
                    cursorAdapter = new SimpleCursorAdapter(getActivity(), layout, null, from, to, 0);
                    break;
                }

                case R.layout.lists_line: {
                    String[] from = {"wlName"};
                    int[] to = {R.id.name_line};
                    cursorAdapter = new SimpleCursorAdapter(getActivity(), layout, null, from, to, 0);
                    break;
                }
            }

        }
        main.setAdapter(cursorAdapter);


    }

    @Override
    public void onPause() {
        super.onPause();
        if (MODE == SHOW_LINES) {
            getActivity().findViewById(R.id.fab_tab).setVisibility(View.INVISIBLE);
        }

    }

    static class MyCursorLoader extends CursorLoader {

        private int MODE;
        private ListView main;

        public MyCursorLoader(Context context, ListView list, int MODE) {
            super(context);
            main = list;
            this.MODE = MODE;
        }

        @Override
        public Cursor loadInBackground() {
            int pos = main.getLastVisiblePosition();

            switch (MODE) {
                case SHOW_LINES: {
                    return dbHelper.getData(LIST_NAME, pos);
                }

                case SHOW_WL: {
                    Cursor data = dbHelper.getLists();
                    return data;
                }
                default:
                    return dbHelper.getData(LIST_NAME, pos);
            }
        }
    }

    private class MyCallBack implements LoaderManager.LoaderCallbacks<Cursor>{

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new MyCursorLoader(getActivity(), main, MODE);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            cursorAdapter.swapCursor(data);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    }


    public interface onListSelectedListener {void onListSelected(String name, View view);}

    //-----State-----//

    public int getState() {
        return STATE;
    }

    public void setState(int state) {
        if (STATE == DONT_NEEDS_UPD) {
            STATE = state;
        }
    }

    public interface onStateChangedListener {
        void onStateChanged(int newState);
    }

}
