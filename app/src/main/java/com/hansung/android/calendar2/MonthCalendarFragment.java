package com.hansung.android.calendar2;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthCalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthCalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // 다른 fragment들과의 통신에서 'year', 'month' 파라미터를 주고 받기 때문에 ARG_PARAM1은 year, ARG_PARAM2는 month로 정의
    private static final String ARG_PARAM1 = "year";
    private static final String ARG_PARAM2 = "month";

    // TODO: Rename and change types of parameters
    private int iYear;      // 입력받은 년
    private int iMonth;     // 입력받은 월
    private int firstDay;   // 그 달의 첫날의 요일
    private int totDays;    // 그 달의 마지막 날

    private String[] days;  // 달력 데이터가 들어갈 배열

    // 블록 선택 효과를 만들기 위해 정의한 객체
    // 각 블록은 LinearLayout 안에 TextView로 정의되어 있기 때문에, 블록 선택 시에는 TextView를 선택
    // 블록이 선택되었을 때 이전에 선택된 블록의 배경색은 다시 흰색으로 바꿔야 하기 떄문에 이전에 선택된 블록을 이 prevBlock에 저장
    // prevBlock의 첫 값은 이 달의 첫날
//    private TextView prevBlock;
    private LinearLayout prevBlock;

    public MonthCalendarFragment() {
        // Required empty public constructor
    }

    /**
     * MonthViewCalendarAdapter에서 달력 생성을 위해 불러오는 static 메소드
     *
     * @param year 이 fragment에서 만들 달력의 년
     * @param month 이 fragment에서 만들 달력의 월
     * @return 입력받은 year, month를 바탕으로 만든 달력이 띄워진 fragment
     */
    // TODO: Rename and change types and number of parameters
    public static MonthCalendarFragment newInstance(int year, int month) {
        // 새로운 MonthCalendarFragment fragment 생성
        MonthCalendarFragment fragment = new MonthCalendarFragment();

        DateInfo.getInstance().set(year, month, 1);

        // 생성한 fragment에 파라미터로 입력받았던 year와 month를 전달
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, year);
        args.putInt(ARG_PARAM2, month);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // 달력 데이터 배열은 총 42개 원소로 구성
            days = new String[7 * 6];
            // 전달받은 year와 month를 통해 iYear, iMonth 초기화
            iYear = getArguments().getInt(ARG_PARAM1);
            iMonth = getArguments().getInt(ARG_PARAM2);

            ((MainActivity) getActivity()).setTitle(iYear + "년 " + (iMonth + 1) + "월"); // 앱바 타이틀 설정

            setCalendar(iYear, iMonth); //mYear와 mMonth를 바탕으로 days에 데이터를 채움.
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // fragment_month_calendar 레이아웃을 가지는 View 객체 생성
        // fragment 내에서 findViewById 등의 액티비티 메소드를 이용하기 위함
        View v = inflater.inflate(R.layout.fragment_month_calendar, container, false);

        //그리드 어댑터에 날짜 배열(days)을 넣어줌.
        GridAdapter gridAdapter = new GridAdapter(getActivity().getApplicationContext(), days);
        GridView gridView = v.findViewById(R.id.gridview);
        gridView.setAdapter(gridAdapter); //그리드뷰에 어댑터설정.

        // 월간 달력의 블록을 선택할 경우 토스트 메시지를 띄우고 배경색을 CYAN으로 변경하는 이벤트 리스너 추가
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 첫 날 이상 마지막 날 이하일 경우 토스트메시지 발생.
                if ((0 < position-firstDay+1) && (position-firstDay+1)<(firstDay+totDays)) {
                    // ex) 2021/4/21
                    final int date = position - firstDay + 1;
                    Toast.makeText(getActivity(), (iYear) + "." + (iMonth + 1) + "." + date + "일", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).setTitle(iYear + "년 " + (iMonth + 1) + "월");

                    DateInfo.getInstance().setDate(date);

                    // 배경색을 바꾸기 위해 눌린 view로부터 TextView 가져옴
//                    TextView textView = (TextView) view.findViewById(R.id.tv_item_gridview);
                    LinearLayout layout = view.findViewById(R.id.tv_layout);
                    // 배경색 변경
//                    textView.setBackgroundColor(Color.CYAN);
                    layout.setBackgroundColor(Color.CYAN);

                    // 이전에 눌렸던 블록의 배경색을 흰색으로 변경
                    prevBlock.setBackgroundColor(Color.WHITE);
                    // 현재 눌린 블록을 prevBlock으로 설정
                    prevBlock = layout;
                }
            }
        });

        return v;
    }

    // 달력 GridView의 데이터를 채워넣기 위한 GridAdapter 내부클래스
    private class GridAdapter extends BaseAdapter {

        // 달력 데이터 배열
        private final String[] days;
        // LayoutInflater 객체 생성.
        private final LayoutInflater inflater;

        public GridAdapter(Context context, String[] days) {
            // 매개변수로 입력 받은 days로 days 배열 초기화.
            this.days = days;
            //context에서 LayoutInflater 가져옴.
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        // 배열 크기 반환
        @Override
        public int getCount() {
            return days.length;
        }

        // 배열 position번째의 원소 반환
        @Override
        public String getItem(int position) {
            return days[position];
        }

        // 몇번째인지 반환
        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // convertView는 그리드의 한 블락(하나의 뷰)
            if ( convertView == null ) {
                // calendar_gridview.xml파일을 View객체로 만들어서 반환.
                convertView = inflater.inflate(R.layout.calendar_gridview, null);
            }
            // calendar_gridview 레이아웃 안의 tv_item_gridview TextView를 가져옴
            TextView textView = (TextView) convertView.findViewById(R.id.tv_item_gridview);
            // gridview의 블록에 해당하는 레이아웃을 가져옴
            LinearLayout layout = convertView.findViewById(R.id.tv_layout);
            // 그 TextView의 글자를 days 배열의 원소로 설정
            textView.setText(days[position]);
            if ( position == firstDay ) {   // 첫날의 데이터를 넣는 중일 경우 배경색을 CYAN으로 설정 및 prevBlock에 이 블록 저장
//                textView.setBackgroundColor(Color.CYAN);
                layout.setBackgroundColor(Color.CYAN);
                prevBlock = layout;
            }

            // 설정된 convertView 반환
            return convertView;
        }
    }

    // 입력 받은 연도와 월을 바탕으로 dayList 설정.(데이터 채워 넣기)
    private void setCalendar(int year, int month){

        //첫날이 무슨요일인지, 마지막이 30,31일인지
        Calendar calendar = Calendar.getInstance(); //calendar 객체 생성.
        calendar.set(Calendar.YEAR, year); // 입력 받은 연도로 년 설정.
        calendar.set(Calendar.MONTH, month); // 입력 받은 달로 월 설정.
        calendar.set(Calendar.DAY_OF_MONTH, 1); // firstDay를 만들기 위해 입력받은 년, 월에 대한 일자를 1로 설정한다.
        firstDay = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 첫날이 무슨 요일인지. DAY_OF_WEEK는 일:1, 월:2, 화:3, ..., 토:7인데 convertView나 dayList 등이 0부터 시작하기 떄문에 -1을 해준다.
        totDays = calendar.getActualMaximum(Calendar.DATE); // 마지막날은 그 달의 최대값 반환.

        //이 두 정보를 갖고 리스트 만드려고 한다.
        for (int i=0; i<days.length; i++) {
            if ( i < firstDay || i > (totDays + firstDay - 1) ) days[i] = ""; // 첫날보다 작거나, 마지막날보다 크면 공백으로 채운다.
            else days[i] = Integer.toString(i - firstDay + 1); // 그게 아니면 해당하는 날짜에 일자를 입력. 1~마지막날까지를 말함.
        }

    }

}
