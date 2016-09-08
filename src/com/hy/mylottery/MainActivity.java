package com.hy.mylottery;

import com.hy.Utils.ResourceUtils;

import android.app.Activity;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	String[] navigationNames;
	int[] unpressedNavigationIcons;
	int[] pressedNavigationIcons;
	Fragment[] fragments = new Fragment[4];
	int showingPageId = -1;
	View showingNavigationItem;
	long previousBackTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initBottomNavigationBar(getInitShowPageId());
		startPushService();
	}

	public int getInitShowPageId() {
		Intent intent = getIntent();
		int initPageId;
		if (intent != null) {
			initPageId = intent.getIntExtra("ReceiveFragment", 0);
		} else {
			initPageId = 0;
		}
		return initPageId;
	}

	// initial bottom option navigation bar
	public void initBottomNavigationBar(int initShowingPageId) {
		navigationNames = ResourceUtils.getNavigationNames();
		unpressedNavigationIcons = ResourceUtils.getUnpressedNavigationIcons();
		pressedNavigationIcons = ResourceUtils.getPressedNavigationIcons();

		LinearLayout navigationContainer = (LinearLayout) findViewById(R.id.bottom_navigation_container);
		for (int i = 0; i < navigationNames.length; i++) {
			View rootView = getLayoutInflater().inflate(R.layout.lottery_tab, navigationContainer, false);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootView.getLayoutParams();
			params.weight = 1;
			rootView.setLayoutParams(params);

			ImageView navigationIcon = (ImageView) rootView.findViewById(R.id.tab_icon);
			navigationIcon.setImageResource(unpressedNavigationIcons[i]);
			TextView navigationName = (TextView) rootView.findViewById(R.id.tab_item);
			navigationName.setText(navigationNames[i]);
			navigationName.setTextColor(getResources().getColor(R.color.gray71));

			rootView.setId(i);// 每个item ID对应item所在位置，对应fragment id；
			rootView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					updateWindow(v);
				}
			});
			if (i == initShowingPageId) {
				// 初始显示画面
				updateWindow(rootView);
			}
			navigationContainer.addView(rootView);
		}
	}

	// 获取tab页的fragment的实例
	private Fragment getFragmentInstance(int position) {
		if (fragments[position] == null) {
			switch (position) {
			case 0:
				fragments[position] = new LotteryCellFragment(navigationNames[position]);
				break;
			case 1:
				fragments[position] = new TrendFragment(navigationNames[position]);
				break;
			case 2:
				fragments[position] = new NoticeFragment();
				break;
			case 3:
				fragments[position] = new UserFragment();
				break;
			default:
				Toast.makeText(this, "no such page!", Toast.LENGTH_SHORT).show();
				return null;
			}
		}
		return fragments[position];
	}

	// 使用对应tab的fragment更新界面
	private void updateWithFragment(Fragment fragment) {
		// getFragmentManager().beginTransaction()
		// .replace(R.id.init_comp, fragment)
		// .commit();
		if (fragment != null) {
			if (!fragment.isAdded()) {
				getFragmentManager().beginTransaction().add(R.id.init_comp, fragment).commit();
			} else {
				getFragmentManager().beginTransaction().show(fragment).commit();
			}
			if (showingPageId >= 0 && getFragmentInstance(showingPageId).isAdded()) {
				getFragmentManager().beginTransaction().hide(getFragmentInstance(showingPageId)).commit();
			}
		}
	}

	private void updateNavigationItemState(View v) {
		if (showingNavigationItem != null) {
			((TextView) showingNavigationItem.findViewById(R.id.tab_item))
					.setTextColor(getResources().getColor(R.color.gray71));
			;
			((ImageView) showingNavigationItem.findViewById(R.id.tab_icon))
					.setImageResource(unpressedNavigationIcons[showingNavigationItem.getId()]);
		}
		((TextView) v.findViewById(R.id.tab_item)).setTextColor(Color.YELLOW);
		;
		((ImageView) v.findViewById(R.id.tab_icon)).setImageResource(pressedNavigationIcons[v.getId()]);
		;
		showingNavigationItem = v;

	}

	public void updateWindow(View willShowNavigationItem) {
		int willShowPageId = willShowNavigationItem.getId();
		if (showingPageId != willShowPageId) {
			updateWithFragment(getFragmentInstance(willShowPageId));
			updateNavigationItemState(willShowNavigationItem);
			showingPageId = willShowPageId;
		}
	}

	// dispatch touch event，作用于输入法是否隐藏。
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View currentView = getCurrentFocus();
			if (isHideIme(currentView, ev)) {
				hideIme(currentView.getWindowToken());
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	/*
	 * 判断是否隐藏输入法 如果触摸的位置本身就是editText或之前获焦view本身就不是editText， 那么返回false
	 */
	private boolean isHideIme(View currentView, MotionEvent ev) {
		if (currentView != null && currentView instanceof EditText) {
			float x, y;
			x = ev.getX();
			y = ev.getY();
			int[] location = new int[2];
			currentView.getLocationInWindow(location);
			float viewOffsetX, viewOffsetY;
			viewOffsetX = location[0];
			viewOffsetY = location[1];
			int viewWidth, viewHeight;
			viewWidth = currentView.getWidth();
			viewHeight = currentView.getHeight();
			if (x >= viewOffsetX && x <= viewOffsetX + viewWidth && y >= viewOffsetY && y <= viewOffsetY + viewHeight) {
				return false;
			} else {
				currentView.clearFocus();
				return true;
			}
		}
		return false;
	}

	// 隐藏IME
	private void hideIme(IBinder token) {
		InputMethodManager imeManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imeManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		if (currentTime - previousBackTime < 1000) {
			super.onBackPressed();
		}
		previousBackTime = currentTime;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(0x123);// 0x123 is push message that exist newer open_code
	}

	public void startPushService() {
		Intent intent = new Intent("com.hy.mylottery.onelottery.PUSH_SERVICE");
		intent.setPackage(getPackageName());
		startService(intent);
	}
}
