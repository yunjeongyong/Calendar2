package com.hansung.android.calendar2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

/**
 * 두 GridView의 스크롤을 동시에 하기 위해 GridView 클래스를 상속하여 새로 정의한 클래스
 *
 * 주간 달력에서 시간(0~23)을 나타내는 GridView와 일정을 나타내는 GridView를 동시에 스크롤 하기 위함
 *
 * */
public class FixedGridView extends GridView {

    public FixedGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FixedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedGridView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
