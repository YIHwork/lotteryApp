package com.hy.mylottery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hy.Utils.RefreshListView;
import com.hy.Utils.ResourceUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SingleLotteryNoticeActivity extends Activity
		implements RefreshListView.RefreshTask, RefreshListView.LoadTask {

	float density;
	Handler mHandler;
	String lotteryCode;
	String lotteryName;
	LayoutInflater inflater;
	// View netFailue;
	RefreshListView mListView;
	NoticeListAdapter adapter;
	View refreshHeader;
	ImageView refreshHeaderAnim;
	Animation headerAnim;
	View loadFooter;
	TextView loadFooterText;
	ImageView loadFooterAnim;
	Animation footerAnim;
	boolean isFooterLoading = false;
	boolean isNoMore = false;
	OpenCodeSQLiteOpenHelper sqliteHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent == null) {
			System.exit(0);
		}
		setContentView(R.layout.single_lottery_notice);
		inflater = getLayoutInflater();
		density = NoticeFragment.density;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0x111:
					if (mListView.getVisibility() == View.GONE) {
						mListView.setVisibility(View.VISIBLE);
						// netFailue.setVisibility(View.GONE);
					}
					adapter.notifyDataSetChanged();

				case 0x112:
					hideRefreshingDisplay();
					if (mListView.getFirstVisiblePosition() == 0
							&& mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1) {
						loadFooter.setVisibility(View.GONE);
					} else {
						loadFooter.setVisibility(View.VISIBLE);
					}
					break;

				case 0x113:
					Bundle data = msg.getData();
					Toast.makeText(SingleLotteryNoticeActivity.this, "Request err: " + data.getInt("errNum")
							+ ", errMsg: " + data.getString("retMsg") + "\nplease try again", Toast.LENGTH_LONG).show();
					break;

				case 0x114:
					if ((adapter.openCodes == null || adapter.openCodes.size() <= 0)
							&& mListView.getVisibility() != View.GONE) {
						mListView.setVisibility(View.GONE);
						// netFailue.setVisibility(View.VISIBLE);
					}
					break;

				case 0x115:
					adapter.notifyDataSetChanged();
				case 0x116:
					isFooterLoading = false;
					loadFooterText.setVisibility(View.VISIBLE);
					loadFooterAnim.setVisibility(View.INVISIBLE);
					loadFooterAnim.clearAnimation();
					if (isNoMore) {
						loadFooterText.setText(getResources().getString(R.string.no_more_data));
					}
					break;
				default:
					break;
				}
			}
		};

		lotteryName = intent.getStringExtra("LotteryName");
		lotteryCode = intent.getStringExtra("LotteryCode");
		String dataPath = "";
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File externalStorage = Environment.getExternalStorageDirectory();
			String externalPath = externalStorage.getPath() + "/oneLottery/databases/";
			File dataFile = new File(externalPath);
			if (!dataFile.exists()) {
				dataFile.mkdirs();
			}
			dataPath = dataFile.getAbsolutePath() + "/";
		}
		sqliteHelper = new OpenCodeSQLiteOpenHelper(this, dataPath + "opencodes.db", null, 1, lotteryCode);
		sqliteHelper.createTableIfNotExist();
		TextView titleName = (TextView) findViewById(R.id.title_name);
		titleName.setText(lotteryName);

		// netFailue = findViewById(R.id.net_failed);
		mListView = (RefreshListView) findViewById(R.id.notice_content);
		refreshHeader = inflater.inflate(R.layout.single_lottery_notice_header, null);
		refreshHeaderAnim = (ImageView) refreshHeader.findViewById(R.id.refresh_ball_for_anim);
		headerAnim = AnimationUtils.loadAnimation(this, R.anim.fragment_tab_notice_refresh_loading_anim);
		mListView.addHeaderView(refreshHeader);
		addPushActionBar();
		loadFooter = inflater.inflate(R.layout.single_lottery_notice_footer, null);
		loadFooterText = (TextView) loadFooter.findViewById(R.id.load_more);
		loadFooterAnim = (ImageView) loadFooter.findViewById(R.id.load_more_anim);
		footerAnim = headerAnim;
		mListView.addFooterView(loadFooter);
		adapter = new NoticeListAdapter();
		initDataList();// 为adapter初始化数据
		mListView.setAdapter(adapter);
		// 为首尾刷新加载数据准备的操作
		mListView.setRefreshTask(this);
		mListView.setRefreshableHeight(67);
		mListView.setLoadTask(this);
		refresh();
	}

	class NoticeListAdapter extends BaseAdapter {

		class ConvertViewHolder {
			View convertViewContent;
			TextView dateView;
			LinearLayout openCodeContainer;
			TextView line;
		}

		ArrayList<Map<String, String>> openCodes = new ArrayList<Map<String, String>>();

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			synchronized (openCodes) {
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.single_lottery_notice_cell, parent, false);
					ConvertViewHolder holder = new ConvertViewHolder();
					holder.convertViewContent = convertView.findViewById(R.id.cell_content);
					holder.dateView = (TextView) convertView.findViewById(R.id.open_date);
					holder.openCodeContainer = (LinearLayout) convertView.findViewById(R.id.open_code);
					holder.line = (TextView) convertView.findViewById(R.id.cell_line);
					convertView.setTag(holder);
				}

				ConvertViewHolder holder = (ConvertViewHolder) convertView.getTag();
				String openCode = getItem(position).get("OpenCode");
				String openExpect = getItem(position).get("Expect");
				String openTime = getItem(position).get("OpenTime");
				String openMsg = String.format(getResources().getString(R.string.open_msg_format), openExpect,
						openTime);
				holder.dateView.setText(openMsg);
				formatDisplayForOpenCode(holder.openCodeContainer, lotteryCode, openCode);

				if (position == getCount() - 1) {
					holder.line.setVisibility(View.GONE);
				} else {
					holder.line.setVisibility(View.VISIBLE);
				}
				holder.convertViewContent.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "拜托，没下级啦", Toast.LENGTH_LONG).show();
					}
				});
				return convertView;
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return openCodes.size();
		}

		@Override
		public Map<String, String> getItem(int position) {
			// TODO Auto-generated method stub
			return openCodes.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}

	public void refresh() {
		new Thread(new LoadDataRunnable(lotteryCode, 20)).start();
		showRefreshingDisplay();
	}

	public View formatDisplayForOpenCode(LinearLayout container, String lotteryCode, String data) {
		container.removeAllViews();
		if (data == null) {
			TextView resultCellTextView = new TextView(this);
			LinearLayout.LayoutParams mparams = new LinearLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT,
					-2);
			resultCellTextView.setLayoutParams(mparams);
			resultCellTextView.setGravity(Gravity.CENTER);
			resultCellTextView.setText("暂无开奖结果");
			container.addView(resultCellTextView);
		} else if (lotteryCode.startsWith("zc")) {
			TextView resultCellTextView = new TextView(this);
			LinearLayout.LayoutParams mparams = new LinearLayout.LayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT,
					-2);
			resultCellTextView.setLayoutParams(mparams);
			resultCellTextView.setGravity(Gravity.CENTER);
			resultCellTextView.setPadding((int) (40 * density), 0, (int) (40 * density), 0);
			resultCellTextView.setText(data.replace(",", "  "));
			resultCellTextView.setTextColor(getResources().getColor(R.color.white));
			resultCellTextView.setBackground(getResources().getDrawable(R.drawable.zc_open_code_bg_border));
			container.addView(resultCellTextView);
		} else
		// if (lotteryCode.equals("ssq"))
		{
			String[] openCodeCells = data.split("\\+");
			for (int i = 0; i < openCodeCells.length; i++) {
				int resid = i % 2 == 0 ? R.drawable.lottery_ball_pressed_red_small_bg_border
						: R.drawable.lottery_ball_pressed_blue_small_bg_border;
				String[] cells = openCodeCells[i].trim().split(",");
				for (int j = 0; j < cells.length; j++) {
					TextView resultCellTextView = new TextView(this);
					LinearLayout.LayoutParams mparams = new LinearLayout.LayoutParams(
							ViewGroup.MarginLayoutParams.WRAP_CONTENT, -2);
					mparams.setMargins((int) (2 * density), (int) (2 * density), (int) (2 * density),
							(int) (2 * density));
					resultCellTextView.setLayoutParams(mparams);
					resultCellTextView.setGravity(Gravity.CENTER);
					resultCellTextView.setPaddingRelative(5, 5, 5, 5);
					resultCellTextView.setBackgroundResource(resid);
					resultCellTextView.setText(cells[j]);
					resultCellTextView.setTextColor(getResources().getColor(R.color.white));
					container.addView(resultCellTextView);
				}
			}
		}
		return container;
	}

	class LoadDataRunnable implements Runnable {

		String httpUrl = "http://apis.baidu.com/apistore/lottery/lotteryquery";
		String httpArg = "lotterycode=%1$s&recordcnt=%2$s";
		ArrayList<Map<String, String>> openCodes = new ArrayList<Map<String, String>>();
		boolean isLoadingSuccessful = false;

		public LoadDataRunnable(String lotteryCode, int recordCnt) {
			// TODO Auto-generated constructor stub
			httpArg = String.format(httpArg, lotteryCode.trim(), String.valueOf(recordCnt));
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (isNecessaryLoadData()) {
				String requestResult = requestUrl(httpUrl, httpArg);
				if (requestResult != null) {
					try {
						JSONObject requestJsonResult = new JSONObject(requestResult);
						int errNum = requestJsonResult.getInt("errNum");
						String retMsg = requestJsonResult.getString("retMsg");
						if (errNum == 0 && retMsg.equals("success")) {
							JSONObject retData = requestJsonResult.getJSONObject("retData");
							JSONArray datas = retData.getJSONArray("data");
							openCodes.clear();
							for (int i = 0; i < datas.length(); i++) {
								JSONObject data = datas.getJSONObject(i);
								Map<String, String> dataMap = new HashMap<String, String>();
								dataMap.put("Expect", data.getString("expect"));
								dataMap.put("OpenCode", data.getString("openCode"));
								dataMap.put("OpenTime", data.getString("openTime"));
								dataMap.put("OpenTimeStamp", String.valueOf(data.getLong("openTimeStamp")));
								openCodes.add(dataMap);
							}

							if (openCodes.size() > 0) {
								adapter.openCodes = openCodes;
								isLoadingSuccessful = true;
								saveDataList(openCodes);
							}
						} else {
							// notify the error info
							Message msg = mHandler.obtainMessage(0x113);
							Bundle data = new Bundle();
							data.putInt("errNum", errNum);
							data.putString("retMsg", retMsg);
							msg.setData(data);
							msg.sendToTarget();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						System.out.println("JSON error: ");
						e.printStackTrace();
					}
				}
			}

			if (isLoadingSuccessful) {
				mHandler.sendEmptyMessage(0x111);// notify adapter data changed
				isLoadingSuccessful = !isLoadingSuccessful;

				ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) adapter.openCodes.clone();
				for (int i = 0; i < list.size(); i++) {
					Map<String, String> data = list.get(i);
					String expect = data.get("Expect");
					String openTime = data.get("OpenTime");
					String openCode = data.get("OpenCode");
					long openTimeStamp = Long.parseLong(data.get("OpenTimeStamp"));
					SQLiteDatabase db = null;
					try {
						db = sqliteHelper.getWritableDatabase();
						db.execSQL("insert into " + sqliteHelper.TABLE_NAME + " values(null,?,?,?,?)",
								new Object[] { expect, openTime, openCode, openTimeStamp });
					} catch (SQLiteConstraintException e) {
						// TODO: handle exception
						System.out.println("constraint error: ");
					} catch (SQLException e) {
						// TODO: handle exception
					} finally {
						if (db != null && db.isOpen()) {
							db.close();
						}
					}
				}
			} else {
				// just notify should hide the
				// refresh display
				mHandler.sendEmptyMessage(0x112);
				mHandler.sendEmptyMessage(0x114);
			}
		}

		public String requestUrl(String httpUrl, String httpArg) {
			String result = null;
			StringBuilder sbd = new StringBuilder();
			BufferedReader reader = null;
			HttpURLConnection con = null;
			try {
				URL url = new URL(httpUrl + "?" + httpArg);
				con = (HttpURLConnection) url.openConnection();
				con.setRequestProperty("apikey", ResourceUtils.getApikey(SingleLotteryNoticeActivity.this));
				con.setRequestMethod("GET");
				con.setConnectTimeout(3000);
				con.connect();

				reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					sbd.append(line);
					sbd.append("\r\n");
				}
				reader.close();
				result = sbd.toString();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (con != null) {
					con.disconnect();
				}
			}

			return result;
		}

	}

	public void saveDataList(ArrayList<Map<String, String>> dataList) {
		try {
			FileOutputStream fos = openFileOutput(lotteryCode + "_cacheOpenCodes.bin", Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			if (dataList != null && dataList.size() > 0) {
				oos.writeObject(dataList);
				oos.flush();
			}
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void initDataList() {
		try {
			FileInputStream fis = openFileInput(lotteryCode + "_cacheOpenCodes.bin");
			ObjectInputStream ois = new ObjectInputStream(fis);
			@SuppressWarnings("unchecked")
			ArrayList<Map<String, String>> openCodes = (ArrayList<Map<String, String>>) ois.readObject();
			if (openCodes != null) {
				adapter.openCodes = openCodes;
			}
			ois.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("FileNotFoundException error:");
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	public void back(View v) {
		this.onBackPressed();
	}

	public void refresh(View v) {
		mListView.setVisibility(View.VISIBLE);
		refresh();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (sqliteHelper != null) {
			sqliteHelper.close();
		}
	}

	@Override
	public void handleRefreshTask() {
		// TODO Auto-generated method stub
		if (isNetworkAvailable()) {
			refresh();
		} else {
			Toast.makeText(this, "please check your network connection", Toast.LENGTH_SHORT).show();
			hideRefreshingDisplay();
		}
	}

	@Override
	public void handleLoadTask() {
		// TODO Auto-generated method stub
		if (!isFooterLoading && !isNoMore) {
			loadFooterText.setVisibility(View.INVISIBLE);
			loadFooterAnim.setVisibility(View.VISIBLE);
			loadFooterAnim.clearAnimation();
			loadFooterAnim.startAnimation(footerAnim);
			isFooterLoading = true;
			new Thread() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					SQLiteDatabase db = null;
					Cursor cursor = null;
					boolean isNecessaryUpdate = false;
					try {
						db = sqliteHelper.getReadableDatabase();
						String sqlstr = "select * from " + sqliteHelper.TABLE_NAME + " where open_time_stamp < ?"
								+ " order by open_time_stamp desc" + " limit 10";
						cursor = db.rawQuery(sqlstr,
								new String[] { adapter.openCodes.get(adapter.getCount() - 1).get("OpenTimeStamp") });
						int count = cursor.getCount();
						while (cursor.moveToNext()) {
							Map<String, String> dataMap = new HashMap<String, String>();
							dataMap.put("Expect", cursor.getString(1));
							dataMap.put("OpenTime", cursor.getString(2));
							dataMap.put("OpenCode", cursor.getString(3));
							dataMap.put("OpenTimeStamp", cursor.getString(4));
							adapter.openCodes.add(dataMap);
							if (!isNecessaryUpdate) {
								isNecessaryUpdate = true;
							}
						}
						if (count < 10) {
							isNoMore = true;
						}
					} catch (SQLiteException e) {
						// TODO: handle exception
					} finally {
						if (db != null && db.isOpen()) {
							db.close();
						}
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
						}
					}
					if (isNecessaryUpdate) {
						mHandler.sendEmptyMessage(0x115);
					} else {
						mHandler.sendEmptyMessage(0x116);
					}
				}
			}.start();
		} else {
			// nothing to do;
		}

	}

	@Override
	public void updateDisplay(boolean updateOrRecover) {
		// TODO Auto-generated method stub
		if (!isFooterLoading && !isNoMore) {
			String text = "";
			if (updateOrRecover) {
				text = getResources().getString(R.string.loading_more_release);
			} else {
				text = getResources().getString(R.string.loading_more);
			}
			loadFooterText.setText(text);
		}
	}

	public void showRefreshingDisplay() {
		LayoutParams params = refreshHeader.getLayoutParams();
		params.height = LayoutParams.WRAP_CONTENT;
		refreshHeader.setLayoutParams(params);
		if (refreshHeader.getVisibility() != View.VISIBLE) {
			refreshHeader.setVisibility(View.VISIBLE);
		}
		refreshHeaderAnim.setVisibility(View.VISIBLE);
		refreshHeaderAnim.clearAnimation();
		refreshHeaderAnim.startAnimation(headerAnim);
		mListView.setEnabled(false);
	}

	public void hideRefreshingDisplay() {
		LayoutParams params = refreshHeader.getLayoutParams();
		params.height = 0;
		refreshHeader.setLayoutParams(params);
		refreshHeader.setVisibility(View.GONE);
		refreshHeaderAnim.clearAnimation();
		refreshHeaderAnim.setVisibility(View.INVISIBLE);
		mListView.setEnabled(true);
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null) {
			return netInfo.isAvailable();
		}
		return false;
	}

	public boolean isNecessaryLoadData() {
		if (lotteryCode != null && lotteryCode.equals("ssq") && adapter != null && adapter.openCodes != null
				&& adapter.openCodes.size() > 0) {
			String openTimeStamp = adapter.openCodes.get(0).get("OpenTimeStamp");
			if (openTimeStamp == null) {
				return true;
			}
			long latestOpenTimeStamp = Long.parseLong(openTimeStamp);
			Calendar calendar = Calendar.getInstance();
			long currentMilliseconds = calendar.getTimeInMillis();
			calendar.setTimeInMillis(latestOpenTimeStamp * 1000);
			int openWeek = calendar.get(Calendar.DAY_OF_WEEK);
			long offsetMilliseconds;
			switch (openWeek) {
			case 1:// Sunday
			case 3:// Tuesday
				offsetMilliseconds = 2 * 24 * 60 * 60 * 1000;
				break;

			case 5:// Thursday
				offsetMilliseconds = 3 * 24 * 60 * 60 * 1000;
				break;
			default:
				offsetMilliseconds = 0;
				break;
			}

			if (offsetMilliseconds > 0) {
				long nextOpenTime = calendar.getTimeInMillis() + offsetMilliseconds;
				if (nextOpenTime > currentMilliseconds) {
					// 还没有到开奖时间
					return false;
				}
			}
		}
		return true;
	}

	public void addPushActionBar() {
		if (lotteryCode.equals("ssq")) {
			// 暂时只给双色球添加push bar
			View msgBar = inflater.inflate(R.layout.single_lottery_notice_header_msg_bar, null);
			ImageView pushOnOff = (ImageView) msgBar.findViewById(R.id.push_on_off);
			final HashMap<String, String> neededPushList = ResourceUtils.getNeededPushList(this);
			if (neededPushList == null || neededPushList.containsKey(lotteryCode)) {
				pushOnOff.setImageDrawable(getResources().getDrawable(R.drawable.pay_password_on));
				pushOnOff.setTag("ON");
			} else {
				pushOnOff.setImageDrawable(getResources().getDrawable(R.drawable.pay_password_off));
				pushOnOff.setTag("OFF");
			}
			pushOnOff.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (v.getTag().toString().equals("ON")) {
						((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.pay_password_off));
						v.setTag("OFF");
						neededPushList.remove(lotteryCode);
					} else {
						((ImageView) v).setImageDrawable(getResources().getDrawable(R.drawable.pay_password_on));
						v.setTag("ON");
						neededPushList.put(lotteryCode, lotteryName);
					}
					ResourceUtils.putNeededPushList(neededPushList);
					Intent mintent = new Intent("com.hy.mylottery.onelottery.PUSH_SERVICE");
					mintent.setPackage(getPackageName());
					mintent.putExtra("Modified", true);
					startService(mintent);
				}
			});
			mListView.addHeaderView(msgBar);
		}
	}

	class OpenCodeSQLiteOpenHelper extends SQLiteOpenHelper {

		String lotteryCode;
		String TABLE_NAME = "_open_code_table";
		String CREATE_TABLE_SQL = "create table %s(_id integer primary key autoincrement, "
				+ "expect varchar(255) not null, " + "open_time TEXT not null, " + "open_code TEXT not null,"
				+ "open_time_stamp integer not null, " + "unique (expect), unique (open_time_stamp))";

		public OpenCodeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version,
				String lotteryCode) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
			this.lotteryCode = lotteryCode;
			TABLE_NAME = lotteryCode + TABLE_NAME;
		}

		public OpenCodeSQLiteOpenHelper(Context context, String name, CursorFactory factory, int version,
				String lotteryCode, DatabaseErrorHandler errorHandler) {
			super(context, name, factory, version, errorHandler);
			// TODO Auto-generated constructor stub
			this.lotteryCode = lotteryCode;
			TABLE_NAME = lotteryCode + TABLE_NAME;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(String.format(CREATE_TABLE_SQL, TABLE_NAME));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}

		public void createTableIfNotExist() {
			SQLiteDatabase db = null;
			try {
				db = this.getReadableDatabase();

				if (!tabbleIsExist(db, TABLE_NAME)) {
					db.execSQL(String.format(CREATE_TABLE_SQL, TABLE_NAME));
				}
			} catch (SQLException e) {
				// TODO: handle exception
			} finally {
				if (db != null && db.isOpen()) {
					db.close();
				}
			}
		}

		/**
		 * 判断某张表是否存在
		 * 
		 * @param tabName
		 *            表名
		 * @return
		 */
		public boolean tabbleIsExist(SQLiteDatabase db, String tableName) {
			boolean result = false;
			if (tableName == null) {
				return false;
			}
			// SQLiteDatabase db = null;
			Cursor cursor = null;
			try {
				db = this.getReadableDatabase();
				String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"
						+ tableName.trim() + "' ";
				cursor = db.rawQuery(sql, null);
				if (cursor.moveToNext()) {
					int count = cursor.getInt(0);
					if (count > 0) {
						result = true;
					}
				}

			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
				// if (db!=null && db.isOpen()) {
				// db.close();
				// }
			}
			return result;
		}
	}

}
