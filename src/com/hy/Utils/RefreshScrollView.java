package com.hy.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class RefreshScrollView extends ScrollView {

	float[] location;
	TextView headView;// 对应下拉表示刷新的组件，需作为此scrollView组件的子组件
	ViewGroup.LayoutParams params;
	int initHeight;

	public RefreshScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public RefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RefreshScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if ((ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)
				&& location != null) {
			location = null;
			params.height = initHeight;
			headView.setLayoutParams(params);
		}

		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		// if (ev.getAction() == MotionEvent.ACTION_MOVE) {
		// if (this.getScrollY() == 0) {
		// if (location == null) {
		// location = new float[2];
		// location[0] = ev.getX();
		// location[1] = ev.getY();
		// headView =
		// (TextView)((LinearLayout)this.getChildAt(0)).getChildAt(0);
		// params = headView.getLayoutParams();
		// initHeight = params.height;
		//
		// }
		// float currentY = ev.getY();
		// if (currentY - location[1] > 0) {
		// params.height = initHeight + (int)(currentY-location[1])/2;
		// headView.setLayoutParams(params);
		// return true;
		// }
		// }else if (location != null && ev.getY() - location[1] < 0){
		// location = null;
		// if (headView.getLayoutParams().height != initHeight) {
		// ViewGroup.LayoutParams params = headView.getLayoutParams();
		// params.height = initHeight;
		// headView.setLayoutParams(params);
		// }
		// }
		// }
		return super.onTouchEvent(ev);
	}

}
