package com.hansung.android.calendar2;

// 일정을 하나의 객체로 다루기 위해 만든 클래스
// 모든 일정은 _id값을 통해 관리됨
public class Schedule {

    public int _id;

    public String title;
    public String place;
    public String memo;

    public int year;
    public int month;
    public int date;

    public int startHour;

    public int startMinute;
    public int endHour;
    public int endMinute;

    public Schedule() {
        _id = -1;
    }

    public Object[] toArray() {
        return new Object[] {
                title, place, memo, year, month, date, startHour, startMinute, endHour, endMinute
        };
    }

    public String toString() {
        String s = "";
        for (Object o : toArray()) s += "" + o + ',';
        return s;
    }
}
