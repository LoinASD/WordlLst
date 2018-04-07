package io.cyanlab.loinasd.wordllst.view;

import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StatisticsPercentsBehavior<V extends TextView> extends CoordinatorLayout.Behavior<V> {

    private TextView textView;
    private LinearLayout parentView;
    private int maxHeight;

    public StatisticsPercentsBehavior(){}

    public StatisticsPercentsBehavior(TextView textView, LinearLayout parentView){
        this.textView = textView;
        this.parentView = parentView;
        maxHeight = textView.getHeight();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency.equals(parentView);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {

        if (dependency.getMeasuredHeight() < 10){
            textView.setHeight(maxHeight * (dependency.getMeasuredHeight() / 10));
        }


        return super.onDependentViewChanged(parent, child, dependency);
    }
}
