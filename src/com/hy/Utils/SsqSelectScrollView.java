package com.hy.Utils;

import com.hy.mylottery.LastestTenOpenCodeListView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class SsqSelectScrollView extends ScrollView {

	LastestTenOpenCodeListView tenListView;
	float[] downLocation = new float[2];
	boolean isTouchInListView = false;

	public SsqSelectScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SsqSelectScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SsqSelectScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (ev.getAction() == MotionEvent.ACTION_UP && isTouchInListView == true) {
			// 每一组touch事件都是一个新的开始，所以在touch up后，
			// 应该把isTouchInListView设置为false，以作用于下次touch事件
			isTouchInListView = false;
		}
		boolean result = super.dispatchTouchEvent(ev);
		return result;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (tenListView == null) {
			tenListView = (LastestTenOpenCodeListView) ((LinearLayout) (this.getChildAt(0))).getChildAt(0);
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// 判断down事件是否落在listView中
			float x = event.getX();
			float y = event.getY();
			downLocation[0] = x;
			downLocation[1] = y;
			float left = tenListView.getX();
			float top = tenListView.getY();
			ViewGroup.LayoutParams params = tenListView.getLayoutParams();
			float right = left + params.width;
			float bottom = top + params.height;
			if (y >= top && y < bottom) {
				// touch in tenListView
				isTouchInListView = true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// 这里不用设置，因为在dispatchTouch中已经做了设置。
			isTouchInListView = false;
		}

		if (isTouchInListView) {
			// 如果以上判断touch down落在ListView中，那么无论如何所有touch事件都由
			// listView消化，不会被scrollView拦截
			// 这里返回false，表明不拦截
			return false;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// 如果touch down没有落在listView中，那么所有touch move都由scroll View消化
			if (Math.abs(event.getY() - downLocation[1]) > 10) {
				// 这里判断相当于设置了move的灵敏度为10；
				return true;
			}
		}
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
