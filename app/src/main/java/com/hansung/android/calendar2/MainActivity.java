package com.hansung.android.calendar2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // calendar.db 생성 및 테이블 생성
        DBHelper helper = new DBHelper(MainActivity.this, "calendar.db", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.onCreate(db);

        // 앱이 실행되면 기본적으로 MonthViewFragment를 보여줌
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout, new MonthViewFragment());
        fragmentTransaction.commit();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ScheduleActivity를 띄우기 위한 intent 생성
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);

                // DateInfo 클래스로부터 연,월,일,시간 정보 가져옴
                DateInfo dateInfo = DateInfo.getInstance();
                int year = dateInfo.getYear();
                int month = dateInfo.getMonth();
                int date = dateInfo.getDate();
                int hour = dateInfo.getHour();

                // intent에 추가
                intent.putExtra("year", year);
                intent.putExtra("month", month);
                intent.putExtra("date", date);
                intent.putExtra("hour", hour);

                // DB로부터 그 연,월,일에 해당하는 일정들 가져옴
                // 만약 시간 정보가 존재한다면 (주간 달력일 경우) DB에서 데이터를 가져올 때 시간 정보도 추가
                ArrayList<Schedule> scheduleList = hour == -1
                        ? helper.getSchedules("sch_year", year, "sch_month", month, "sch_date", date)                       // 월간 달력일 경우
                        : helper.getSchedules("sch_year", year, "sch_month", month, "sch_date", date, "start_hour", hour);  // 주간 달력일 경우

                if ( scheduleList.size() > 1 ) {    // 가져온 데이터의 길이가 1 초과일 경우

                    // 다이얼로그에 채우기 위한 schedules 배열 생성 및 초기화
                    String[] schedules = new String[scheduleList.size()];
                    for (int i=0; i<scheduleList.size(); i++) schedules[i] = scheduleList.get(i).title;

                    // 일정들의 제목이 적힌 다이얼로그 띄움
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle(year + "." + (month+1) + "." + date + "일");
                    dialog.setItems(schedules, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 다이얼로그의 항목을 누를 경우 일정의 id값을 intent에 추가
                            intent.putExtra("_id", scheduleList.get(i)._id);
                            startActivityForResult(intent, 0);
                        }
                    });
                    dialog.show();
                } else if ( scheduleList.size() == 1 ) {    // 가져온 데이터가 1개일 경우 리스트의 0번째 항목의 id값을 intent에 추가
                    intent.putExtra("_id", scheduleList.get(0)._id);
                    startActivityForResult(intent, 0);
                } else {
                    //가져온 데이터가 없으면 intent에 id값 추가하지 않음
                    startActivityForResult(intent, 0);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
        ft.detach(fragment).attach(fragment).commit();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // 앱바의 옵션 버튼을 누를 경우 mainmenu.xml의 레이아웃을 보여줌
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 옵션 메뉴에서 '월간' 클릭 시 MonthViewFragment를 띄워 월간 달력 보여줌
            case R.id.monthview:
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.frameLayout, new MonthViewFragment());
                fragmentTransaction.commit();
                Toast.makeText(getApplicationContext(), "monthview", Toast.LENGTH_SHORT).show();
                return true;

            // 옵션 메뉴에서 '주간' 클릭 시 WeekViewFragment를 띄워 주간 달력 보여줌
            case R.id.weekview:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.frameLayout, new WeekViewFragment());
                fragmentTransaction.commit();
                Toast.makeText(getApplicationContext(), "weekview", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
