package io.cyanlab.loinasd.wordllst;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.cyanlab.loinasd.wordllst.model.Lang;
import io.cyanlab.loinasd.wordllst.model.Line;
import io.cyanlab.loinasd.wordllst.model.Word;
import io.cyanlab.loinasd.wordllst.model.Wordlist;

public class MainActivity extends AppCompatActivity {


    LayoutInflater wlInflater;
    TextView prim, tran;
    LinearLayout ll;
    private static ArrayList<Wordlist> wls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-----------Other------------------
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //-------------------------------//

        //-------MY----------------//
        wlInflater = getLayoutInflater();
        ll = (LinearLayout) findViewById(R.id.ll);

        //----------------------------------//
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.addWL) {
            createNewWordList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createNewWordList() {
        Wordlist wl = new Wordlist("First");
        Word word1 = new Word("foo", Lang.EN);
        Word word2 = new Word("bar", Lang.RU);
        ArrayList<Word> pr = new ArrayList<>();
        pr.add(word1);
        ArrayList<Word> tr = new ArrayList<>();
        tr.add(word2);
        Line line = new Line(pr, tr, new Lang[] {Lang.EN, Lang.RU});
        wl.addLine(line);
        wls.add(wl);

        for (Line l : wl.getlines()) {
            View v = wlInflater.inflate(R.layout.line, ll, false);

            prim = (TextView) v.findViewById(R.id.primeTV);
            tran = (TextView) v.findViewById(R.id.translateTV);

            prim.setText("");
            tran.setText("");

            for (Word w : l.getPrime()) {
                prim.append(w.getWord() + " ");
            }

            for (Word w : l.getTranslate()) {
                tran.append(w.getWord() + " ");
            }

            ll.addView(v);
        }

    }
}
