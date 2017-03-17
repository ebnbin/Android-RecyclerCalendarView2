package com.ebnbin.recyclercalendarview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ebnbin.recyclercalendarview.RecyclerCalendarView;

import java.util.Arrays;

public class MainActivity extends Activity {
    private RecyclerCalendarView mRecyclerCalendarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRecyclerCalendarView = (RecyclerCalendarView) findViewById(R.id.recycler_calendar_view);
        mRecyclerCalendarView.setRange(2015, 5, 2017, 3);
        mRecyclerCalendarView.selectDate(new int[]{2017, 3, 18});
        mRecyclerCalendarView.scrollToSelected();
        mRecyclerCalendarView.listeners.add(new RecyclerCalendarView.Listener() {
            @Override
            public void onSelected(int[] date) {
                super.onSelected(date);

                Toast.makeText(MainActivity.this, Arrays.toString(date), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
