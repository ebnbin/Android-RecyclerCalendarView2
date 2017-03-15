package com.ebnbin.recyclercalendarview.rn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ebnbin.recyclercalendarview.RecyclerCalendarView;

import java.util.Locale;

public class RCTTimerFilterView extends RecyclerCalendarView {
    public RCTTimerFilterView(@NonNull Context context) {
        super(context);

        listener = new Listener() {
            @Override
            public void onSingleSelected(int[] date) {
                super.onSingleSelected(date);

                if (rnListener != null) {
                    String dateString = getRNDateString(date);
                    rnListener.onSelectDate(dateString, dateString);
                }
            }

            @Override
            public void onDoubleSelected(int[] dateFrom, int[] dateTo, int dayCount) {
                super.onDoubleSelected(dateFrom, dateTo, dayCount);

                if (rnListener != null) {
                    rnListener.onSelectDate(getRNDateString(dateFrom), getRNDateString(dateTo));
                }
            }
        };
    }

    //*****************************************************************************************************************
    // React Native.

    private String mRNStartTime;
    private String mRNType;
    public RNListener rnListener;

    public void setRNStartTime(String startTime) {
        mRNStartTime = TextUtils.isEmpty(startTime) ? "" : startTime;

        if (mRNType != null && !mRNType.equals("custome")) {
            setRNSingleSelectedMode();

            mRNStartTime = null;
            mRNType = null;
        }
    }

    public void setRNType(String type) {
        mRNType = TextUtils.isEmpty(type) ? "" : type;

        boolean doubleSelectedMode = "custome".equals(mRNType);
        if (doubleSelectedMode) {
            setDoubleSelectedMode(true);

            mRNStartTime = null;
            mRNType = null;
        } else if (mRNStartTime != null) {
            setRNSingleSelectedMode();

            mRNStartTime = null;
            mRNType = null;
        }
    }

    private void setRNSingleSelectedMode() {
        int date[] = new int[3];

        String[] dateStringSplit = mRNStartTime.split("-");
        if (dateStringSplit.length == 3) {
            try {
                date[0] = Integer.parseInt(dateStringSplit[0]);
                date[1] = Integer.parseInt(dateStringSplit[1]);
                date[2] = Integer.parseInt(dateStringSplit[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        setDoubleSelectedMode(date);
    }

    private String getRNDateString(int[] date) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", date[0], date[1], date[2]);
    }

    public interface RNListener {
        void onSelectDate(String begin, String end);
    }
}
