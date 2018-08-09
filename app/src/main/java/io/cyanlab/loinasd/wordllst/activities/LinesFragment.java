package io.cyanlab.loinasd.wordllst.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.LinesHolder;
import io.cyanlab.loinasd.wordllst.controller.ListEditor;
import io.cyanlab.loinasd.wordllst.controller.database.DataProvider;
import io.cyanlab.loinasd.wordllst.controller.pdf.ListEditorHolder;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;
import io.cyanlab.loinasd.wordllst.view.BottomSheetManager;

public class LinesFragment extends Fragment implements LinesHolder, ListEditor.Listener, ListEditorHolder {

    private static String KEY_NAME = "Name";

    public static LinesFragment getLinesFragment(String name){

        Bundle data = new Bundle();

        data.putString(KEY_NAME, name);

        LinesFragment fragment = new LinesFragment();

        fragment.setArguments(data);

        return fragment;
    }

    private List<Node> nodes;

    public WordList list;

    private RecyclerView recycler;

    private ListEditor editor;

    private LinesAdapter adapter;

    private Listener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lines1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (nodes == null)
            return;

        View view = getView();

        if (view == null)
            return;

        recycler = view.findViewById(R.id.recycler_lines);

        if (recycler == null)
            return;

        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        editor = new BottomSheetManager(this, view.findViewById(R.id.bottom_sheet));

        adapter = new LinesAdapter(nodes, editor);
        recycler.setAdapter(adapter);

    }

    @Override
    public void onStop() {
        super.onStop();

        if (recycler == null || recycler.getAdapter() == null)
            return;

        recycler.setAdapter(null);

        recycler = null;
    }

    @Override
    public void updateLines() {

        if (adapter == null || nodes == null)
            return;

        getActivity().runOnUiThread(() -> {

            adapter.updateNodes(nodes);
        });
    }

    @Override
    public void updateLine(Node node) {

        adapter.notifyItemChanged(nodes.indexOf(node));
    }

    @Override
    public void loadLines(WordList list) {

        if (!DataProvider.isBaseLoaded)
            return;

        Thread load = new Thread(() -> nodes = DataProvider.getNodes(list));

        load.start();

        try {
            load.join();

            updateLines();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setListener(Listener listener) {

        this.listener = listener;
    }

    @Override
    public ListEditor getEditor() {

        return editor;
    }

    @Override
    public void changeLine(Node node) {

        DataProvider.updateNode(node);

        int position = nodes.indexOf(node);

        adapter.notifyItemChanged(position);
    }

    @Override
    public void removeLine(Node node) {

        DataProvider.deleteNode(node);

        int position = nodes.indexOf(node);

        nodes.remove(node);

        adapter.notifyItemRemoved(position);
    }

    @Override
    public void copyLine(Node node) {

        Toast.makeText(getContext(), "Line copied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void pasteLine(Node node) {

        Toast.makeText(getContext(), "Line pasted", Toast.LENGTH_SHORT).show();

        changeLine(node);
    }

    @Override
    public void cutLine(Node node) {

        removeLine(node);

        Toast.makeText(getContext(), "Line cut", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void changeList(WordList list, String newName) {

        DataProvider.updateList(list, newName);

        listener.onListChanged(list);
    }

    @Override
    public void deleteList(WordList list) {

        DataProvider.deleteList(list);

        listener.onListRemoved(list);

        nodes.clear();
    }
}



class NodeHolder extends RecyclerView.ViewHolder {

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

class LinesAdapter extends RecyclerView.Adapter<NodeHolder>{

    private List<Node> nodes;

    ListEditor editor;

    public void updateNodes(List<Node> newNodes){

        nodes = newNodes;

        notifyDataSetChanged();
    }

    public LinesAdapter(List<Node> nodes, ListEditor editor){

        this.nodes = nodes;
        this.editor = editor;
    }

    @Override
    public NodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_line, parent, false);

        return new NodeHolder(view);
    }

    @Override
    public void onBindViewHolder(NodeHolder holder, int position) {

        final Node item = nodes.get(position);

        holder.primTV.setText(item.primText);

        holder.transTV.setText(item.transText);

        holder.itemView.setOnLongClickListener(p0 -> {

            editor.editLine(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }
}
