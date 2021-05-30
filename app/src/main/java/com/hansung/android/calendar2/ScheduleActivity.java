package com.hansung.android.calendar2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        Intent intent = getIntent();
        year = intent.getIntExtra("year", -1);
        month = intent.getIntExtra("month", -1) + 1;
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

        Button saveButton = findViewById(R.id.schedule_save_btn);
        Button cancelButton = findViewById(R.id.schedule_cancel_btn);
        Button delButton = findViewById(R.id.schedule_del_btn);

        // 저장 버튼 누르면
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 저장할 때 어떻게 해야 되는지
            }
        });

        //취소 버튼 누르면
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(0);
            }
        });

        // 삭제 버튼 누르면
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 스케줄 삭제할 때
            }
        });
    }
}
