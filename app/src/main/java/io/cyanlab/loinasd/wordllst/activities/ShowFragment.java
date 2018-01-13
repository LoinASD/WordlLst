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
    private static int lastNum;

    onListSelectedListener listener;

    SimpleCursorAdapter cursorAdapter;
    ListView main;
    static DBHelper dbHelper;
    private MyCallBack callBack;

    private int MODE;

    @Override
    public void setArguments(Bundle args) {

        MODE = args.getInt("MODE");

        super.setArguments(args);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.content_nav,null);
        if (main == null) {
            main = (ListView)v.findViewById(R.id.scrollView);
        }

        if (callBack == null) {
            callBack = new MyCallBack();
        }
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


        setAdapter(R.layout.simple_line);
        if (getActivity().getLoaderManager().getLoader(1) == null) {
            (getActivity()).getLoaderManager().initLoader(1, null, callBack);
        }
        getActivity().getLoaderManager().getLoader(1).forceLoad();

        main.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int lineId = Integer.parseInt(((Cursor) main.getItemAtPosition(position))
                        .getString(((Cursor) main.getItemAtPosition(position)).getColumnIndex("_id")));
                Intent changeLine = new Intent(getContext(), ChangingWLActivity.class);
                changeLine.putExtra("ID", lineId);
                changeLine.putExtra("Name", LIST_NAME);
                changeLine.putExtra("Action", "Change");
                startActivityForResult(changeLine, REQUEST_CODE_CHANGE);
                getActivity().setResult(getActivity().RESULT_OK, changeLine);

                return false;
            }
        });
    }

    public void loadLists(){


        setAdapter(R.layout.lists_line);
        if (getActivity().getLoaderManager().getLoader(0) == null) {
            getActivity().getLoaderManager().initLoader(0, null, callBack);
        }
        getActivity().getLoaderManager().getLoader(0).forceLoad();

        main.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String wlName = ((TextView)view.findViewById(R.id.name_line)).getText().toString();
                listener.onListSelected(wlName, view);
            }
        });
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden){
            main.scheduleLayoutAnimation();
            switch (MODE){
                case SHOW_WL:
                    loadLists();
                    break;

                case SHOW_TEST:
                    showTest();
                    break;
                case SHOW_LINES:
                    load();
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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (MODE == SHOW_WL) {
            try {
                listener = (onListSelectedListener) getActivity();
            }catch (ClassCastException e){
                e.printStackTrace();
            }
        }

        dbHelper = ((NavActivity)getActivity()).dbHelper;

    }

    @Override
    public void onResume() {
        super.onResume();


        switch (MODE){
            case SHOW_WL:
                loadLists();
                break;

            case SHOW_TEST:
                showTest();
                break;
            case SHOW_LINES:
                ((NavActivity)getActivity()).showFabTab();
                load();
                break;

            default: break;
        }
        if (MODE == SHOW_WL) {

        }
        main.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (MODE == SHOW_LINES) {

                    getActivity().getLoaderManager().getLoader(1).forceLoad();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

    }


    //-----Adapter-----//

    void setAdapter(int layout) {

        if (cursorAdapter == null) {
            switch (layout){

                case R.layout.simple_line: {
                    String[] from = {"_id", PRIM_COLUMN_NAME, TRANS_COLUMN_NAME};
                    int[] to = {R.id.idPlace, R.id.primeTV, R.id.translateTV};
                    cursorAdapter = new SimpleCursorAdapter(getActivity(), layout, null, from, to, 0);
                    break;
                }

                case R.layout.lists_line: {
                    String[] from = {"wlId"};
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


}
