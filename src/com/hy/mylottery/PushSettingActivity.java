package com.hy.mylottery;

import java.util.HashMap;
import java.util.Iterator;

import com.hy.Utils.ResourceUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PushSettingActivity extends Activity {

	ListView mListView;
	LayoutInflater inflater;
	HashMap<String, String> mNeededPushList;
	Handler mHandler;
	Dialog refreshDialog;
	ImageView refreshImg;
	PushSettingReceiver receiver;
	Intent pushService;
	boolean isDialogBackKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.push_setting);
		inflater = getLayoutInflater();
		mNeededPushList = ResourceUtils.getNeededPushList(this);
		mHandler = new MHandler();
		mListView = (ListView) findViewById(R.id.push_list);
		mListView.setAdapter(new BaseAdapter() {
			HashMap<String, String> pushList = ResourceUtils.getInitPushList(getApplicationContext());
			String[] keys;

			class ConvertViewHolder {
				TextView lotteryName;
				ImageView pushOnOff;
			}

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.push_setting_cell, parent, false);
					ConvertViewHolder holder = new ConvertViewHolder();
					holder.lotteryName = (TextView) convertView.findViewById(R.id.push_setting_lottery_name);
					holder.pushOnOff = (ImageView) convertView.findViewById(R.id.push_setting_on_off);
					convertView.setTag(holder);
				}
				ConvertViewHolder holder = (ConvertViewHolder) convertView.getTag();
				holder.lotteryName.setText(pushList.get(getItem(position)));
				if (mNeededPushList == null || mNeededPushList.containsKey(getItem(position))) {
					holder.pushOnOff.setImageDrawable(getResources().getDrawable(R.drawable.more_on));
					holder.pushOnOff.setTag("PUSH_ON");
				} else {
					holder.pushOnOff.setImageDrawable(getResources().getDrawable(R.drawable.more_off));
					holder.pushOnOff.setTag("PUSH_OFF");
				}
				holder.pushOnOff.setId(position);
				holder.pushOnOff.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						showRefreshDialog();
						if (pushService == null) {
							pushService = new Intent("com.hy.mylottery.onelottery.PUSH_SERVICE");
							pushService.setPackage(getPackageName());
							pushService.putExtra("Modified", true);
						}

						if (v.getTag().toString().equals("PUSH_ON")) {
							((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.more_off));
							v.setTag("PUSH_OFF");
							mNeededPushList.remove(getItem(v.getId()));
							pushService.putExtra("RequestMethod", "REMOVE");
							pushService.putExtra("Key", getItem(v.getId()));
						} else if (v.getTag().toString().equals("PUSH_OFF")) {
							((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.more_on));
							v.setTag("PUSH_ON");
							mNeededPushList.put(getItem(v.getId()), pushList.get(getItem(v.getId())));
							pushService.putExtra("RequestMethod", "ADD");
							pushService.putExtra("Key", getItem(v.getId()));
							pushService.putExtra("Value", pushList.get(getItem(v.getId())));
						}
						new Thread() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								ResourceUtils.putNeededPushList(mNeededPushList);
								startService(pushService);
							}
						}.start();
					}
				});
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public String getItem(int position) {
				// TODO Auto-generated method stub
				if (keys == null) {
					Iterator<String> iterator = pushList.keySet().iterator();
					keys = new String[getCount()];
					int i = 0;
					while (iterator.hasNext()) {
						keys[i] = iterator.next();
						i++;
					}
				}
				return keys[position];
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return pushList == null ? 0 : pushList.size();
			}
		});

		receiver = new PushSettingReceiver();
		registerReceiver(receiver, new IntentFilter("com.hy.mylottery.onelottery.PUSH_SETTING_SERVER"));
	}

	public void back(View v) {
		onBackPressed();
	}

	public void showRefreshDialog() {
		if (refreshDialog == null) {
			View root = inflater.inflate(R.layout.push_setting_refresh, null);
			refreshImg = (ImageView) root.findViewById(R.id.push_setting_refresh_img);
			refreshDialog = new Dialog(PushSettingActivity.this);
			refreshDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			refreshDialog.setContentView(root);
			Window dialogWindow = refreshDialog.getWindow();
			WindowManager.LayoutParams params = dialogWindow.getAttributes();
			params.width = WindowManager.LayoutParams.WRAP_CONTENT;
			dialogWindow.setAttributes(params);
			refreshDialog.setCanceledOnTouchOutside(false);
			refreshDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					// if the dialog is showing and click back, mark cancel the
					// mission,
					// then dismiss the dialog and exit the activity(execute in
					// this activity's dispatchKeyEvent)
					dialog.dismiss();
					isDialogBackKey = true;
					return false;
				}
			});
		}
		refreshImg.startAnimation(AnimationUtils.loadAnimation(PushSettingActivity.this,
				R.anim.fragment_tab_notice_refresh_loading_anim));
		refreshDialog.show();
	}

	public void dismissRefreshDialog() {
		if (refreshDialog != null && refreshDialog.isShowing()) {
			refreshDialog.dismiss();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// if the back event is from dialog, exit the activity
			if (isDialogBackKey) {
				onBackPressed();
				isDialogBackKey = false;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (receiver != null) {
			unregisterReceiver(receiver);
		}
	}

	class MHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0x111:
				dismissRefreshDialog();
				break;

			default:
				break;
			}
		}
	}

	class PushSettingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(0x111);
		}
	}
}
