package com.hansung.android.calendar2;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private int _id;
    private Schedule schedule;

    private int year;
    private int month;
    private int date;

    private EditText titleEditText;
    private TimePicker startPicker;
    private TimePicker endPicker;
    private EditText placeEditText;
    private EditText memoEditText;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        DBHelper helper = new DBHelper(ScheduleActivity.this, "calendar.db", null, 1);

        Calendar calendar = Calendar.getInstance();
        Intent intent = getIntent();

        year = intent.getIntExtra("year", -1);
        month = intent.getIntExtra("month", -2) + 1;
        date = intent.getIntExtra("date", -1);
        _id = intent.getIntExtra("_id", -1);

        titleEditText = findViewById(R.id.schedule_title);
        startPicker = findViewById(R.id.schedule_start);
        endPicker = findViewById(R.id.schedule_end);
        placeEditText = findViewById(R.id.schedule_place);
        memoEditText = findViewById(R.id.schedule_memo);

        if ( _id != -1 ) {
            // 수정할 _id 값을 통해 DB에서 일정을 불러옴
            schedule = helper.getSchedules("_id", _id).get(0);

            // 불러온 일정 객체를 바탕으로 위젯들 초기화
            titleEditText.setText(schedule.title);
            startPicker.setCurrentHour(schedule.startHour);
            startPicker.setCurrentMinute(schedule.startMinute);
            endPicker.setCurrentHour(schedule.endHour);
            endPicker.setCurrentMinute(schedule.endMinute);
            placeEditText.setText(schedule.place);
            memoEditText.setText(schedule.memo);

        } else {
            // 일정 객체 초기화
            schedule = new Schedule();

            // startHour 변수를 입력받은 시간이 있으면 (=주간 달력에서 넘어온 경우) 해당 시간으로, 없으면 현재 시간으로 설정
            int startHour = intent.getIntExtra("hour", -1);
            if ( startHour == -1 ) startHour = calendar.get(Calendar.HOUR_OF_DAY);
            // endHour는 startHour의 1시간 뒤
            int endHour = (startHour + 1) % 24;

            titleEditText.setText(String.format(Locale.KOREA, "%d년 %d월 %d일 %d시", year, month, date, startHour));

            startPicker.setCurrentHour(startHour);
            startPicker.setCurrentMinute(0);

            endPicker.setCurrentHour(endHour);
            endPicker.setCurrentMinute(0);
        }

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

                // 같은 연,월,일에 동일한 제목의 일정이 있으면 저장 불가
                ArrayList<Schedule> prevSchedules = helper.getSchedules("sch_year", year, "sch_month", month, "sch_date", date, "title", titleEditText.getText().toString());
                if ( !prevSchedules.isEmpty() ) {
                    Toast.makeText(ScheduleActivity.this.getApplicationContext(), "동일한 제목의 일정이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

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

                _id = helper.upsertSchedule(schedule);
                schedule._id = _id;
                Toast.makeText(ScheduleActivity.this.getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //취소 버튼 누르면 액티비티 종료
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
                // 컨펌 다이얼로그를 띄워 '예'를 누르면 삭제, '아니오'를 누르면 아무 작업 안함
                new AlertDialog.Builder(ScheduleActivity.this.getApplicationContext())
                        .setTitle("삭제")
                        .setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // _id 값이 있다면 (-1이 아니라면) helper를 통해 DB에서 그 _id에 해당하는 값 삭제
                                // _id 값이 없다면 DB에서 삭제할 게 없다는 말이므로 아무것도 하지 않음
                                if ( _id != -1 ) helper.deleteSchedule(_id);

                                Toast.makeText(ScheduleActivity.this.getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();

                                // 삭제 작업 후 액티비티 종료
                                ScheduleActivity.this.finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null);
            }
        });

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
