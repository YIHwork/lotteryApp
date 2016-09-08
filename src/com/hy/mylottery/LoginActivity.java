package com.hy.mylottery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	boolean isHidePasswd = true;
	boolean isLocalAccount = true;
	EditText usernmText;
	EditText passwdText;
	TextView registerBtn;
	Button loginBtn;
	TextView forgetPasswd;
	TextView accountTypeSelector;
	PopupWindow accountTypePopup;
	TextView accountTypePopupShowingBg;
	PopupMenu registerPopup;

	String[] accountTypes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		usernmText = (EditText) findViewById(R.id.username);
		passwdText = (EditText) findViewById(R.id.password);
		passwdText.setOnTouchListener(new OnTouchListener() {
			// 主要用于点击EditText图片时的response
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_UP) {
					// v.performClick();
					float x = event.getX();
					int vWidth = v.getWidth();

					Drawable[] drawable = ((EditText) v).getCompoundDrawables();
					int endDrawableWidth = drawable[2].getIntrinsicWidth();
					// int endDrawableHeight = drawable[2].getIntrinsicHeight();
					int paddingRight = v.getPaddingRight();
					if (x >= vWidth - endDrawableWidth - paddingRight && x <= vWidth) {
						if (isHidePasswd) {
							((EditText) v).setCompoundDrawablesWithIntrinsicBounds(0, 0,
									R.drawable.usercenter_pwd_pressed, 0);
							setHidePasswd((EditText) v, false);
						} else {
							((EditText) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.usercenter_pwd, 0);
							setHidePasswd((EditText) v, true);
						}
					}
				}
				return false;
			}
		});

		registerBtn = (TextView) findViewById(R.id.register);
		registerBtn.setOnClickListener(this);
		loginBtn = (Button) findViewById(R.id.login);
		loginBtn.setOnClickListener(this);
		forgetPasswd = (TextView) findViewById(R.id.forget_passwd);
		forgetPasswd.setOnClickListener(this);

		accountTypeSelector = (TextView) findViewById(R.id.account_type_selector);
		accountTypeSelector.setOnClickListener(this);
		initData();
	}

	// 改变password EditText的输入方式
	private void setHidePasswd(EditText editText, boolean isHidePasswd) {
		this.isHidePasswd = isHidePasswd;
		if (isHidePasswd) {
			editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		} else {
			editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			if (accountTypePopup != null && accountTypePopup.isShowing()) {
				int[] popupLocation = new int[2];
				int[] selectorLocation = new int[2];
				accountTypePopup.getContentView().getLocationOnScreen(popupLocation);
				accountTypeSelector.getLocationOnScreen(selectorLocation);
				float x = ev.getX();
				float y = ev.getY();
				if (!((x >= popupLocation[0] && x <= popupLocation[0] + accountTypePopup.getWidth()
						&& y >= popupLocation[1] && y <= popupLocation[1] + accountTypePopup.getHeight())
						|| (x >= selectorLocation[0]
								&& x <= selectorLocation[0] + accountTypeSelector.getMeasuredWidth()
								&& y >= selectorLocation[1]
								&& y <= selectorLocation[1] + accountTypeSelector.getMeasuredHeight()))) {
					accountTypePopup.dismiss();
					return true;
				}
			} else {
				View currentView = getCurrentFocus();
				if (isHideIme(currentView, ev)) {
					hideIme(currentView.getWindowToken());
					// return true;
				}
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
				// currentView.clearFocus();
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

	public void back(View v) {
		this.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.account_type_selector:
			if (accountTypePopup != null && accountTypePopup.isShowing()) {
				accountTypePopup.dismiss();
			} else {
				showAccountTypePopup(v);
			}
			break;
		case R.id.login:
			String username = usernmText.getText().toString();
			String passwd = passwdText.getText().toString();
			if ((username == null || username.length() <= 0) || (passwd == null || passwd.length() < 6)) {
				Toast.makeText(this, "请输入有效长度字符", Toast.LENGTH_SHORT).show();
			} else {
				requestLogin();
			}
			break;

		case R.id.forget_passwd:

			break;

		case R.id.register:
			showRegisterPopup(v);
			break;
		default:
			break;
		}
	}

	public void showAccountTypePopup(View v) {
		if (accountTypePopup == null) {
			View rootView = getLayoutInflater().inflate(R.layout.account_type_selector_popup, null);
			ListView list = (ListView) rootView.findViewById(R.id.account_type_selector_popup_list);
			list.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					isLocalAccount = position == 0;
					updateComponentData();
					accountTypePopup.dismiss();
				}
			});
			accountTypePopup = new PopupWindow(this);
			accountTypePopup.setContentView(rootView);
			accountTypePopup.setWidth(v.getMeasuredWidth() + 40);
			accountTypePopup.setHeight(LayoutParams.WRAP_CONTENT);
			accountTypePopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.user_component_bg_border));
			// locationPopup.setOutsideTouchable(true);
			accountTypePopupShowingBg = (TextView) findViewById(R.id.account_type_popup_show_bg);
			accountTypePopup.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss() {
					// TODO Auto-generated method stub
					if (accountTypePopupShowingBg != null && accountTypePopupShowingBg.getVisibility() != View.GONE) {
						accountTypePopupShowingBg.setVisibility(View.GONE);
					}
					accountTypeSelector.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
							R.drawable.title_location_unclick, 0);
				}
			});
		}
		accountTypePopup.showAsDropDown(v, -20, 10, Gravity.CENTER_HORIZONTAL);
		accountTypePopupShowingBg.setVisibility(View.VISIBLE);
		accountTypeSelector.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.title_location_click, 0);
	}

	public void showRegisterPopup(View v) {
		if (registerPopup == null) {
			registerPopup = new PopupMenu(this, v);
			registerPopup.inflate(R.menu.user_register_menu);
			registerPopup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					// TODO Auto-generated method stub
					switch (item.getItemId()) {
					case R.id.local_account:
						Intent intent01 = new Intent(getApplicationContext(), RegisterActivity.class);
						Bundle data01 = new Bundle();
						data01.putBoolean("isLocalAccount", true);
						data01.putCharSequence("AccountType", "注册" + item.getTitle());
						intent01.putExtra("Data", data01);
						startActivityForResult(intent01, 22);
						break;

					case R.id.remote_account:
						Intent intent02 = new Intent(getApplicationContext(), RegisterActivity.class);
						Bundle data02 = new Bundle();
						data02.putBoolean("isLocalAccount", false);
						data02.putCharSequence("AccountType", "注册" + item.getTitle());
						intent02.putExtra("Data", data02);
						startActivityForResult(intent02, 22);
						break;

					default:
						break;
					}
					return false;
				}
			});
		}
		registerPopup.show();
	}

	public void initData() {
		isLocalAccount = true;
		updateComponentData();
	}

	public void updateComponentData() {
		if (accountTypes == null) {
			accountTypes = getResources().getStringArray(R.array.account_type);
		}

		if (isLocalAccount) {
			accountTypeSelector.setText(accountTypes[0]);
			usernmText.setText(accountTypes[0]);
			usernmText.setFocusable(false);
			// usernmText.setClickable(false);
			usernmText.setTextColor(getResources().getColor(R.color.dimGrey));
			usernmText.setBackground(getResources().getDrawable(R.drawable.editor_username_bg_border_unfocusable));
		} else {
			accountTypeSelector.setText(accountTypes[1]);
			usernmText.setText(null);
			usernmText.setFocusable(true);
			usernmText.setFocusableInTouchMode(true);
			usernmText.requestFocus();
			// usernmText.setClickable(true);
			usernmText.setTextColor(getResources().getColor(R.color.black));
			usernmText.setBackground(getResources().getDrawable(R.drawable.editor_username_bg_border));
		}
	}

	public void requestLogin() {
		String username = usernmText.getText().toString();
		String passwd = passwdText.getText().toString();
		if (isLocalAccount) {

		} else {

		}
	}
}
