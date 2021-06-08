package com.hansung.android.calendar2;

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

    public Schedule() {}

    public Schedule(String title, String place, String memo, int year, int month, int date, int startHour, int startMinute, int endHour, int endMinute) {
        this.title = title;
        this.place = place;
        this.memo = memo;
        this.year = year;
        this.month = month;
        this.date = date;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
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
