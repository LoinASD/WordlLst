package io.cyanlab.loinasd.wordllst.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

import static io.cyanlab.loinasd.wordllst.activities.NavActivity.LIST_NAME;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.REQUEST_CODE_CHANGE;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_LINES;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_TEST;
import static io.cyanlab.loinasd.wordllst.activities.NavActivity.SHOW_WL;

public class ShowFragment extends android.support.v4.app.Fragment {

    private int STATE;

    public static final int RIGHT_ANSWERS_TO_COMPLETE = 3;
    public static final int HANDLE_MESSAGE_NAME_LOADED = 0;

    public static final int NEEDS_UPD = 2;
    public static final int DONT_NEEDS_UPD = 1;

    public Header header;

    public static FragHandler h;

    onListSelectedListener listener;
    onStateChangedListener stateListener;

    WLAdapter adapter;
    RecyclerView main;

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

        final View v = inflater.inflate(MODE == SHOW_LINES ? R.layout.content_nav_lines : R.layout.content_nav_lists, null);
        main = v.findViewById(R.id.scrollView);
        main.setLayoutManager(new LinearLayoutManager(getActivity()));
        switch (MODE) {

            case SHOW_LINES:

                h = new FragHandler(this);
                header = new Header(v.findViewById(R.id.appbar));
                //main.addHeaderView(header);

                /*StatisticsPercentsBehavior behavior = new StatisticsPercentsBehavior((TextView)(header.findViewById(R.id.percents)), (LinearLayout)(header.findViewById(R.id.name_plus_button)));
                CoordinatorLayout.LayoutParams params = ((CoordinatorLayout.LayoutParams)(header.findViewById(R.id.percents)).getLayoutParams());
                params.setBehavior(behavior);*/

                setAdapter(R.layout.simple_line);

                break;
            case SHOW_WL:

                DrawerLayout drawer = ((NavActivity)getActivity()).findViewById(R.id.drawer_layout);
                Toolbar toolbar = v.findViewById(R.id.toolbar);
                ((NavActivity)getActivity()).toolbar = toolbar;
                ((NavActivity)getActivity()).setSupportActionBar(toolbar);
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        (getActivity()), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
                toggle.syncState();

                setAdapter(R.layout.list_line);

                try {
                    listener = (onListSelectedListener) getActivity();
                    stateListener = (onStateChangedListener) getActivity();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                break;

        }

        return v;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden){
            main.scheduleLayoutAnimation();
            adapter.loadFromDB();
            switch (MODE){
                case SHOW_WL:
                    break;

                case SHOW_TEST:

                    break;
                case SHOW_LINES:

                    main.scrollToPosition(0);
                    ((AppBarLayout)getView().findViewById(R.id.appbar)).setExpanded(false, false);
                    break;

                default: break;
            }

        }
        if (MODE == SHOW_LINES) {
            if (hidden) {
                ((NavActivity) getActivity()).setBarVisibility(View.GONE);

            }else {
                if (((NavActivity)getActivity()).progBarLayout.getVisibility() != View.VISIBLE){
                    ((NavActivity) getActivity()).setBarVisibility(View.VISIBLE);
                }
            }

        }
        STATE = NEEDS_UPD;
    }


    @Override
    public void onResume() {

        if (!isHidden()) {
            //main.scheduleLayoutAnimation();
            adapter.loadFromDB();
            switch (MODE) {
                case SHOW_WL:
                    //getActivity().getLoaderManager().getLoader(0).forceLoad();
                    break;

                case SHOW_TEST:

                    break;
                case SHOW_LINES:
                    STATE = DONT_NEEDS_UPD;
                    break;

                default:
                    break;
            }

        }


        if (MODE == SHOW_LINES) {
            if (isHidden()) {
                ((NavActivity) getActivity()).setBarVisibility(View.GONE);
            } else {
                if (((NavActivity)getActivity()).progBarLayout.getVisibility() != View.VISIBLE){
                    ((NavActivity) getActivity()).setBarVisibility(View.VISIBLE);
                }
            }
        }


        super.onResume();

    }

    void changeHeader() {
        header.listName.setText(LIST_NAME);
        header.listName.refreshDrawableState();
        header.bar.setMax(adapter.list.maxWeight);
        header.bar.setProgress(adapter.list.maxWeight - adapter.list.currentWeight);

        String prog = (adapter.list.maxWeight - adapter.list.currentWeight) * 100 / (adapter.list.maxWeight != 0 ? adapter.list.maxWeight : 1) + "%";
        header.percents.setText(prog);

        String words = "Words: " + adapter.list.maxWeight / RIGHT_ANSWERS_TO_COMPLETE;
        header.statsWords.setText(words);

        String learned = "Learned words: " + (adapter.list.maxWeight - adapter.list.currentWeight) / RIGHT_ANSWERS_TO_COMPLETE;
        header.statsApprLearnedWords.setText(learned);

    }

    private class Header{

        TextView listName;
        TextView percents;
        TextView statsWords;
        TextView statsApprLearnedWords;
        ProgressBar bar;

        private Header(View header){
            listName = header.findViewById(R.id.name_line);
            percents = header.findViewById(R.id.percents);
            statsWords = header.findViewById(R.id.stats_words);
            statsApprLearnedWords = header.findViewById(R.id.stats_appr_learned_words);
            bar = header.findViewById(R.id.progressBar2);

            header.findViewById(R.id.edit_listname).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((NavActivity) getActivity()).deleteList(LIST_NAME);
                }
            });
        }
    }

    /*public void loadProgress() {
        for (int i = 0; i < main.getChildCount(); i++) {
            int progress = ;
            int max = dbHelper.getData(((TextView) main.getChildAt(i).findViewById(R.id.name_line)).getText().toString(), 0).getCount() * RIGHT_ANSWERS_TO_COMPLETE;
            ((ProgressBar) main.getChildAt(i).findViewById(R.id.progressBar2)).setProgress(max - progress + RIGHT_ANSWERS_TO_COMPLETE);
            ((ProgressBar) main.getChildAt(i).findViewById(R.id.progressBar2)).setMax(max);
            ((TextView) main.getChildAt(i).findViewById(R.id.percents)).setText(((max - progress) * 100 / max) + "%");
        }
    }*/


    //-----Adapter-----//

    void setAdapter(final int layout) {

        adapter = new WLAdapter(layout);
        adapter.loadFromDB();

        main.setAdapter(adapter);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (MODE == SHOW_LINES) {
            ((NavActivity) getActivity()).setBarVisibility(View.GONE);

        }

    }


    public interface onListSelectedListener {void onListSelected(String name, View view);}

    //-----State-----//

    public int getState() {
        return STATE;
    }

    public void setState(int state) {
        STATE = state;
    }

    public interface onStateChangedListener {
        void onStateChanged(int newState);
    }


    protected void notifyAdapter() {
        adapter.notifyDataSetChanged();
    }

    protected void adapterLoadData() {
        adapter.loadFromDB();
    }

    private class ListHolder extends RecyclerView.ViewHolder {

        TextView namePlace;

        ProgressBar progressBar;

        TextView progress;

        LinearLayout listLayout;

        ListHolder(View itemView) {
            super(itemView);
            namePlace = itemView.findViewById(R.id.name_line);
            progressBar = itemView.findViewById(R.id.progressBar2);
            progress = itemView.findViewById(R.id.percents);
            listLayout = itemView.findViewById(R.id.list_layout);
        }
    }

    private class NodeHolder extends RecyclerView.ViewHolder {

        TextView primTV;
        TextView transTV;

        LinearLayout lineLayout;

        NodeHolder(View itemView) {
            super(itemView);

            primTV = itemView.findViewById(R.id.primeTV);
            transTV = itemView.findViewById(R.id.translateTV);
            lineLayout = itemView.findViewById(R.id.lineLayout);
        }
    }

    public static class FragHandler extends Handler {

        private WeakReference<ShowFragment> mFragment;

        private FragHandler(ShowFragment fragment) {
            mFragment = new WeakReference<ShowFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            ShowFragment fragment = mFragment.get();

            switch (msg.what) {

                case (HANDLE_MESSAGE_NAME_LOADED): {
                    fragment.changeHeader();
                }
            }


        }
    }

    private class WLAdapter extends RecyclerView.Adapter {

        @LayoutRes
        private int resource;
        WordList list;

        List<WordList> lists;

        List<Node> nodes;

        private WLAdapter(@LayoutRes int resource) {
            this.resource = resource;
        }

        private void colorLines() {
            for (int i = 0; i < main.getChildCount(); i++) {
                if (i % 2 == 0) {
                    main.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.colorAccentLowAlpha));
                } else
                    main.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        }

        void loadFromDB() {

            Thread load = new Thread();

            switch (MODE) {

                case (SHOW_WL): {
                    load = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            lists = NavActivity.database.listDao().getAllLists();
                        }
                    });
                    break;

                }
                case (SHOW_LINES): {
                    load = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            nodes = NavActivity.database.nodeDao().getNodes(LIST_NAME);
                        }
                    });
                    break;
                }

            }
            try {
                if (MODE == SHOW_LINES){
                    Thread loadName = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            list = NavActivity.database.listDao().getWordlist(LIST_NAME);
                        }
                    });
                    loadName.start();
                    loadName.join();
                    changeHeader();
                }
                load.start();
                load.join();
                adapter.notifyDataSetChanged();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);

            RecyclerView.ViewHolder holder;

            switch (resource) {
                case R.layout.list_line: {
                    holder = new ListHolder(view);
                    break;
                }
                case R.layout.simple_line: {
                    holder = new NodeHolder(view);
                    break;
                }
                default:
                    holder = new NodeHolder(view);
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final int id = position;
            switch (MODE) {
                case SHOW_WL: {
                    ((ListHolder) holder).namePlace.setText(lists.get(position).getWlName());
                    ((ListHolder) holder).progressBar.setMax(lists.get(position).maxWeight);
                    ((ListHolder) holder).progressBar.setProgress(lists.get(position).maxWeight - lists.get(position).currentWeight);
                    String prog = (lists.get(position).maxWeight - lists.get(position).currentWeight) * 100 / (lists.get(position).maxWeight != 0 ? lists.get(position).maxWeight : 1) + "%";
                    ((ListHolder) holder).progress.setText(prog);


                    ((ListHolder) holder).listLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String wlName = (lists.get(id)).getWlName();
                            listener.onListSelected(wlName, view);
                        }
                    });

                    break;
                }
                case SHOW_LINES: {
                    ((NodeHolder) holder).primTV.setText(nodes.get(position).getPrimText());
                    ((NodeHolder) holder).transTV.setText(nodes.get(position).getTransText());
                    ((NodeHolder) holder).lineLayout.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            Intent changeLine = new Intent(getContext(), ChangingWLActivity.class).
                                    putExtra("Node", nodes.get(id)).
                                    putExtra("Action", "Change");
                            startActivityForResult(changeLine, REQUEST_CODE_CHANGE);

                            setState(NEEDS_UPD);

                            return true;
                        }
                    });

                    break;
                }
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return ((MODE == SHOW_WL && lists != null || MODE == SHOW_LINES && nodes != null) ? (MODE == SHOW_WL ? lists.size() : nodes.size()) : 0);
        }

    }

}
