package com.hansung.android.calendar2;

// 날짜 및 시간 선택 시 선택된 정보를 담아두기 위한 클래스
public class DateInfo {

    private static final DateInfo instance = new DateInfo();

    // 연,월,일,시간
    // 기본값은 -1
    private int year;
    private int month;
    private int date;
    private int hour;

    private DateInfo() {
        year = -1;
        month = -1;
        date = -1;
        hour = -1;
    }

    public static DateInfo getInstance() {
        return instance;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void set(int year, int month, int date, int hour) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hour;
    }

    public void set(int year, int month, int date) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = -1;
    }
}
