package com.hansung.android.calendar2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;

import static java.lang.Math.abs;

// ViewPager2를 통해 페이지가 스와이프 될 때마다 일어날 행동에 대한 정의를 담당하는 어댑터
// 왼쪽으로 스와이프 하면 한 달 전, 오른쪽으로 스와이프 하면 한 달 후
public class MonthVIewCalendarAdapter extends FragmentStateAdapter {
    // 전체 스와이프 가능한 횟수
    private static final int NUM_ITEMS = 100;

    private int iYear;  // 앱이 실행된 날짜의 년
    private int iMonth; // 앱이 실행된 날짜의 월

    public MonthVIewCalendarAdapter(@NonNull Fragment fragment) {
        super(fragment);

        // 자바 Calendar 클래스를 통해 iYear와 iMonth를 현재 년,월로 초기화
        Calendar calendar = Calendar.getInstance();
        iYear = calendar.get(Calendar.YEAR);
        iMonth = calendar.get(Calendar.MONTH);

    }

    @NonNull
    @Override
    public MonthCalendarFragment createFragment(int position) {

        // 시작 포지션이 50페이지 이므로 position 값에서 50을 빼줌
        position -= NUM_ITEMS / 2;

        // i는 계산을 위해 필요한 변수
        // 현재 월에 페이지 번호를 더함
        // ex) 현재가 5월이고 페이지를 오른쪽으로 10번 넘겼으면 i는 15
        int i = iMonth + position;
        // 스와이프 된 페이지의 년
        int newYear;
        // 스와이프 된 페이지의 월
        int newMonth;

        if ( (i % 12) < 0 ) {   // 페이지를 왼쪽으로 넘겼을 때 (i가 음수일 때)
            // 년도를 그만큼 빼줌
            newYear = iYear + (i / 12) - 1;
            //
            newMonth = 12 + (i % 12);

        } else {    // 넘긴 페이지가 1월이 아닐 때
            // 년이 늘어났으면 그만큼 년을 더해줌
            newYear = iYear + (i / 12);
            // 달을 0~11 사이의 값으로 설정
            newMonth = i % 12;
        }

        // MonthCalendarFragment의 newInstance 메소드를 통해 newYear년 newMonth월에 해당하는 fragment를 생성하여 반환
        return MonthCalendarFragment.newInstance(newYear, newMonth);
    }

    // 전체 페이지 수
    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }


}
