package com.hansung.android.calendar2;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

public class WeekCalendarFragment extends Fragment implements CalendarFragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // 다른 fragment들과의 통신에서 'year', 'month' 파라미터를 주고 받기 때문에 ARG_PARAM1은 year, ARG_PARAM2는 month로 정의
    private static final String ARG_PARAM1 = "year";
    private static final String ARG_PARAM2 = "month";

    private int iYear;  // 입력받은 년
    private int iMonth; // 입력받은 월

    private DBHelper helper;
    private GridView gridView;
    private int position;

    // 주간 달력에서 현재 주의 날짜가 어떤 날들인지 저장하는 배열
    private int[] daySeven;
    // 일정 데이터가 들어갈 배열
    private ArrayList<Schedule> schedules;

    // 블록 선택 효과를 만들기 위해 정의한 객체
    // 블록이 선택되었을 때 이전에 선택된 블록의 배경색은 다시 흰색으로 바꿔야 하기 떄문에 이전에 선택된 블록을 이 prevBlock에 저장
    // prevBlock의 첫 값은 이 스케줄의 처음
    private TextView prevBlock;

    public WeekCalendarFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            MainActivity.fragment = WeekCalendarFragment.this;

            // 전달받은 인자들을 통해 iYear, iMonth 초기화
            iYear = getArguments().getInt(ARG_PARAM1);
            iMonth = getArguments().getInt(ARG_PARAM2);

            // 날짜 배열 초기화 및 day1 ~ day7 인자들을 통해 내용 채워 넣음
            daySeven = new int[7];
            for (int i=0; i<daySeven.length; i++) {
                daySeven[i] = getArguments().getInt("day" + (i+1));
            }

            helper = new DBHelper(getActivity().getApplicationContext(), "calendar.db", null, 1);
            schedules = helper.getSchedulesInDays(iYear, iMonth, daySeven);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // fragment_month_calendar 레이아웃을 가지는 View 객체 생성
        // fragment 내에서 findViewById 등의 액티비티 메소드를 이용하기 위함
        View v = inflater.inflate(R.layout.fragment_week_calendar, container, false);

        // 시간대, 스케줄을 둘 다 GridView를 통해 표현
        // schedules 배열을 바탕으로 ScheduleAdapter 어댑터 객체 생성
        // 스케줄 GridView에 ScheduleAdapter 어댑터를 설정
        // gridView는 fragment_week_calender의 schedule_grid
        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), schedules);
        gridView = v.findViewById(R.id.schedule_grid);
        gridView.setAdapter(scheduleAdapter);

        // TimeAdapter 어댑터 객체 생성
        // TimeAdapter에서는 자체적으로 시간대 배열(0~23의 숫자가 들어가있는 배열)을 생성하기 때문에 따로 파라미터로 배열을 넣어줄 필요가 없음
        // 시간대 GridView에 TimeAdapter 어댑터를 설정
        // gridView는 fragment_week_calender의 hour_grid
        TimeAdapter timeAdapter = new TimeAdapter(getActivity().getApplicationContext());
        GridView timeGridView = v.findViewById(R.id.hour_grid);
        timeGridView.setAdapter(timeAdapter);

        // 스케줄 블록을 선택할 경우 토스트 메시지를 띄우고 배경색을 CYAN으로 변경하는 이벤트 리스너 추가
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 배경색을 바꾸기 위해 눌린 view로부터 TextView 가져옴
                TextView textView = (TextView) view.findViewById(R.id.week_tv_item_gridview);
                // 가져온 TextView (눌린 TextView)의 배경색을 CYAN으로 설정
                textView.setBackgroundColor(Color.CYAN);
                // 이전에 선택된 블록의 배경색을 흰색으로 변경
                prevBlock.setBackgroundColor(Color.WHITE);
                // prevBlock을 방금 눌렸던 블록으로 설정
                prevBlock = textView;

//                int day = daySeven[position % 7];
//                int hour = position / 7;
//                int x = position % 7;
//                int y = position / 7;
//                String message = day + "일 " + hour + "시 / position=(" + x + "," + y + ")";

                DateInfo dateInfo = DateInfo.getInstance();
                dateInfo.setDate(daySeven[position % 7]);
                dateInfo.setHour(position / 7);
                if ( daySeven[0] > daySeven[position % 7] ) {
                    int newMonth = iMonth + 1;
                    if ( newMonth == 12 ) {
                        dateInfo.setMonth(0);
                        dateInfo.setYear(iYear + 1);
                    } else {
                        dateInfo.setMonth(newMonth);
                    }
                }

                // "position=(눌린 블록의 x좌표)" 형태로 출력
                Toast.makeText(getActivity(), "position=" + (position % 7), Toast.LENGTH_SHORT).show();

                WeekCalendarFragment.this.position = position;
            }
        });

        // 날짜들이 저장될 TextView 7개
        TextView[] textViews = {
                v.findViewById(R.id.week_1),
                v.findViewById(R.id.week_2),
                v.findViewById(R.id.week_3),
                v.findViewById(R.id.week_4),
                v.findViewById(R.id.week_5),
                v.findViewById(R.id.week_6),
                v.findViewById(R.id.week_7)
        };

        // 앱바 타이틀 변경
        ((MainActivity) getActivity()).setTitle(iYear + "년 " + (iMonth + 1) + "월");

        // 날짜를 선택했을 시 이전에 선택했던 블록의 배경색을 흰색으로 바뀌게 하기 위한 배열
        // 이벤트 리스너로 만들 익명 클래스에서는 final 타입의 변수만 이용 가능하기에, 1개짜리 배열 생성
        final TextView[] prevSelectedColumn = { textViews[0] };
        // 첫 날짜의 배경색을 CYAN으로 설정
        prevSelectedColumn[0].setBackgroundColor(Color.CYAN);

        for (int i=0; i<daySeven.length; i++) {
            final int idx = i;
            // 날짜 설정
            textViews[i].setText("" + daySeven[i]);
            // 날짜 클릭 이벤트 리스너 설정
            textViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // "position=(눌린 x좌표)" 형태의 토스트 메시지 출력
                    Toast.makeText(getActivity(), "position=" + idx, Toast.LENGTH_SHORT).show();
                    // 눌린 TextView의 배경색을 CYAN으로 설정
                    textViews[idx].setBackgroundColor(Color.CYAN);
                    // 이전에 선택된 블록의 색을 흰색으로 변경
                    prevSelectedColumn[0].setBackgroundColor(Color.WHITE);
                    // prevSelectedColumn의 값을 방금 눌린 블록으로 변경
                    prevSelectedColumn[0] = textViews[idx];
                }
            });
        }

//        return inflater.inflate(R.layout.fragment_month_calendar, container, false);
        return v;
    }

    @Override
    public void refresh() {
        schedules = helper.getSchedules("sch_year", iYear, "sch_month", iMonth);
        ScheduleAdapter gridAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), schedules, position);

        gridView.invalidateViews();
        gridView.setAdapter(gridAdapter); //그리드뷰에 어댑터설정.
    }

    // newInstance 메소드에서는 파라미터로 주간 달력의 날짜 7개, 년, 월 정보를 입력 받음
    public static WeekCalendarFragment newInstance(int[] daySeven, int year, int month) {
        // 새로운 WeekCalendarFragment fragment 객체 생성
        WeekCalendarFragment fragment = new WeekCalendarFragment();

        DateInfo.getInstance().set(year, month, daySeven[0], 0);

        Bundle args = new Bundle();
        // fragment에 아까 전달받은 날짜 배열을 day1, day2, ..., day7 형태로 전달
        for (int i=0; i<daySeven.length; i++) {
            args.putInt("day" + (i+1), daySeven[i]);
        }
        // fragment에 year, month를 전달
        args.putInt("year", year);
        args.putInt("month", month);
        fragment.setArguments(args);
        return fragment;
    }

    // 스케줄 GridView의 데이터를 채워넣기 위한 GridAdapter 내부클래스
    private class ScheduleAdapter extends BaseAdapter {

        // 블록에 적힐 텍스트 배열
        private final String[] labels;
        // 스케줄 데이터 리스트
        private final ArrayList<Schedule> schedules;
        // LayoutInflater 객체 생성.
        private final LayoutInflater inflater;
        // 미리 눌려있을 블록 위치
        private int selected;

        public ScheduleAdapter(Context context, ArrayList<Schedule> schedules) {
            // labels 배열 초기화 및 공백으로 채워넣음
            // labels 배열은 일주일에 각 24시간씩의 스케줄이 있으므로 7*24개
            labels = new String[7 * 24];
            Arrays.fill(labels, "");
            // 매개변수로 입력 받은 schedules로 schedules 리스트 초기화.
            this.schedules = schedules;
            //context에서 LayoutInflater 가져옴.
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 0번째 블록 미리 눌려있음
            this.selected = 0;
        }

        public ScheduleAdapter(Context context, ArrayList<Schedule> schedules, int selected) {
            // labels 배열 초기화 및 공백으로 채워넣음
            // labels 배열은 일주일에 각 24시간씩의 스케줄이 있으므로 7*24개
            labels = new String[7 * 24];
            Arrays.fill(labels, "");
            // 매개변수로 입력 받은 schedules로 schedules 리스트 초기화.
            this.schedules = schedules;
            //context에서 LayoutInflater 가져옴.
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // selected 변수 초기화
            this.selected = selected;
        }

        // 배열 크기 반환
        @Override
        public int getCount() {
            return labels.length;
        }

        // 배열 position번째의 원소 반환
        @Override
        public String getItem(int position) {
            return labels[position];
        }

        // 몇번째인지 반환
        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // convertView는 그리드의 한 블락(하나의 뷰)
            // calendar_week_gridview.xml파일을 View객체로 만들어서 반환.
            if ( convertView == null ) convertView = inflater.inflate(R.layout.calendar_week_gridview, null);

            // schedules 리스트로부터 블록의 날짜, 시간에 해당하는 일정을 labels[position]에 더함
            for (Schedule s : schedules) {
                if ( s.date == daySeven[position % 7] && s.startHour == position / 7 ) {
                    // 여러 일정이 있을 경우 \n으로 구분
                    labels[position] += s.title + '\n';
                }
            }
            // trim을 통해 마지막의 \n 제거
            labels[position] = labels[position].trim();

            // calendar_week_gridview 레이아웃 안의 week_tv_item_gridview TextView를 가져옴
            TextView textView = (TextView) convertView.findViewById(R.id.week_tv_item_gridview);
            // 그 TextView의 글자를 labels 배열의 원소로 설정
            textView.setText(labels[position]);
            if ( position == selected ) {  // 처음의 데이터를 넣는 중일 경우 배경색을 CYAN으로 설정 및 prevBlock에 이 블록 저장
                textView.setBackgroundColor(Color.CYAN);
                prevBlock = textView;
            }
            // 설정된 convertView 반환
            return convertView;
        }
    }

    // 시간대 GridView의 데이터를 채워넣기 위한 GridAdapter 내부클래스
    private class TimeAdapter extends BaseAdapter {

        // 시간대 데이터 배열
        private final String[] times;
        // LayoutInflater 객체 생성.
        private final LayoutInflater inflater;

        public TimeAdapter(Context context) {
            // 시간대 배열 직접 설정 (0~23의 숫자 입력)
            this.times = new String[24];
            for (int i=0; i<times.length; i++) times[i] = "" + i;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return times.length;
        }

        @Override
        public String getItem(int position) {
            return times[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            // convertView는 그리드의 한 블락(하나의 뷰)
            // schedule_time_gridview.xml파일을 View객체로 만들어서 반환.
            if ( convertView == null ) convertView = inflater.inflate(R.layout.schedule_time_gridview, null);
            // schedule_time_gridview 레이아웃 안의 schedule_time_gridview TextView를 가져옴
            TextView textView = (TextView) convertView.findViewById(R.id.schedule_time);
            // TextView를 해당 시간대로 설정
            textView.setText(times[position]);
            return convertView;
        }
    }


}
