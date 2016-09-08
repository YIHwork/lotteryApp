package com.hy.mylottery;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity implements OnClickListener {

	ImageView backBtn;
	TextView noticeBtn;
	TextView feedbackBtn;
	TextView helpBtn;
	TextView aboutBtn;
	View checkBtn;
	TextView versionView;
	Dialog aboutDialog;
	TextView versionInDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
		backBtn = (ImageView) findViewById(R.id.back_btn);
		noticeBtn = (TextView) findViewById(R.id.notice_setting);
		feedbackBtn = (TextView) findViewById(R.id.feedback);
		helpBtn = (TextView) findViewById(R.id.help);
		aboutBtn = (TextView) findViewById(R.id.about);
		checkBtn = findViewById(R.id.check);
		versionView = (TextView) findViewById(R.id.version);
		versionView.setText(getAppVersion());

		backBtn.setOnClickListener(this);
		noticeBtn.setOnClickListener(this);
		feedbackBtn.setOnClickListener(this);
		helpBtn.setOnClickListener(this);
		aboutBtn.setOnClickListener(this);
		checkBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn:
			this.onBackPressed();
			break;

		case R.id.notice_setting:
			Intent intent = new Intent(this, PushSettingActivity.class);
			startActivity(intent);
			break;

		case R.id.feedback:
			Toast.makeText(this, "Sorry,拒绝接受任何意见！", Toast.LENGTH_SHORT).show();
			break;

		case R.id.help:
			Toast.makeText(this, "免费App，随便操作。", Toast.LENGTH_SHORT).show();
			break;

		case R.id.about:
			showAboutDialog();
			break;

		case R.id.check:
			break;

		default:
			break;
		}
	}

	public void showAboutDialog() {
		if (aboutDialog == null) {
			View rootView = getLayoutInflater().inflate(R.layout.about_dialog, null);
			versionInDialog = (TextView) rootView.findViewById(R.id.current_version);
			aboutDialog = new Dialog(this);
			// aboutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			aboutDialog.setContentView(rootView);
			aboutDialog.setTitle("关于一张彩票");
			aboutDialog.setCanceledOnTouchOutside(true);
		}
		versionInDialog.setText(getAppVersion());
		aboutDialog.show();
	}

	public String getAppVersion() {
		String version = "";
		PackageManager pm = getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			version = "v" + info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}
}
