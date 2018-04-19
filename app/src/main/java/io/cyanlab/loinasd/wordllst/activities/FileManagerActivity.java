package io.cyanlab.loinasd.wordllst.activities;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.pdf.Node;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class FileManagerActivity extends AppCompatActivity implements View.OnClickListener{

    Logger logger = Logger.getLogger("FM");
    private String[] files;
    private static final String ATTRIBUTE_NAME_TEXT = "text";
    private static final String ATTRIBUTE_NAME_IMAGE = "image";
    private static String CURRENT_PATH;// = "/sdcard/storage/0/Download";
    private static String ROOT_PATH =
            Environment.getExternalStorageDirectory().getPath();
    LinearLayout wayLayout;
    ListView lw;
    TextView way;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        //----------------------------------//
        wayLayout = findViewById(R.id.wayLayout);
        way = findViewById(R.id.wayTextView);
        lw = findViewById(R.id.treeListView);
        lw.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD-card is not responding", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        CURRENT_PATH = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(CURRENT_PATH);
        //-----------------------------------//
        showDir(dir);
        ImageButton ib = this.findViewById(R.id.backButton);
        View.OnClickListener backBut = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CURRENT_PATH.equals(ROOT_PATH)) {
                    File f = new File(CURRENT_PATH);
                    CURRENT_PATH = f.getParent();
                    logger.log(Level.INFO, CURRENT_PATH);
                    showDir(f.getParentFile());
                }
            }
        };
        ib.setOnClickListener(backBut);
        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                File f = new File(CURRENT_PATH, files[position]);
                CURRENT_PATH = f.getAbsolutePath();
                logger.log(Level.INFO, CURRENT_PATH);
                if (f.isDirectory()) showDir(f.getAbsoluteFile());
                else {
                    Intent intent = new Intent();
                    intent.putExtra("file", f.getAbsolutePath());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void showDir(final File dir) {
        way.setText(dir.getName());
        files = dir.list();
        if (files != null) {
            Arrays.sort(files,new SortedByName());
            ArrayList<String> usefulFiles = new ArrayList<>();
            ArrayList<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> m;
            File f;
            for (String file : files) {
                f = new File(dir, file);
                int img;
                if (f.isDirectory()){
                    if (!file.startsWith(".")) {
                        img = R.drawable.folder;
                        usefulFiles.add(file);
                    }else continue;
                }else {
                    if (file.endsWith(".pdf")){
                        img = R.drawable.pdf;
                        usefulFiles.add(file);
                    }else continue;
                }
                m = new HashMap<>();
                m.put(ATTRIBUTE_NAME_TEXT, file);
                m.put(ATTRIBUTE_NAME_IMAGE, img);
                data.add(m);
            }
            usefulFiles.toArray(files);
            String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE };
            int[] to = { R.id.fileTextView, R.id.fileImageView };
            SimpleAdapter sa = new SimpleAdapter(this, data, R.layout.file_line, from, to);
            lw.setAdapter(sa);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wayTextView) {
            File f = new File(CURRENT_PATH);
            CURRENT_PATH = f.getParent();
            showDir(f.getParentFile());
        }
    }

    //-------Sorts the list-------

    class SortedByName implements Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            String str1 = (String) o1;
            String str2 = (String) o2;
            str1 = str1.toUpperCase();
            str2 = str2.toUpperCase();
            return str1.compareTo(str2);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line_context_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.addNewWL){
            Thread addList = new Thread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.database.beginTransaction();

                    WordList list = new WordList();

                    list.setWlName("New List");
                    list.currentWeight = ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;
                    list.maxWeight = ShowFragment.RIGHT_ANSWERS_TO_COMPLETE;

                    Node node = new Node();
                    node.setPrimText("kek");
                    node.setTransText("mek");
                    node.setWlName("New List");
                    node.setWeight(ShowFragment.RIGHT_ANSWERS_TO_COMPLETE);

                    MainActivity.database.listDao().insertList(list);
                    MainActivity.database.nodeDao().insertNode(node);

                    MainActivity.database.setTransactionSuccessful();
                    MainActivity.database.endTransaction();
                }
            });
            addList.start();
            try {
                addList.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
