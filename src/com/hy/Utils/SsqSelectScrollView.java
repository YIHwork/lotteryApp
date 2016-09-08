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
			// ÿһ��touch�¼�����һ���µĿ�ʼ��������touch up��
			// Ӧ�ð�isTouchInListView����Ϊfalse�����������´�touch�¼�
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
			// �ж�down�¼��Ƿ�����listView��
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
			// ���ﲻ�����ã���Ϊ��dispatchTouch���Ѿ��������á�
			isTouchInListView = false;
		}

		if (isTouchInListView) {
			// ��������ж�touch down����ListView�У���ô�����������touch�¼�����
			// listView���������ᱻscrollView����
			// ���ﷵ��false������������
			return false;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// ���touch downû������listView�У���ô����touch move����scroll View����
			if (Math.abs(event.getY() - downLocation[1]) > 10) {
				// �����ж��൱��������move��������Ϊ10��
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
