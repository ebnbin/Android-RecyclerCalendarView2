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

        setContentView(R.layout.activity_main);

        mRecyclerCalendarView = (RecyclerCalendarView) findViewById(R.id.recycler_calendar_view);
        mRecyclerCalendarView.setDoubleSelectedMode(2015, 5, 2017, 3);
        mRecyclerCalendarView.scrollToSelected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset_selected: {
                mRecyclerCalendarView.resetSelected();

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
