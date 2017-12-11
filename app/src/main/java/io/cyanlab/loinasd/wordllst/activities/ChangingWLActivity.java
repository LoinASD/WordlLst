package io.cyanlab.loinasd.wordllst.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;

public class ChangingWLActivity extends AppCompatActivity implements View.OnClickListener {

    int lineID;
    String wlName;
    boolean isAdding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (getIntent().getStringExtra("Action")) {
            case ("Change"): {
                setContentView(io.cyanlab.loinasd.wordllst.R.layout.activity_changing_line);

                isAdding = false;
                lineID = getIntent().getIntExtra("ID", -1);
                wlName = getIntent().getStringExtra("Name");

                Cursor cursor = DBHelper.getDBHelper(this).getRow(wlName, lineID);
                cursor.moveToFirst();
                ((EditText) findViewById(R.id.primET)).setText(cursor.getString(cursor.getColumnIndex("prim")));
                ((EditText) findViewById(R.id.transET)).setText(cursor.getString(cursor.getColumnIndex("trans")));
                findViewById(R.id.primET).refreshDrawableState();
                findViewById(R.id.transET).refreshDrawableState();
                findViewById(R.id.saveBut).setOnClickListener(this);
                findViewById(R.id.delLineBut).setOnClickListener(this);
                findViewById(R.id.clearBut).setOnClickListener(this);
                findViewById(R.id.addBut).setOnClickListener(this);
                break;
            }
            case ("Delete"): {
                setContentView(R.layout.activity_delete);
                wlName = getIntent().getStringExtra("Name");
                ((TextView) findViewById(R.id.reqestDel)).setText("Delete \n" + wlName + "?");
                findViewById(R.id.delBut).setOnClickListener(this);
                findViewById(R.id.cancelBut).setOnClickListener(this);
                break;

            }
            case ("Add"): {
                setContentView(R.layout.activity_add_wl);
                findViewById(R.id.saveBut).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_OK,
                                new Intent().putExtra("Name", ((EditText) findViewById(R.id.namePlace)).getText().toString()));
                        finish();
                    }
                });
                findViewById(R.id.cancelBut).setOnClickListener(this);
                break;
            }
        }

        //-----------------------------------------//


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.saveBut): {
                try {
                    if (!isAdding) {
                        DBHelper.getDBHelper(this).saveWLRow(wlName, lineID,
                                ((EditText) findViewById(R.id.primET)).getText().toString(),
                                ((EditText) findViewById(R.id.transET)).getText().toString());
                    } else {
                        Cursor cursor = DBHelper.getDBHelper(this).getRow(wlName, lineID);
                        cursor.moveToFirst();
                        int order = cursor.getInt(cursor.getColumnIndex("position"));
                        DBHelper.getDBHelper(this).saveNewWLRow(wlName, order,
                                ((EditText) findViewById(R.id.primET)).getText().toString(),
                                ((EditText) findViewById(R.id.transET)).getText().toString());
                    }
                    setResult(RESULT_OK,
                            new Intent().putExtra("Name", wlName));
                } catch (SQLiteException e) {
                    return;
                }
                break;
            }
            case (R.id.addBut): {
                if (!isAdding) {
                    isAdding = true;
                    ((ImageButton) findViewById(R.id.delLineBut)).setImageResource(android.R.drawable.arrow_down_float);
                    ((ImageButton) findViewById(R.id.saveBut)).setImageResource(android.R.drawable.arrow_up_float);
                    ((ImageButton) findViewById(R.id.addBut)).setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                } else {
                    isAdding = false;
                    ((ImageButton) findViewById(R.id.delLineBut)).setImageResource(android.R.drawable.ic_menu_delete);
                    ((ImageButton) findViewById(R.id.saveBut)).setImageResource(android.R.drawable.ic_menu_save);
                    ((ImageButton) findViewById(R.id.addBut)).setImageResource(android.R.drawable.ic_menu_add);
                }
                return;
            }
            case (R.id.delBut): {
                setResult(RESULT_OK,
                        new Intent().putExtra("Name", wlName));
                break;
            }
            case (R.id.delLineBut): {
                if (!isAdding) {
                    DBHelper.getDBHelper(this).deleteLine(wlName, lineID);
                } else {
                    Cursor cursor = DBHelper.getDBHelper(this).getRow(wlName, lineID);
                    cursor.moveToFirst();
                    int order = cursor.getInt(cursor.getColumnIndex("position")) + 1;
                    DBHelper.getDBHelper(this).saveNewWLRow(wlName, order,
                            ((EditText) findViewById(R.id.primET)).getText().toString(),
                            ((EditText) findViewById(R.id.transET)).getText().toString());
                }
                setResult(RESULT_OK,
                        new Intent().putExtra("Name", wlName));
                break;
            }
            case (R.id.clearBut): {
                ((EditText) findViewById(R.id.primET)).setText("");
                ((EditText) findViewById(R.id.transET)).setText("");
                return;
            }
            case (R.id.cancelBut): {
                setResult(RESULT_CANCELED);
                break;
            }

        }
        finish();
    }
}
