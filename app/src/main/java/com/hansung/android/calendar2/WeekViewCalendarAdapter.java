package com.hansung.android.calendar2;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;

public class WeekViewCalendarAdapter extends FragmentStateAdapter {
    private static final int NUM_ITEMS = 100;

    // 앱을 실행시킨 순간의 년,월,일
    private int iYear;
    private int iMonth;
    private int iDate;

    public WeekViewCalendarAdapter(@NonNull Fragment fragment) {
        super(fragment);

        // 자바 Calendar 클래스를 통해 iYear, iMonth, iDate 변수 초기화
        Calendar calendar = Calendar.getInstance();
        iYear = calendar.get(Calendar.YEAR);
        iMonth = calendar.get(Calendar.MONTH);
        iDate = calendar.get(Calendar.DATE);
    }

    @NonNull
    @Override
    public WeekCalendarFragment createFragment(int position) {

        // 시작 포지션이 50페이지 이므로 position 값에서 50을 빼줌
        position -= NUM_ITEMS / 2;

        // 시작 일자인 newDate 변수를 오늘 일자에서 position*7을 더한 값으로 설정
        int newDate = iDate + (position * 7);
        Calendar calendar = Calendar.getInstance();
        // 이번달의 마지막 날을 구함
        int lastDate = calendar.getActualMaximum(Calendar.DATE);

        // newYear, newMonth를 현재 년,월로 초기화
        int newYear = iYear;
        int newMonth = iMonth;

        if ( newDate > lastDate ) { // newDate가 lastDate보다 크면 (예를 들어 오늘이 30일인데 7을 더해 newDate가 37이 되었다면)
            // newDate가 lastDate보다 작아질 때까지 루프를 돌림
            while ( newDate > lastDate ) {
                // newDate가 lastDate보다 크면 달을 늘린 뒤 마지막 날을 받아와 뺌
                calendar.set(Calendar.YEAR, newYear);
                calendar.set(Calendar.MONTH, newMonth);
                lastDate = calendar.getActualMaximum(Calendar.DATE);
                newDate -= lastDate;
                if ( newMonth == 11 ) {
                    newYear++;
                    newMonth = 0;
                } else {
                    newMonth++;
                }
            }
        } else if ( newDate < 1 ) { // newDate가 1보다 작을 경우 (페이지를 왼쪽으로 많이 넘겼을 경우)
            // newDate가 1보다 커질 때까지 루프를 돌림
            while ( newDate < 1 ) {
                // newDate가 0보다 작으면 달을 줄인 뒤 전월의 마지막 날을 더해줌
                if ( newMonth < 0 ) {
                    newYear--;
                    newMonth = 11;
                } else {
                    newMonth--;
                }
                calendar.set(Calendar.YEAR, newYear);
                calendar.set(Calendar.MONTH, newMonth);
                int lastDateOfPrevMonth = calendar.getActualMaximum(Calendar.DATE);
                newDate += lastDateOfPrevMonth;
            }
        }

        // 설정된 newYear, newMonth, newDate를 바탕으로 날짜 배열을 생성
        int[][] daySevenAndYearAndMonth = getSevenDaysAndYearAndMonth(newYear, newMonth, newDate);

        // 날짜 배열, 년, 월을 반환
        return WeekCalendarFragment.newInstance(daySevenAndYearAndMonth[0], daySevenAndYearAndMonth[1][0], daySevenAndYearAndMonth[1][1]);
    }

    // 년,월,일을 입력받아 해당하는 주간 날짜 배열을 생성하는 메소드
    private int[][] getSevenDaysAndYearAndMonth(int year, int month, int date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year); // 입력 받은 연도로 년 설정.
        calendar.set(Calendar.MONTH, month); // 입력 받은 달로 월 설정.
        calendar.set(Calendar.DAY_OF_MONTH, date);

        // 입력받은 년,월,일로 달력을 설정한 뒤 요일을 구함
        // 만약 요일이 일요일이 아니면 일요일로 설정해줌
        // ex) 요일이 목요일(4)이면 (4-1=3)을 date로부터 빼줌
        // 이렇게 해서 date가 그 주의 일요일로 설정
        date -= calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int lastDate = calendar.getActualMaximum(Calendar.DATE);

        // date 1보다 작을 때에 대한 설정
        if ( date < 1 ) {
            if ( month == 0 ) { // 1월이면
                // year을 1 빼고 월을 12월로 설정
                year--;
                month = 11;
                calendar.set(Calendar.YEAR, year);
            } else { // 1월이 아니면 month에서 1을 뺌
                month--;
            }
            // 새롭게 설정된 year, month를 통해 Calendar를 설정한 후 date 얻음
            calendar.set(Calendar.MONTH, month);
            lastDate = calendar.getActualMaximum(Calendar.DATE);
            date += lastDate;
        }

        // 날짜 배열 초기화
        int[] daySeven = new int[7];
        for (int i=0; i<daySeven.length; i++) {
            int d = date + i;
            if ( d > lastDate ) {   // 날짜가 그 달의 마지막을 넘어설 경우 (30, 31, 32, ... 이런 식으로 커질 경우)
                // 그 달의 마지막을 빼줌
                d -= lastDate;
            }
            daySeven[i] = d;
        }

        // 배열과 연,월을 다 전달하기 위해 반환에 2차원 배열 형식 사용
        return new int[][] {
                daySeven,
                new int[] {
                        year,
                        month
                }
        };
    }


    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }
}
