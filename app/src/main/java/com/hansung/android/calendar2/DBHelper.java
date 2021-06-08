package com.hansung.android.calendar2;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "[[SQLiteDB]]";

    private static final String TYPE_TEXT = "text";
    private static final String TYPE_INT = "integer";

    private static final String[][] columns = {
        {
            "title",
            "place",
            "memo",
            "sch_year",
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
    private static final HashMap<String, String> columnMap = new HashMap<>(columns[0].length);

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        for (int i=0; i<columns[0].length; i++) {
            columnMap.put(columns[0][i], columns[1][i]);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        class Temp {
            public String generateInsertPhrase(String name, String type, boolean isLast) {
                return String.format("%s %s not null%s", name, type, isLast ? "" : ",");
            }
            public String generateInsertPhrase(String name, String type) {
                return generateInsertPhrase(name, type, false);
            }
        }
        Temp t = new Temp();

        StringBuilder sqlBuilder = new StringBuilder("create table if not exists schedules (_id integer primary key autoincrement,");
        final int last = columns[0].length - 1;
        for (int i=0; i<last; i++) sqlBuilder.append(t.generateInsertPhrase(columns[0][i], columns[1][i]));
        sqlBuilder.append(t.generateInsertPhrase(columns[0][last], columns[1][last], true));
        sqlBuilder.append(')');
        db.execSQL(sqlBuilder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE if exists schedules";
        db.execSQL(sql);
        onCreate(db);
    }

    public void insertSchedule(Schedule s) {
        try {
            String sql = String.format(
                    Locale.KOREA,
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

            ArrayList<Schedule> schedules = getSchedules();
            String res = "";
            for (Schedule schedule : schedules) res += schedule.toString() + '\n';
            Log.i(TAG, res);


        } catch(SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public ArrayList<Schedule> getSchedules(Object ...args) {
        try {
            StringBuilder whereBuilder = new StringBuilder();
            if ( args.length > 0 ) {
                whereBuilder.append(" where ");
                for (int i = 0, j = 0; i < args.length / 2; i += 2, j++) {
                    whereBuilder.append(args[i]);
                    whereBuilder.append('=');
                    switch (columnMap.get(args[i])) {
                        case TYPE_TEXT:
                            whereBuilder.append('\'');
                            whereBuilder.append(args[i + 1]);
                            whereBuilder.append('\'');
                            break;
                        case TYPE_INT:
                            whereBuilder.append(args[i + 1]);
                    }
                    if ( j != args.length - 1 ) whereBuilder.append(" and ");
                }
            }

            String sql = "select * from schedules" + whereBuilder.toString();
            Cursor cursor = getReadableDatabase().rawQuery(sql, null);
            return cursorToSchedules(cursor);

        } catch(SQLException e) {
            Log.e(TAG, e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public Schedule getLastSchedule() {
        try {
            String sql = "select * from schedules order by _id desc limit 1";
            Cursor cursor = getReadableDatabase().rawQuery(sql, null);
            return cursorToSchedules(cursor).get(0);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private void update(int _id, String column, String sValue, int iValue) {
        try {
            String sql = String.format(
                    Locale.KOREA,
                    "update calendars set %s=%s where _id=%d",
                    column,
                    sValue != null ? '\'' + sValue + '\'' : Integer.toString(iValue),
                    _id
            );
            getWritableDatabase().execSQL(sql);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void updateSchedule(int _id, String column, String value) {
        update(_id, column, value, -1);
    }

    public void updateSchedule(int _id, String column, int value) {
        update(_id, column, null, value);
    }

    public void updateSchedule(int _id, Schedule schedule) {
        try {
            Object[] fields = schedule.toArray();
            String s = "";
            for (int i=0; i<columns[0].length; i++) {
                s += columns[0][i] + "=" + (fields[i] instanceof String ? "\"" + fields[i] + "\"" : fields[i]);
                if ( i != columns[0].length - 1 ) s += ',';
            }
            String sql = "update calendars set " + s + " where _id=" + _id;
            getWritableDatabase().execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void deleteSchedule(int _id) {
        try {
            String sql = "delete from calendars where _id=" + _id;
            getWritableDatabase().execSQL(sql);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private ArrayList<Schedule> cursorToSchedules(Cursor c) {
        ArrayList<Schedule> scheduleList = new ArrayList<>();
        String[] columnNames = c.getColumnNames();
        while (c.moveToNext()) {
            Schedule s = new Schedule();
            for (String ss : c.getColumnNames()) Log.i(TAG, ss);
            int i = 0;
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
        return scheduleList;
    }
}
