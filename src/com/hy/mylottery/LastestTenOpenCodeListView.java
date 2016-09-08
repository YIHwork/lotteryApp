package com.hy.mylottery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

public class LastestTenOpenCodeListView extends ListView {

	public LastestTenOpenCodeListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public LastestTenOpenCodeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public LastestTenOpenCodeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addHeaderView(View v) {
		// TODO Auto-generated method stub

		v.setVisibility(View.GONE);
		// add parent for each header
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		FrameLayout frame = new FrameLayout(getContext());
		frame.setLayoutParams(params);
		frame.addView(v);
		super.addHeaderView(frame);
	}

	@Override
	public void addFooterView(View v) {
		// TODO Auto-generated method stub
		v.setVisibility(View.GONE);
		// add parent for each footer
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		LinearLayout linear = new LinearLayout(getContext());
		linear.setLayoutParams(params);
		linear.setGravity(Gravity.CENTER);
		linear.addView(v);
		super.addFooterView(linear);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		boolean result = super.dispatchTouchEvent(ev);
		return result;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		boolean result = super.onInterceptTouchEvent(event);
		return result;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		boolean result = super.onTouchEvent(ev);
		return result;
	}
}
