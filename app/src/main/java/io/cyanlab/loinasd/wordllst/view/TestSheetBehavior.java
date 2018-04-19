package io.cyanlab.loinasd.wordllst.view;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import io.cyanlab.loinasd.wordllst.R;

public class TestSheetBehavior<V extends LinearLayout> extends CoordinatorLayout.Behavior<V> {

    private LinearLayout parentView;
    private int maxHeight;

    public TestSheetBehavior(Context context, AttributeSet attrs){
        super(context, attrs);
        height = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency instanceof AppBarLayout;
    }


    private float height;

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {

        if (dependency.getId() != R.id.scrollView && dependency.getY() + dependency.getHeight() < 56 * height){
            child.setTranslationY(dependency.getY() - (dependency.getHeight() - child.getHeight()) - height * 49 + ((dependency.getHeight() + dependency.getY())/ 56 * height - 1) * 32 * height);
            return true;
        }else if (dependency.getId() != R.id.scrollView && dependency.getY() + dependency.getHeight() >= 56 * height){
            child.setY(dependency.getY() - (dependency.getHeight() - child.getHeight()) - height * 49);
            return true;
            //child.setTranslationY((((dependency.getY() + dependency.getHeight()) - (100 * height))/(dependency.getHeight() - (100 * height)))*100);
        }

        return false;
    }
}
