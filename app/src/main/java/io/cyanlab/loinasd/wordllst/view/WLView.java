package io.cyanlab.loinasd.wordllst.view;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.app.Activity;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.model.Facade;


public class WLView extends LinearLayout {
    private String wordlistName;

    public String getWordlistName(){
        return wordlistName;
    }

    public WLView(Context context) {
        super(context);
    }

    public void getWordlistAsList(int wordlistNum, LayoutInflater layoutInflater){
        Facade facade = Facade.getFacade();
        wordlistName = facade.getWordlistNameByNum(wordlistNum);
        for (int i = 0; i<facade.getWordlistLinesCountByNum(wordlistNum);i++){
            LineView.getLine(layoutInflater, this, wordlistNum,i);
        }
    }

    public void changeWlView(){
        LinearLayout.LayoutParams lp;
        LinearLayout.LayoutParams lp2;
        switch (this.getChildAt(0).findViewById(R.id.primeTV).getLayoutParams().height){
            case (LayoutParams.MATCH_PARENT):{
                lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,7);
                lp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,0);
                break;
            }
            case(LayoutParams.WRAP_CONTENT):{
                lp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,6);
                lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,4);
                break;
            }
            default:{
                lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,7);
                lp2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,0);
                break;
            }
        }
        lp.setMargins(0,0,8,0);
        for (int i = 0; i< this.getChildCount();i++){
            this.getChildAt(i).findViewById(R.id.primeTV).setLayoutParams(lp);
            this.getChildAt(i).findViewById(R.id.translateTV).setLayoutParams(lp2);
        }
    }

    public static void getWordlistAsButton(final int wordListNum, final Activity activity, final WLView wlView, final LayoutInflater layoutInflater, LinearLayout linearLayout) {
        Facade facade = Facade.getFacade();
        Button button = new Button(activity);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wlView.removeAllViews();
                wlView.getWordlistAsList(wordListNum,layoutInflater);
            }
        };


        button.setText(facade.getWordlistNameByNum(wordListNum));
        button.setOnClickListener(onClickListener);
        linearLayout.addView(button);
    }

    /*public void getEditableWordlist(LayoutInflater layoutInflater){
        Facade facade = Facade.getFacade();
        int wordlistNum = facade.getWordlistNumByName(wordlistName);
        for (int i = 0; i<facade.getWordlistLinesCountByNum(wordlistNum);i++){
            LineView.getEditableLine(wordlistNum, i, this,layoutInflater);
        }
    }*/

}
