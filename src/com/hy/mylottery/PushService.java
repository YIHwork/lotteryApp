package com.hy.mylottery;

import java.util.Calendar;
import java.util.HashMap;

import com.hy.Utils.ResourceUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class PushService extends Service {

	MBinder mBinder;
	NotificationManager nm;
	final int EXIST_UPDATE = 0x123;
	AlarmManager alarmManager;
	PendingIntent pi;
	HashMap<String, String> mNeededPushList;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mBinder = new MBinder();
		mNeededPushList = ResourceUtils.getNeededPushList(this);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent("com.hy.mylottery.onelottery.PUSH_SERVICE");
		intent.putExtra("Alarm", true);
		pi = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 21);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pi);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (mNeededPushList == null) {
			stopSelf();
		} else {
			if (intent == null) {
				return super.onStartCommand(intent, flags, startId);
			}
			if (intent.getBooleanExtra("Modified", false)) {
				String requestMethod = intent.getStringExtra("RequestMethod");
				if (requestMethod == null) {
					new Thread() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							synchronized (mNeededPushList) {
								mNeededPushList = ResourceUtils.getNeededPushList(PushService.this);
								sendBroadcast();
								if (mNeededPushList.size() <= 0) {
									stopSelf();
								}
							}
						}
					}.start();
				} else {
					if (requestMethod.equals("ADD")) {
						mNeededPushList.put(intent.getStringExtra("Key"), intent.getStringExtra("Value"));
					} else if (requestMethod.equals("REMOVE")) {
						mNeededPushList.remove(intent.getStringExtra("Key"));
					}
					sendBroadcast();
					if (mNeededPushList.size() <= 0) {
						stopSelf();
					}
				}
			} else if (mNeededPushList.size() <= 0) {
				stopSelf();
			} else if (intent.getBooleanExtra("Alarm", false)) {
				// started by alarm
				new Thread() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Intent noticeIntent = new Intent(PushService.this, MainActivity.class);
						noticeIntent.putExtra("ReceiveFragment", 2);// 开奖公告页面相应此notification
						PendingIntent noticePi = PendingIntent.getActivity(PushService.this, 0, noticeIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);
						Notification notice = new Notification.Builder(PushService.this).setTicker("有新消息")
								.setAutoCancel(true).setSmallIcon(R.drawable.lottery).setContentTitle("有更新了")
								.setContentText("有新的开奖结果，点击查看")
								.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
								.setWhen(System.currentTimeMillis()).setContentIntent(noticePi).build();
						nm.notify(EXIST_UPDATE, notice);
					}
				}.start();
			}
		}
		// return super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	private void sendBroadcast() {
		Intent mintent = new Intent("com.hy.mylottery.onelottery.PUSH_SETTING_SERVER");
		sendBroadcast(mintent);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if ((mNeededPushList == null || mNeededPushList.size() <= 0) && pi != null && alarmManager != null) {
			alarmManager.cancel(pi);
		}
	}

	class MBinder extends Binder {
		public HashMap<String, String> getNeededPushList() {
			return mNeededPushList;
		}

		public void setNeededPushList(HashMap<String, String> list) {
			if (mNeededPushList == null) {
				mNeededPushList = list;
			}
			cancelAlarm();
		}

		public void removeElementList(String key) {
			if (mNeededPushList != null) {
				mNeededPushList.remove(key);
			}
			cancelAlarm();
		}

		public void addElementList(String key, String value) {
			if (mNeededPushList != null) {
				mNeededPushList.put(key, value);
			}
		}

		private void cancelAlarm() {
			if (mNeededPushList == null || mNeededPushList.size() <= 0) {
				if (pi != null && alarmManager != null) {
					alarmManager.cancel(pi);
				}
				stopSelf();
			}
		}
	}
}
