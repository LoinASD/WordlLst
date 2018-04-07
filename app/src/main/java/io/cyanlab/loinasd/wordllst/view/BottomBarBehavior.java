package io.cyanlab.loinasd.wordllst.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.cyanlab.loinasd.wordllst.R;

public class BottomBarBehavior<V extends LinearLayout> extends CoordinatorLayout.Behavior<V> {

    private LinearLayout parentView;
    private int maxHeight;

    public BottomBarBehavior(Context context, AttributeSet attrs){
        super(context, attrs);
        height = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency instanceof AppBarLayout;
    }


    float height;

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {

        if (dependency.getId() != R.id.scrollView && dependency.getY() + dependency.getHeight() < 56 * height){
            child.setTranslationY((1 - ((dependency.getY() + dependency.getHeight())/ (56 * height))) * 100);
            return true;
        }else if (dependency.getId() != R.id.scrollView && dependency.getY() + dependency.getHeight() >= 56 * height){
            child.setTranslationY(0);
            //child.setTranslationY((((dependency.getY() + dependency.getHeight()) - (100 * height))/(dependency.getHeight() - (100 * height)))*100);
        }

        return false;
    }
}
