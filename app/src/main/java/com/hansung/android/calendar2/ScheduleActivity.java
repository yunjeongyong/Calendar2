package com.hansung.android.calendar2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class ScheduleActivity extends Activity {

    private int year;
    private int month;
    private int date;
    private int hour, endHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Intent intent = new Intent();
        year = intent.getIntExtra("year", -1);
        month = intent.getIntExtra("month", -1);
        date = intent.getIntExtra("date", -1);
        hour = intent.getIntExtra("hour", -1);
        endHour = (hour + 1) % 24;

        if ( hour == -1 ) {
            Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR);
        }

        EditText titleEditText = findViewById(R.id.schedule_title);
        titleEditText.setText(String.format("%d년 %d월 %d일 %d시", year, month, date, hour));

        TimePicker startPicker = findViewById(R.id.schedule_start);
        startPicker.setCurrentHour(hour);

        TimePicker endPicker = findViewById(R.id.schedule_end);
        endPicker.setCurrentHour(endHour);
    }
}
