package com.hansung.android.calendar2;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper { //DB와 안드로이드를 연결시키기 위한 징검다리 역할 : DBHelper, SQLiteOpenHelper를 상속해서 만들었다.
    private static final String TAG = "[[SQLiteDB]]"; //에러 디버깅할 때 태그에 쓰기 위해서 선언함.

    private static final String TYPE_TEXT = "text"; // 컬럼의 타입이 "text"타입이라는 것을 지정하기 위해 선언.
    private static final String TYPE_INT = "integer"; // 위와 동일하게 인티저로 지정.

    private static final String[][] columns = { // 컬럼 정보를 모아 놓은 배열.
        {
            "title",
            "place",
            "memo",
            "sch_year",     // year, month, date는 DB 변수명과의 중복을 피하기 위해 sch를 앞에 붙임 (sch_year, sch_month, sch_date)
            "sch_month",
            "sch_date",
            "start_hour",
            "start_minute",
            "end_hour",
            "end_minute"
        }, {
            TYPE_TEXT,
            TYPE_TEXT,
            TYPE_TEXT,
            TYPE_INT,
            TYPE_INT,
            TYPE_INT,
            TYPE_INT,
            TYPE_INT,
            TYPE_INT,
            TYPE_INT
        }
    };
    private static final HashMap<String, String> columnMap = new HashMap<>(columns[0].length); // 컬럼 정보를 모아 놓은 맵.

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        // 맵 초기화
        for (int i=0; i<columns[0].length; i++) {
            columnMap.put(columns[0][i], columns[1][i]);
        }
        columnMap.put("_id", TYPE_INT);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 테이블 생성 코드를 쉽게 쓰기 위한 generateInsertPhrase 메소드를 담아 놓은 클래스
        class Temp {
            public String generateInsertPhrase(String name, String type, boolean isLast) {
                return String.format("%s %s not null%s", name, type, isLast ? "" : ",");
            }
            public String generateInsertPhrase(String name, String type) {
                return generateInsertPhrase(name, type, false);
            }
        }
        Temp t = new Temp();

        // 테이블 생성 코드를 위한 StringBuilder
        StringBuilder sqlBuilder = new StringBuilder("create table if not exists schedules (_id integer primary key autoincrement,");
        final int last = columns[0].length - 1;
        for (int i=0; i<last; i++) sqlBuilder.append(t.generateInsertPhrase(columns[0][i], columns[1][i]));
        sqlBuilder.append(t.generateInsertPhrase(columns[0][last], columns[1][last], true));
        sqlBuilder.append(')');
        db.execSQL("drop table if exists schedules"); // 디버깅을 위한 코드로, 앱이 실행될 때마다 테이블이 존재하면 삭제 및 재생성
        db.execSQL(sqlBuilder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE if exists schedules";
        db.execSQL(sql);
        onCreate(db);
    }

    // String.format을 간단하게 쓰려고 만든 함수
    private String f(String s, Object... args) {
        return String.format(s, args);
    }

    // 입력받은 Schedule 객체를 바탕으로 insert 코드 생성 및 실행
    public void insertSchedule(Schedule s) {
        try {
            String sql = f(
                    "insert into schedules (%s) values ('%s','%s','%s',%d,%d,%d,%d,%d,%d,%d)",
                    TextUtils.join(",", columns[0]),
                    s.title,
                    s.place,
                    s.memo,
                    s.year,
                    s.month,
                    s.date,
                    s.startHour,
                    s.startMinute,
                    s.endHour,
                    s.endMinute
            );
            getWritableDatabase().execSQL(sql);

        } catch(SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // 파라미터를 기준으로 DB에서 select 작업 실행
    // title이 hello, year이 2021인 데이터를 가져오고 싶다면 getSchedules("title", "hello", "sch_year", 2021) 실행
    public ArrayList<Schedule> getSchedules(Object ...args) {//DB에서 일정들을 가져옴
        try {
            // where 부분을 만들기 위한 builder
            String where = "";
            // 인자로 받은 args가 있을 경우
            if ( args.length > 0 ) {
                // 각각의 조건들을 conditions 배열에 저장
                String[] conditions = new String[args.length / 2];
                for (int i = 0, j = 0; i < args.length; i += 2, j++) {
                    conditions[j] = args[i] + "=";
                    switch (columnMap.get(args[i])) {
                        case TYPE_TEXT:
                            conditions[j] += "'" + args[i + 1] + "'";
                            break;
                        case TYPE_INT:
                            conditions[j] += args[i + 1];
                    }
                }
                // 작성된 where 조건들을 and로 묶음
                where = " where " + TextUtils.join(" and ", conditions);
            }

            String sql = "select * from schedules" + where; //컬럼 전체를 표로부터 가져와라. schedules는 표, 테이블 이름이다. 데이리스트 같은 애.
            Cursor cursor = getReadableDatabase().rawQuery(sql, null);
            return cursorToSchedules(cursor);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    // 주간달력에서, 한 fragment에서 보여지는 모든 날들의 일정을 가져오고자 할 때 사용
    // 예를 들어 2020년 12월 마지막주 주간달력을 틀 경우 2020.12.27~2021.1.2 이렇게 년,월이 도중에 모두 바뀌게 됨
    // 주간달력 fragment에서 갖고 있는 멤버변수들은 year=2020, month=11이므로 이걸 이용해서 select를 하게 되면 2020.12.27, 2020.12.28, ... 2020.12.2 이런식으로 예상치 못한 결과를 얻게 될 수 있음
    // 이걸 방지하기 위해 만든 함수
    public ArrayList<Schedule> getSchedulesInDays(int year, int month, int[] days) {
        try {
            // days 배열의 마지막 원소
            final int lastOfDays = days[days.length - 1];
            // 마지막 원소가 첫번째 원소보다 크다는 것은 월이 바뀌지 않는다는 것
            if ( lastOfDays > days[0] ) {
                String sql = f("select * from schedules where sch_year=%d and sch_month=%d and sch_date>=%d and sch_date<=%d", year, month, days[0], lastOfDays);
                Cursor cursor = getReadableDatabase().rawQuery(sql, null);
                ArrayList<Schedule> schedules = cursorToSchedules(cursor);
                cursor.close();
                return schedules;

            } else {    // 월이 바뀌는 케이스
                int lastDay = -1;   // days 배열에서 가장 큰 값을 추출 (해당 월의 마지막 날)
                for (int day : days) {
                    if (lastDay < day) {
                        lastDay = day;
                        break;
                    }
                }
                // 해당 월, 그 다음 월에서의 select를 각각 수행하기 위해 쿼리 2개 선언
                String sql1 = f("select * from schedules where sch_year=%d and sch_month=%d and sch_date>=%d and sch_date<=%d", year, month, days[0], lastDay);
                String sql2;

                if ( month == 11 ) {    // 12월일 경우 (연이 바뀜)
                    sql2 = f("select * from schedules where sch_year=%d and sch_month=%d and sch_date>=%d and sch_date<=%d",
                            year + 1, 0, 1, lastOfDays);
                } else {    // 아닐 경우 (월만 바뀜)
                    sql2 = f("select * from schedules where sch_year=%d and sch_month=%d and sch_date>=%d and sch_date<=%d",
                            year, month + 1, 1, lastOfDays);
                }

                Cursor cursor = getReadableDatabase().rawQuery(sql1, null);
                ArrayList<Schedule> schedules = cursorToSchedules(cursor);
                cursor.close();

                Cursor cursor2 = getReadableDatabase().rawQuery(sql2, null);
                schedules.addAll(cursorToSchedules(cursor2));
                cursor2.close();

                return schedules;
            }

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    // 값이 DB에 없으면 insert, 있으면 update
    public int upsertSchedule(Schedule s) {
        try {
            Cursor cursor = getReadableDatabase().rawQuery("select title from schedules where _id=" + s._id, null);
            if ( cursor.getCount() == 0 ) { // 새로운 값을 추가할 때
                insertSchedule(s);

            } else {
                String sql = f(
                        "update schedules set title='%s',place='%s',memo='%s',sch_year=%d,sch_month=%d,sch_date=%d,start_hour=%d,start_minute=%d,end_hour=%d,end_minute=%d where _id=%d",
                        s.title,
                        s.place,
                        s.memo,
                        s.year,
                        s.month,
                        s.date,
                        s.startHour,
                        s.startMinute,
                        s.endHour,
                        s.endMinute,
                        s._id
                );
                getWritableDatabase().execSQL(sql);
            }
            cursor.close();

            Cursor cursor2 = getReadableDatabase().rawQuery("select _id from schedules order by _id desc limit 1", null);
            cursor2.moveToFirst();

            int _id = cursor2.getInt(0);
            cursor2.close();
            return _id;

        } catch(SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        return -1;
    }

    // _id를 바탕으로 값 삭제
    public void deleteSchedule(int _id) {
        try {
            String sql = "delete from calendars where _id=" + _id;
            getWritableDatabase().execSQL(sql);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // Cursor를 ArrayList로 변환하는 함수
    // 빈 ArrayList를 생성한 뒤 Cursor를 루프를 돌리면서 schedule 객체를 만들고, 그걸 추가시킴
    private ArrayList<Schedule> cursorToSchedules(Cursor c) {
        ArrayList<Schedule> scheduleList = new ArrayList<>(c.getCount());
        String[] columnNames = c.getColumnNames();
        while (c.moveToNext()) {
            Schedule s = new Schedule();
            int i = 0;
            s._id = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.title = c.getString(c.getColumnIndex(columnNames[i++]));
            s.place = c.getString(c.getColumnIndex(columnNames[i++]));
            s.memo = c.getString(c.getColumnIndex(columnNames[i++]));
            s.year = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.month = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.date = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.startHour = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.startMinute = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.endHour = c.getInt(c.getColumnIndex(columnNames[i++]));
            s.endMinute = c.getInt(c.getColumnIndex(columnNames[i]));
            scheduleList.add(s);
        }
//        c.close();
        return scheduleList;
    }
}
