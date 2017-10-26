package io.cyanlab.loinasd.wordllst.view;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.app.Activity;
import android.widget.ListView;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.activities.MainActivity;
import io.cyanlab.loinasd.wordllst.controller.DBHelper;
import io.cyanlab.loinasd.wordllst.model.Facade;


public class WLView extends ListView {
    private String wordlistName;

    public String getWordlistName(){
        return wordlistName;
    }

    public WLView(Context context) {
        super(context);
    }

    /*public void getWordlistAsList(int wordlistNum, LayoutInflater layoutInflater){
        Facade facade = Facade.getFacade();
        wordlistName = facade.getWlName(wordlistNum);
        for (int i = 0; i<facade.getLinesCount(wordlistNum); i++){
            LineView.getLine(layoutInflater, this, wordlistNum,i);
        }
    }*/

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

    public static void getWLsAsButtons(final Activity activity, LinearLayout linearLayout, DBHelper dbHelper) {


        final String[] wlNames = dbHelper.loadWlsNames();

        for (int i = 0; i < wlNames.length; i++) {
            Button button = new Button(activity);
            final int k = i;
            OnClickListener onClickListener = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString("wlName", wlNames[k]);
                    Message msg = new Message();
                    msg.setData(bundle);
                    msg.what = 3;
                    MainActivity.h.sendMessage(msg);

                }
            };
            button.setText(wlNames[i]);
            button.setOnClickListener(onClickListener);
            button.setBackgroundColor(activity.getResources().getColor(R.color.colorButtons));
            linearLayout.addView(button);
        }
    }

    /*public void getEditableWordlist(LayoutInflater layoutInflater){
        Facade facade = Facade.getFacade();
        int wordlistNum = facade.getWordlistNumByName(wordlistName);
        for (int i = 0; i<facade.getWordlistLinesCountByNum(wordlistNum);i++){
            LineView.getEditableLine(wordlistNum, i, this,layoutInflater);
        }
    }*/

}
