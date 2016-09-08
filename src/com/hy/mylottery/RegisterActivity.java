package com.hy.mylottery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	boolean isLocalAcount = true;
	boolean isHidePasswd = true;
	TextView titleName;
	View accountComponent;
	EditText usernameView;
	EditText passwdView;
	Button registerBtn;
	ImageView usernameClear;
	ImageView passwdClear;
	ImageView passwdType;

	OnClickListener mClickListener;
	OnFocusChangeListener mFocusChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		titleName = (TextView) findViewById(R.id.register_title_name);
		accountComponent = findViewById(R.id.account_component);
		usernameView = (EditText) findViewById(R.id.register_username);
		passwdView = (EditText) findViewById(R.id.register_passwd);
		registerBtn = (Button) findViewById(R.id.register_btn);
		usernameClear = (ImageView) findViewById(R.id.username_clear);
		passwdClear = (ImageView) findViewById(R.id.passwd_clear);
		passwdType = (ImageView) findViewById(R.id.is_passwd_type);

		mClickListener = getClickListener();
		mFocusChangeListener = getFocusChangeListener();

		initData();

		usernameView.addTextChangedListener(getTextWatcher(usernameView));
		usernameView.setOnFocusChangeListener(mFocusChangeListener);
		passwdView.addTextChangedListener(getTextWatcher(passwdView));
		passwdView.setOnFocusChangeListener(mFocusChangeListener);

		registerBtn.setOnClickListener(mClickListener);
		usernameClear.setOnClickListener(mClickListener);
		passwdClear.setOnClickListener(mClickListener);
		passwdType.setOnClickListener(mClickListener);
	}

	public void initData() {
		Intent intent = getIntent();
		if (intent == null) {
			System.exit(0);
		}
		Bundle data = intent.getBundleExtra("Data");
		if (data != null) {
			if (data.getBoolean("isLocalAccount")) {
				accountComponent.setVisibility(View.GONE);
				isLocalAcount = true;
			} else {
				accountComponent.setVisibility(View.VISIBLE);
				isLocalAcount = false;
			}
			titleName.setText(data.getCharSequence("AccountType", "注册"));
		}
	}

	public TextWatcher getTextWatcher(final View v) {
		return new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.register_username:
					if (s == null || s.toString().length() <= 0) {
						usernameClear.setVisibility(View.GONE);
					} else {
						usernameClear.setVisibility(View.VISIBLE);
					}
					break;

				case R.id.register_passwd:
					if (s == null || s.toString().length() <= 0) {
						passwdClear.setVisibility(View.GONE);
					} else {
						passwdClear.setVisibility(View.VISIBLE);
					}
					break;
				default:
					break;
				}

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
			}
		};
	}

	public OnFocusChangeListener getFocusChangeListener() {
		return new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.register_username:
					if (hasFocus && (((EditText) v).getText() != null && ((EditText) v).getText().length() >= 1)) {
						usernameClear.setVisibility(View.VISIBLE);
					} else {
						usernameClear.setVisibility(View.GONE);
					}
					break;

				case R.id.register_passwd:
					if (hasFocus && (((EditText) v).getText() != null && ((EditText) v).getText().length() >= 1)) {
						passwdClear.setVisibility(View.VISIBLE);
					} else {
						passwdClear.setVisibility(View.GONE);
					}
					break;

				default:
					break;
				}
			}
		};
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

	public OnClickListener getClickListener() {
		return new OnClickListener() {
			InputMethodManager imm = (InputMethodManager) getApplicationContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.register_btn:
					CharSequence username = usernameView.getText();
					CharSequence passwd = passwdView.getText();
					if (((username == null || username.length() < 1) && !isLocalAcount) || passwd == null
							|| passwd.length() < 6) {
						Toast.makeText(getApplicationContext(), "请输入有效长度的字符", Toast.LENGTH_SHORT).show();
					} else {
						requestRegister();
					}
					break;

				case R.id.username_clear:
					usernameView.setText("");
					if (usernameView.isFocused()) {
						imm.showSoftInput(usernameView, 0);
					}
					break;

				case R.id.passwd_clear:
					passwdView.setText("");
					if (passwdView.isFocused()) {
						imm.showSoftInput(passwdView, 0);
					}
					break;

				case R.id.is_passwd_type:
					if (isHidePasswd) {
						passwdType.setImageDrawable(getResources().getDrawable(R.drawable.usercenter_pwd_pressed));
						setHidePasswd(passwdView, false);
					} else {
						passwdType.setImageDrawable(getResources().getDrawable(R.drawable.usercenter_pwd));
						setHidePasswd(passwdView, true);
					}
					if (!passwdView.isFocused()) {
						passwdView.requestFocus();
					}
					imm.showSoftInput(passwdView, 0);
					passwdView.setSelection(passwdView.length());
					break;
				default:
					break;
				}
			}
		};
	}

	private void requestRegister() {
		CharSequence username = usernameView.getText();
		CharSequence passwd = passwdView.getText();
		// need continue to code
	}

	public void back(View v) {
		this.onBackPressed();
	}
}
