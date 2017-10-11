package io.cyanlab.loinasd.wordllst.controller;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;

import io.cyanlab.loinasd.wordllst.R;

public class FileManagerActivity extends AppCompatActivity implements View.OnClickListener{

    private static File dir;
    Logger logger = Logger.getLogger("FM");
    private static ArrayList<Map<String, Object>> data;
    private static String[] files;
    private static int img;
    private static ArrayAdapter<String> adapter;
    private static  SimpleAdapter sa;
    private static final String ATTRIBUTE_NAME_TEXT = "text";
    private static final String ATTRIBUTE_NAME_IMAGE = "image";
    private static String CURRENT_PATH = "/sdcard/storage";
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
        wayLayout = (LinearLayout) findViewById(R.id.wayLayout);
        way = (TextView) findViewById(R.id.wayTextView);
        lw = (ListView) findViewById(R.id.treeListView);
        lw.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        CURRENT_PATH = ROOT_PATH;
        dir = new File(CURRENT_PATH);
        //-----------------------------------//
        showDir(dir);

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

    private void showDir(File dir) {
        way.setText(dir.getName());
        files = dir.list();
        data = new ArrayList<>(files.length);
        Map<String, Object> m;
        File f;
        for (String file : files) {
            f = new File(dir, file);
            if (f.isDirectory()) img = R.drawable.folder;
            else img = R.drawable.pdf;

            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_TEXT, file);
            m.put(ATTRIBUTE_NAME_IMAGE, img);
            data.add(m);
        }
        String[] from = { ATTRIBUTE_NAME_TEXT, ATTRIBUTE_NAME_IMAGE };
        int[] to = { R.id.fileTextView, R.id.fileImageView };
        sa = new SimpleAdapter(this, data, R.layout.file_line, from, to);
        lw.setAdapter(sa);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.wayTextView) {
            File f = new File(CURRENT_PATH);
            CURRENT_PATH = f.getParent();
            showDir(f.getParentFile());
        }
    }
}
