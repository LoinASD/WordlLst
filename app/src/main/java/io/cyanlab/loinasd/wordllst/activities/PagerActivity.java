package io.cyanlab.loinasd.wordllst.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.cyanlab.loinasd.wordllst.R;
import io.cyanlab.loinasd.wordllst.controller.FragmentsController;
import io.cyanlab.loinasd.wordllst.controller.MainController;
import io.cyanlab.loinasd.wordllst.controller.database.DataProvider;
import io.cyanlab.loinasd.wordllst.controller.pdf.SimplePDFLoader;
import io.cyanlab.loinasd.wordllst.controller.pdf.WordList;

public class PagerActivity extends AppCompatActivity implements FragmentsController {

    ViewPager pager;

    WordListPagerAdapter pagerAdapter;

    MainController mainController;

    static final int REQUEST_CODE_FM = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pager);

        pager = findViewById(R.id.pager);

        DataProvider.loadDatabase(this);

        ListsFragment lists = new ListsFragment();

        LinesFragment lines = new LinesFragment();

        mainController = new MainController(lists, lines, this, lines);

        adjustPager(pager, lists, lines);

        showLists();

        View home = findViewById(R.id.home_button);

        home.setOnClickListener(view -> {

            if (pager.getCurrentItem() == 0)
                return;

            pager.setCurrentItem(0, true);
        });

        View add = findViewById(R.id.add_button);

        if (add != null){

            add.setOnClickListener(view -> {

                if (pager.getCurrentItem() == 1){

                    mainController.editList(pagerAdapter.currentList);

                    return;
                }

                Intent data = new Intent(view.getContext(), FileManagerActivity.class);

                startActivityForResult(data, REQUEST_CODE_FM);

            });
        }

        TextView title = findViewById(R.id.title_text);

        if (title != null){

            title.setOnClickListener(view -> {

                if (pager.getCurrentItem() != 1)
                    return;

                View toolbar = findViewById(R.id.toolbar);

                if (toolbar == null)
                    return;

                boolean isCollapsed = toolbar.getLayoutParams().height != ViewGroup.LayoutParams.WRAP_CONTENT;

                float height = isCollapsed ? ViewGroup.LayoutParams.WRAP_CONTENT : getResources().getDimension(R.dimen.app_bar_height);

                toolbar.getLayoutParams().height = (int) height;

                title.setSingleLine(!isCollapsed);

                findViewById(R.id.appbar).refreshDrawableState();
            });

            title.setText(getTitle());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        DataProvider.closeDatabase();
    }

    private void adjustPager(ViewPager viewPager, Fragment lists, Fragment lines){

        if (viewPager == null)
            return;

        pagerAdapter = new WordListPagerAdapter(getSupportFragmentManager(), lists, lines);

        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                /*boolean isPrev = position < pager.getCurrentItem();

                TextView title = findViewById(R.id.title_text);

                if (title == null)
                    return;

                float alpha = Math.abs(positionOffset - 0.5f) * 2f;

                title.setAlpha(alpha);

                int textPos;

                if (isPrev)
                    if (positionOffset < 0.5f)
                        textPos = position - 1;
                    else
                        textPos = position;
                else if (positionOffset > 0.5f)
                    textPos = position;
                else
                    textPos = position - 1;

                CharSequence name = textPos != -1 ? pagerAdapter.currentList.name : getTitle();

                if (title.getText() != name)
                    title.setText(name);

                View appbar = findViewById(R.id.appbar);

                float trans = (-1 + alpha) * appbar.getHeight();

                appbar.setTranslationY(trans);

                int colorID;

                if (isPrev)
                    if (positionOffset < 0.5f){
                        colorID = R.color.primaryColor;
                    }else
                        colorID = R.color.secondaryDarkColor;
                else if (positionOffset > 0.5f)
                    colorID = R.color.secondaryDarkColor;
                else
                    colorID = R.color.primaryColor;

                if (position == 1)
                    colorID = R.color.secondaryDarkColor;

                int color = getResources().getColor(colorID);

                appbar.setBackgroundColor(color);

                getWindow().setStatusBarColor(color);*/
            }

            @Override
            public void onPageSelected(final int position) {

                /*View appbar = findViewById(R.id.appbar);

                int colorID;

                if (position == 1)
                    colorID = R.color.secondaryColor;
                else
                    colorID = R.color.primaryColor;

                int color = getResources().getColor(colorID);

                appbar.setBackgroundColor(color);

                getWindow().setStatusBarColor(color);*/

                TextView title = findViewById(R.id.title_text);

                title.animate().alpha(0f).setDuration(150).withEndAction(() -> {

                        title.setText(position == 1 ? pagerAdapter.currentList.name : getTitle());

                        title.animate().alpha(1f).setDuration(225).start();
                }).start();


                ImageView addButton = findViewById(R.id.add_button);

                int drawID = position == 0 ? R.drawable.ic_add_white_24dp : R.drawable.ic_mode_edit_white_24dp;

                Drawable drawable = getDrawable(drawID);

                drawable.setColorFilter(getResources().getColor(R.color.primaryTextColor), PorterDuff.Mode.SRC_IN);

                addButton.setImageDrawable(drawable);

                addButton.setAlpha(0f);

                addButton.animate().alpha(1f).setDuration(150).start();


                View toolbar = findViewById(R.id.toolbar);

                boolean isCollapsed = toolbar.getLayoutParams().height != ViewGroup.LayoutParams.WRAP_CONTENT;

                if (isCollapsed)
                    return;

                float height = getResources().getDimension(R.dimen.app_bar_height);

                toolbar.getLayoutParams().height = (int) height;

                title.setSingleLine(true);

                findViewById(R.id.appbar).refreshDrawableState();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null)
            return;

        if (requestCode == REQUEST_CODE_FM){

            String path = data.getStringExtra("file");

            if (path == null)
                return;

            mainController.loadFromPDF(new SimplePDFLoader(), path);
        }
    }

    @Override
    public void showLists() {

        runOnUiThread(() -> {

            if (pagerAdapter == null || pager.getAdapter() == null)
                return;

            pager.setCurrentItem(0, true);
        });
    }

    @Override
    public void showLines(WordList list) {

        runOnUiThread(() -> {

            if (pagerAdapter == null || pager.getAdapter() == null)
                return;

            boolean isFirst = pagerAdapter.currentList == null;

            pagerAdapter.currentList = list;

            if (isFirst)
                pagerAdapter.notifyDataSetChanged();

            pager.setCurrentItem(1, true);
        });
    }

    @Override
    public void hideLines(){

        if (pagerAdapter == null || pager.getAdapter() == null)
            return;

        pager.setCurrentItem(0);

        pagerAdapter.currentList = null;
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPDFLoadingStarted() {

        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
    }

    @Override
    @AnyThread
    public void onPDFLoadingFinished() {

        runOnUiThread(() -> findViewById(R.id.loading_layout).setVisibility(View.GONE));
    }

    @Override
    @AnyThread
    public void onPDFError(String what) {

        runOnUiThread(() -> {

            findViewById(R.id.loading_layout).setVisibility(View.GONE);

            Toast.makeText(this, what, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onListChanged(WordList list) {

        if (pager.getCurrentItem() != 1)
            return;

        TextView title = findViewById(R.id.title_text);

        title.animate().alpha(0f).setDuration(150).withEndAction(() -> {

            title.setText(list.name);

            title.animate().alpha(1f).setDuration(225).start();
        }).start();

        Toast.makeText(this, "List renamed", Toast.LENGTH_SHORT).show();
    }
}

class WordListPagerAdapter extends FragmentPagerAdapter {

    private Fragment listsFragment;
    private Fragment linesFragment;

    public WordList currentList;

    WordListPagerAdapter(FragmentManager manager, Fragment lists, Fragment lines){
        super(manager);

        listsFragment = lists;
        linesFragment = lines;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment;

        switch (position){

            case 0 : {

                fragment = listsFragment;
                break;
            }

            case 1 : {

                fragment = linesFragment;
                break;
            }

            default: fragment = null;
        }

        return fragment;
    }

    @Override
    public int getCount() {

        return currentList == null ? 1 : 2;
    }
}
