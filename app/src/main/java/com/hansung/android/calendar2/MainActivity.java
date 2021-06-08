package com.hansung.android.calendar2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);

                DateInfo dateInfo = DateInfo.getInstance();
                intent.putExtra("year", dateInfo.getYear());
                intent.putExtra("month", dateInfo.getMonth());
                intent.putExtra("date", dateInfo.getDate());
                intent.putExtra("hour", dateInfo.getHour());

                startActivity(intent);
            }
        });
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
