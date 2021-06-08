package com.hansung.android.calendar2;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private int year;
    private int month;
    private int date;

    private EditText titleEditText;
    private TimePicker startPicker;
    private TimePicker endPicker;
    private EditText placeEditText;
    private EditText memoEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        DBHelper helper = new DBHelper(ScheduleActivity.this, "calendar.db", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.onCreate(db);

        Calendar calendar = Calendar.getInstance();
        Intent intent = getIntent();

        year = intent.getIntExtra("year", -1);
        month = intent.getIntExtra("month", -2) + 1;
        date = intent.getIntExtra("date", -1);

        int hour = intent.getIntExtra("hour", -1);
        if ( hour == -1 ) hour = calendar.get(Calendar.HOUR);
        int endHour = (hour + 1) % 24;

        titleEditText = findViewById(R.id.schedule_title);
        startPicker = findViewById(R.id.schedule_start);
        endPicker = findViewById(R.id.schedule_end);
        placeEditText = findViewById(R.id.schedule_place);
        memoEditText = findViewById(R.id.schedule_memo);

        Button placeFindButton = findViewById(R.id.schedule_find_btn);
        placeFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 장소 옆에 '찾기' 버튼 눌렀을 때
            }
        });

        Button saveButton = findViewById(R.id.schedule_save_btn);
        Button cancelButton = findViewById(R.id.schedule_cancel_btn);
        Button delButton = findViewById(R.id.schedule_del_btn);

        // 저장 버튼 누르면
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 저장할 때 어떻게 해야 되는지
                Schedule schedule = new Schedule();

                schedule.title = titleEditText.getText().toString();
                schedule.place = placeEditText.getText().toString();
                schedule.memo = memoEditText.getText().toString();

                int[] start = getDataFromTimePicker(startPicker);
                schedule.startHour = start[0];
                schedule.startMinute = start[1];

                int[] end = getDataFromTimePicker(endPicker);
                schedule.endHour = end[0];
                schedule.endMinute = end[1];

                schedule.year = year;
                schedule.month = month;
                schedule.date = date;

                if ( _id == -1 ) {
                    helper.insertSchedule(schedule);
                    _id = helper.getLastSchedule()._id;
                    Toast.makeText(ScheduleActivity.this.getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    helper.updateSchedule(_id, schedule);
                    Toast.makeText(ScheduleActivity.this.getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //취소 버튼 누르면
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScheduleActivity.this.finish();
            }
        });

        // 삭제 버튼 누르면
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 스케줄 삭제할 때
                TextView idTextView = view.findViewById(R.id.week_schedule_id);
                int _id = Integer.parseInt(idTextView.getText().toString());
                helper.deleteSchedule(_id);
            }
        });

//        Schedule prevExistingSchedule = helper.getSchedules()
        ArrayList<Schedule> prevSchedules = helper.getSchedules("sch_year", year, "sch_month", month, "sch_date", date);
        if ( !prevSchedules.isEmpty() ) {
            
        }

        _id = intent.getIntExtra("_id", -1);
        if ( _id != -1 ) {
            Schedule schedule = helper.getSchedules("_id", "" + _id).get(0);
            if ( schedule.year == year && schedule.month == month && schedule.date == date ) {
                titleEditText.setText(schedule.title);
                startPicker.setCurrentHour(schedule.startHour);
                startPicker.setCurrentMinute(schedule.startMinute);
                endPicker.setCurrentHour(schedule.endHour);
                endPicker.setCurrentMinute(schedule.endMinute);
                return;
            }
        }

        titleEditText.setText(String.format(Locale.KOREA, "%d년 %d월 %d일 %d시", year, month, date, hour));

        startPicker.setCurrentHour(hour);
        startPicker.setCurrentMinute(0);

        endPicker.setCurrentHour(endHour);
        endPicker.setCurrentMinute(0);

    }

    private int[] getDataFromTimePicker(TimePicker picker) {
        int hour, minute;
        if ( Build.VERSION.SDK_INT < 23 ) {
            hour = picker.getCurrentHour();
            minute = picker.getCurrentMinute();
        } else {
            hour = picker.getHour();
            minute = picker.getMinute();
        }
        return new int[] { hour, minute };
    }
}
