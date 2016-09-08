package com.hy.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class UserExpandableListView extends ExpandableListView {

	public UserExpandableListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public UserExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public UserExpandableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		int expandHeightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandHeightSpec);
	}
}
