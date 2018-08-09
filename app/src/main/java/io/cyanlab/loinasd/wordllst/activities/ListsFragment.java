package io.cyanlab.loinasd.wordllst.activities;

import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.database.DataProvider;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;
import io.cyanlab.loinasd.wordllst.controller.*;

public class ListsFragment extends Fragment implements ListsHolder {

    private List<WordList> lists = null;

    private RecyclerView recycler;

    private Listener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadLists();
    }

    private void loadLists(){

        if (!DataProvider.isBaseLoaded)
            return;

        Thread load = new Thread(() -> {

            lists = DataProvider.getLists();
        });

        load.start();

        try {
            load.join();

            if (recycler == null || recycler.getAdapter() == null)
                return;

            recycler.getAdapter().notifyDataSetChanged();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lists1, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (lists == null)
            return;

        View view = getView();

        if (view == null)
            return;

        recycler = view.findViewById(R.id.recycler_lists);

        if (recycler == null)
            return;

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(new ListsAdapter(this));
    }

    @Override
    public void onStop() {
        super.onStop();

        if (recycler == null || recycler.getAdapter() == null)
            return;

        recycler.setAdapter(null);
    }

    @Override
    public void updateList(WordList list) {

        int position = lists.indexOf(list);

        System.out.println(position);

        recycler.getAdapter().notifyItemChanged(position);
    }

    @Override
    public void updateLists() {

        loadLists();
    }

    @Override
    @AnyThread
    public void addList(WordList list) {

        getActivity().runOnUiThread(() -> {

            int position = lists.size();

            lists.add(list);

            recycler.getAdapter().notifyItemRemoved(position);
        });
    }

    @Override
    public void removeList(WordList list) {

        int position = lists.indexOf(list);

        lists.remove(list);

        recycler.getAdapter().notifyItemRemoved(position);
    }

    @Override
    public void setListener(Listener listener) {

        this.listener = listener;
    }

    @Override
    public Listener getListener() {
        return listener;
    }

    @Override
    public List<WordList> getLists() {
        return lists;
    }
}

class ListHolder extends RecyclerView.ViewHolder {

    TextView namePlace;

    ProgressBar progressBar;

    TextView progress;

    ListHolder(View itemView) {
        super(itemView);
        namePlace = itemView.findViewById(R.id.name_line);
        progressBar = itemView.findViewById(R.id.progressBar2);
        progress = itemView.findViewById(R.id.percents);
    }
}

class ListsAdapter extends RecyclerView.Adapter<ListHolder>{

    private ListsHolder listsHolder;

    private WordList selectedList;

    ListsAdapter(ListsHolder holder){

        this.listsHolder = holder;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_line, parent, false);

        return new ListHolder(view);
    }

    @Override
    public void onBindViewHolder(ListHolder holder, int position) {

        final WordList item = listsHolder.getLists().get(position);

        holder.namePlace.setText(item.name);

        holder.itemView.setOnClickListener(view ->{

            listsHolder.getListener().onListSelected(item);

            selectedList = item;
        });
    }

    @Override
    public int getItemCount() {
        return listsHolder.getLists().size();
    }
}
