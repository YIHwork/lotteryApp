package com.hy.mylottery;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hy.Utils.RefreshListView;
import com.hy.Utils.RefreshListView.RefreshTask;
import com.hy.Utils.ResourceUtils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class NoticeFragment extends Fragment implements RefreshTask {

	String titleName = "";
	public static float density;
	RefreshListView noticeListView;
	NoticeListAdapter adapter;
	// View header;
	// View loading;
	View headerRefresh;
	TextView headerWinMsg;
	// TextView winMsg;
	ImageView refreshAnimView;
	Animation anim;
	TextView refreshTime;
	ExecutorService loadingExecutorService;
	Handler mHandler;

	public NoticeFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NoticeFragment(String titleName) {
		// TODO Auto-generated constructor stub
		this();
		this.titleName = titleName;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0x111://
					hideRefreshDisplay();
					if (adapter.lotteryResultMap == null || adapter.lotteryResultMap.size() <= 0) {
						noticeListView.setVisibility(View.GONE);
					} else {
						adapter.notifyDataSetChanged();
					}
					// break;

				case 0x112:
					refreshTime.setText(getCurrentRefreshTime());
					break;
				default:
					break;
				}
			}
		};
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		density = metrics.density;
		anim = AnimationUtils.loadAnimation(this.getActivity(), R.anim.fragment_tab_notice_refresh_loading_anim);
		adapter = new NoticeListAdapter();
		initDataForAdapter();
		loadingExecutorService = Executors.newFixedThreadPool(8);
		// if (isNetworkAvailable()) {
		// loadingExecutorService.submit(new LoadingDataRunnable());
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_tab_notice, container, false);

		noticeListView = (RefreshListView) rootView.findViewById(R.id.notice_content);
		headerRefresh = inflater.inflate(R.layout.notice_header, null);
		refreshAnimView = (ImageView) headerRefresh.findViewById(R.id.refresh_ball_for_anim);

		refreshTime = (TextView) headerRefresh.findViewById(R.id.refresh_date_time);
		View winMsgContainer = inflater.inflate(R.layout.notice_header_win_msg, null);
		headerWinMsg = (TextView) winMsgContainer.findViewById(R.id.fragment_tab_notice_win_msg);
		headerWinMsg.setText(String.format(getResources().getString(R.string.fragment_tab_notice_win_msg_format), 0));
		noticeListView.addHeaderView(headerRefresh);
		noticeListView.addHeaderView(winMsgContainer);
		noticeListView.setRefreshableHeight(67);
		noticeListView.setRefreshTask(this);
		noticeListView.setAdapter(adapter);

		if (isNetworkAvailable()) {
			refresh();
			//
			//// for (int i = 0; i < noticeListView.getChildCount(); i++) {
			//// noticeListView.getChildAt(i).setClickable(false);
			//// }
			// if (headerRefresh.getVisibility() != View.VISIBLE) {
			// headerRefresh.setVisibility(View.VISIBLE);
			// }
			// ViewGroup.LayoutParams params = headerRefresh.getLayoutParams();
			// params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			// headerRefresh.setLayoutParams(params);
			// refreshAnimView.setVisibility(View.VISIBLE);
			// refreshAnimView.clearAnimation();
			// refreshAnimView.startAnimation(anim);
			// noticeListView.setEnabled(false);
		} else {
			if (adapter.lotteryResultMap == null || adapter.lotteryResultMap.size() <= 0) {
				noticeListView.setVisibility(View.GONE);
			}
			mHandler.sendEmptyMessage(0x112);
		}

		Button refreshAgain = (Button) rootView.findViewById(R.id.try_again);
		refreshAgain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				noticeListView.setVisibility(View.VISIBLE);
				refresh();
			}
		});

		TextView pushSetting = (TextView) rootView.findViewById(R.id.pushSetting);
		pushSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(), PushSettingActivity.class);
				startActivity(intent);
			}
		});
		return rootView;
	}

	public View formatDisplayForOpenCode(LinearLayout container, String lotteryCode, String data) {
		container.removeAllViews();
		if (data == null) {
			TextView resultCellTextView = new TextView(getActivity());
			LinearLayout.LayoutParams mparams = new LinearLayout.LayoutParams(ViewGroup.MarginLayoutParams.MATCH_PARENT,
					-2);
			resultCellTextView.setLayoutParams(mparams);
			resultCellTextView.setGravity(Gravity.CENTER);
			resultCellTextView.setText("暂无开奖结果,请再刷新试试");
			container.addView(resultCellTextView);
		} else if (lotteryCode.startsWith("zc")) {
			TextView resultCellTextView = new TextView(getActivity());
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
					TextView resultCellTextView = new TextView(getActivity());
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

	class NoticeListAdapter extends BaseAdapter {

		Map<String, Map<String, String>> lotteryResultMap = new HashMap<String, Map<String, String>>();

		class ConvertViewHolder {
			TextView nameView;
			TextView dateView;
			LinearLayout openCodeContainer;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			synchronized (lotteryResultMap) {
				if (convertView == null) {
					convertView = getActivity().getLayoutInflater().inflate(R.layout.notice_cell, parent, false);
					ConvertViewHolder holder = new ConvertViewHolder();
					holder.nameView = (TextView) convertView.findViewById(R.id.lottery_name);
					holder.dateView = (TextView) convertView.findViewById(R.id.open_date);
					holder.openCodeContainer = (LinearLayout) convertView.findViewById(R.id.open_code);
					convertView.setTag(holder);
				}

				ConvertViewHolder holder = (ConvertViewHolder) convertView.getTag();
				final Map<String, String> dataMap = getItem(position);
				holder.nameView.setText(dataMap.get("LotteryName"));
				holder.dateView.setText(dataMap.get("OpenDate"));
				formatDisplayForOpenCode(holder.openCodeContainer, dataMap.get("LotteryCode"), dataMap.get("OpenCode"));

				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getActivity(), SingleLotteryNoticeActivity.class);
						intent.putExtra("LotteryName", dataMap.get("LotteryName"));
						intent.putExtra("LotteryCode", dataMap.get("LotteryCode"));
						getActivity().startActivity(intent);
					}
				});
				return convertView;
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return lotteryResultMap.size();
		}

		@SuppressWarnings("unchecked")
		@Override
		public Map<String, String> getItem(int position) {
			// TODO Auto-generated method stub
			return (Map<String, String>) (lotteryResultMap.values().toArray()[position]);
			// keySet().get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}

	class LoadingDataRunnable implements Runnable {

		// Map<String, Map<String, String>> lotteryResultMap = new
		// HashMap<String, Map<String, String>>();
		String httpUrl = "http://apis.baidu.com/apistore/lottery/lotterylist";
		String httpArg = "lotterytype=1";

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String lotteryNamesJsonString = request(httpUrl, httpArg);

			try {
				JSONObject jsonLotteryNames = new JSONObject(lotteryNamesJsonString);
				int errNum = jsonLotteryNames.optInt("errNum");
				boolean isSuccess = jsonLotteryNames.optString("retMsg").equals("success");

				if (errNum == 0 && isSuccess) {
					JSONArray jsonDatas = jsonLotteryNames.getJSONArray("retData");
					JSONObject nameData;
					ArrayList<Future<Map<String, String>>> resultList = new ArrayList<Future<Map<String, String>>>();
					HashMap<String, Future<Map<String, String>>> futuresMap = new HashMap<String, Future<Map<String, String>>>();
					for (int i = 0; i < jsonDatas.length() && !loadingExecutorService.isShutdown(); i++) {
						nameData = jsonDatas.getJSONObject(i);
						String lotteryCode = nameData.getString("lotteryCode");
						String lotteryName = nameData.getString("lotteryName");
						final Map<String, String> resultMap = new HashMap<String, String>();
						resultMap.put("LotteryName", lotteryName);
						resultMap.put("LotteryCode", lotteryCode);
						Future<Map<String, String>> future = loadingExecutorService.submit(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								String httpUrl = "http://apis.baidu.com/apistore/lottery/lotteryquery";
								String httpArg = "lotterycode=" + resultMap.get("LotteryCode") + "&recordcnt=1";
								String openCodeJsonString = LoadingDataRunnable.this.request(httpUrl, httpArg);
								try {
									JSONObject jsonOpenCode = new JSONObject(openCodeJsonString);
									int errNum = jsonOpenCode.optInt("errNum");
									boolean isSuccess = jsonOpenCode.optString("retMsg").equals("success");

									if (errNum == 0 && isSuccess) {
										JSONObject jsonDatas = jsonOpenCode.getJSONObject("retData");
										JSONArray jsonArrayDatas = jsonDatas.getJSONArray("data");
										JSONObject data = jsonArrayDatas.getJSONObject(0);
										String expect = data.getString("expect");
										String openTime = data.getString("openTime");
										String openCode = data.getString("openCode");
										resultMap.put("OpenDate", "第" + expect + "期     " + openTime);
										resultMap.put("OpenCode", openCode);
										return;
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									System.out.println("JSONException error:");
									e.printStackTrace();
								}
								resultMap.put("OpenDate", "");
								resultMap.put("OpenCode", null);
							}
						}, resultMap);
						resultList.add(future);
						futuresMap.put(lotteryCode, future);
					}
					// 阻塞直到每一个票种加载开奖结果的线程结束
					Iterator<String> iterator = futuresMap.keySet().iterator();
					while (iterator.hasNext()) {
						try {
							String key = iterator.next();
							Map<String, String> resultMap = futuresMap.get(key).get(3, TimeUnit.SECONDS);
							// lotteryResultMap.put(key, resultMap);

							if (resultMap.get("OpenCode") == null) {
								Map<String, String> existingResultMap = adapter.lotteryResultMap.get(key);
								if (existingResultMap != null && existingResultMap.get("OpenCode") != null) {
									// nothing to do, next loop;
									continue;
								}
							}
							adapter.lotteryResultMap.put(key, resultMap);

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println("newwork err: interruptedException");
							// e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							System.out.println("newwork err: executtionException");
							// e.printStackTrace();
						} catch (TimeoutException e) {
							// TODO Auto-generated catch block
							System.out.println("newwork err: timeout");
							// e.printStackTrace();
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				System.out.println("JSONException error:");
				e.printStackTrace();
			}
			mHandler.sendEmptyMessage(0x111);

			putDataForAdapter();
		}

		/**
		 * @param urlAll
		 *            :请求接口
		 * @param httpArg
		 *            :参数
		 * @return 返回结果
		 */
		public String request(String httpUrl, String httpArg) {
			BufferedReader reader = null;
			String result = "";
			StringBuffer sbf = new StringBuffer();
			httpUrl = httpUrl + "?" + httpArg;

			try {
				URL url = new URL(httpUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				// 填入apikey到HTTP header
				connection.setRequestProperty("apikey", ResourceUtils.getApikey(getActivity()));
				connection.setConnectTimeout(3000);
				// connection.setReadTimeout(2000);
				connection.connect();
				InputStream is = connection.getInputStream();
				reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String strRead = null;
				while ((strRead = reader.readLine()) != null) {
					sbf.append(strRead);
					sbf.append("\r\n");
				}
				reader.close();
				result = sbf.toString();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				System.out.println("newwork err: MalformedURLException");
				// e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("newwork err: IOException");
				// e.printStackTrace();
			}
			return result;
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		loadingExecutorService.shutdown();
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connManager = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}

	public boolean isNetworkConnectedOrConnecting() {
		ConnectivityManager connManager = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.getState() == NetworkInfo.State.CONNECTED;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void initDataForAdapter() {
		try {
			FileInputStream input = getActivity().openFileInput("lottteryOpenCode.bin");
			ObjectInputStream objectInStream = new ObjectInputStream(input);
			adapter.lotteryResultMap = (Map<String, Map<String, String>>) objectInStream.readObject();
			objectInStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("FileNotFoundException error: when get file input stream");
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException error:");
			// e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("ClassNotFoundException error:");
			// e.printStackTrace();
		}
	}

	public void putDataForAdapter() {
		try {
			FileOutputStream output = getActivity().openFileOutput("lottteryOpenCode.bin", Context.MODE_PRIVATE);
			ObjectOutputStream objectOutStream = new ObjectOutputStream(output);
			if (adapter.lotteryResultMap != null && adapter.lotteryResultMap.size() > 0) {
				objectOutStream.writeObject(adapter.lotteryResultMap);
				objectOutStream.flush();
			}
			objectOutStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("FileNotFoundException error:");
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException error:");
			// e.printStackTrace();
		}
	}

	public String getCurrentRefreshTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.refresh_time_format),
				Locale.getDefault());
		String refreshTime = sdf.format(System.currentTimeMillis());
		// String refreshTime2 = sdf.format(new Date());
		return refreshTime;
	}

	public void refresh() {
		loadingExecutorService.submit(new LoadingDataRunnable());
		showRefreshDisplay();
	}

	@Override
	public void handleRefreshTask() {
		// TODO Auto-generated method stub
		if (isNetworkAvailable()) {
			refresh();
		} else {
			Toast.makeText(getActivity(), "please check your network connection", Toast.LENGTH_SHORT).show();
			hideRefreshDisplay();
			mHandler.sendEmptyMessage(0x112);
		}
	}

	public void showRefreshDisplay() {
		// During loading data, shouldn't response the other actions above it.
		noticeListView.setEnabled(false);
		for (int i = 0; i < noticeListView.getChildCount(); i++) {
			noticeListView.getChildAt(i).setClickable(false);
		}
		if (headerRefresh.getVisibility() != View.VISIBLE) {
			headerRefresh.setVisibility(View.VISIBLE);
		}
		ViewGroup.LayoutParams params = headerRefresh.getLayoutParams();
		params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		headerRefresh.setLayoutParams(params);
		refreshAnimView.setVisibility(View.VISIBLE);
		refreshAnimView.clearAnimation();
		refreshAnimView.startAnimation(anim);
	}

	public void hideRefreshDisplay() {
		ViewGroup.LayoutParams params = headerRefresh.getLayoutParams();
		params.height = 0;
		headerRefresh.setLayoutParams(params);
		headerRefresh.setVisibility(View.GONE);
		refreshAnimView.setVisibility(View.INVISIBLE);
		refreshAnimView.clearAnimation();
		noticeListView.setEnabled(true);
	}
}
