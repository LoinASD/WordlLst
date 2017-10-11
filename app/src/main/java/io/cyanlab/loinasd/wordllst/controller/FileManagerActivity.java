package io.cyanlab.loinasd.wordllst.controller;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.zip.Inflater;

import io.cyanlab.loinasd.wordllst.R;

public class FileManagerActivity extends AppCompatActivity{

    private static File dir;
    private static String[] files;
    private static ArrayAdapter<String> adapter;
    private static String CURRENT_PATH = "/sdcard/storage";
    private static String ROOT_PATH =
            Environment.getExternalStorageDirectory().getPath();
    LinearLayout wayLayout;
    ListView lw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        //----------------------------------//
        wayLayout = (LinearLayout) findViewById(R.id.wayLayout);
        lw = (ListView) findViewById(R.id.treeListView);
        lw.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dir = new File(ROOT_PATH);
        //-----------------------------------//
        showDir(dir);
    }



    private void showDir(File dir) {
        files = dir.list();
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, files);

        lw.setAdapter(adapter);
    }

}
