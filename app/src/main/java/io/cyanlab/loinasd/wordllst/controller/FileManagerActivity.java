package io.cyanlab.loinasd.wordllst.controller;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.zip.Inflater;

import io.cyanlab.loinasd.wordllst.R;

public class FileManagerActivity extends AppCompatActivity {

    private static File dir;
    public static String CURRENT_PATH = "/sdcard/storage";
    public static String ROOT_PATH =
            Environment.getExternalStorageDirectory().getPath();
    LinearLayout treeLayout, wayLayout;
    LayoutInflater fileInflater;
    ScrollView scrollFileView;
    TextView ftv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);
        //----------------------------------//
        treeLayout = (LinearLayout) findViewById(R.id.treeLayout);
        wayLayout = (LinearLayout) findViewById(R.id.wayLayout);
        scrollFileView = (ScrollView) findViewById(R.id.scrollFileView);
        dir = new File(CURRENT_PATH);
        fileInflater = getLayoutInflater();
        //-----------------------------------//
        showDir(dir);
    }

    private void showDir(File dir) {
        for (File f: dir.listFiles()) {
            View v = fileInflater.inflate(R.layout.file_line, treeLayout);
            ftv = (TextView) v.findViewById(R.id.fileTextView);

            ftv.setText(f.getName());
        }
    }


}
