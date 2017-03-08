package com.ebnbin.recyclercalendarview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.ebnbin.recyclercalendarview.RecyclerCalendarView;

public class MainActivity extends Activity {
    private RecyclerCalendarView mRecyclerCalendarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerCalendarView = new RecyclerCalendarView(this);

        setContentView(mRecyclerCalendarView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        boolean doubleSelected = mRecyclerCalendarView.isDoubleSelectedMode();
        MenuItem doubleSelectedMenuItem = menu.findItem(R.id.double_selected_mode);
        doubleSelectedMenuItem.setTitle(doubleSelected ? R.string.single_selected_mode : R.string.double_selected_mode);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.double_selected_mode: {
                boolean doubleSelectedMode = mRecyclerCalendarView.isDoubleSelectedMode();
                mRecyclerCalendarView.setDoubleSelectedMode(!doubleSelectedMode);
                mRecyclerCalendarView.scrollToSelected();

                item.setTitle(doubleSelectedMode ? R.string.double_selected_mode : R.string.single_selected_mode);

                return true;
            }
            case R.id.reset_selected: {
                mRecyclerCalendarView.resetSelected();
                mRecyclerCalendarView.scrollToSelected();

                return true;
            }
            case R.id.scroll_to_today: {
                mRecyclerCalendarView.scrollToToday();

                return true;
            }
            case R.id.scroll_to_selected: {
                mRecyclerCalendarView.scrollToSelected();

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
