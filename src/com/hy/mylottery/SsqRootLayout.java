package com.hy.mylottery;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SsqRootLayout extends RelativeLayout {

	int[] stamp;

	public SsqRootLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SsqRootLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SsqRootLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean fitSystemWindows(Rect insets) {
		// TODO Auto-generated method stub
		if (stamp == null) {
			stamp = new int[4];
			stamp[0] = insets.top;
			stamp[1] = insets.left;
			stamp[2] = insets.right;
			stamp[3] = insets.bottom;
		}
		// System.out.println("insets top is " + insets.top);
		// System.out.println("insets left is " + insets.left);
		// System.out.println("insets right is " + insets.right);
		// System.out.println("insets bottom is " + insets.bottom);
		// System.out.println("sdk version is " + Build.VERSION.SDK_INT);
		// if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
		Rect mInsets = new Rect(insets);
		mInsets.top = 0;
		return super.fitSystemWindows(mInsets);
		// }
		//
		// return super.fitSystemWindows(insets);
	}

}
