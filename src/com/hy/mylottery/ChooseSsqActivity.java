package com.hy.mylottery;

import com.hy.mylottery.lotteryType.SsqLottery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hy.Utils.ResourceUtils;
import com.hy.Utils.SsqSelectScrollView;
import com.hy.mylottery.lotteryType.LotteryBallContainerTableLayout;
import com.hy.mylottery.lotteryType.LotteryBallType.LotteryBallAppearance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseSsqActivity extends Activity implements Handler.Callback, SensorEventListener {

	private static final int RED_BALL_STYLE_ID = 0;
	private static final int BLUE_BALL_STYLE_ID = 1;
	public static final String CACHE_DATABASE_NAME = "ssqCacheBettingSlip.db";
	LayoutInflater inflater;
	// public static LotteryBallTypeBettingDatabaseThread bettingDatabaseThread
	// = new LotteryBallTypeBettingDatabaseThread();
	SsqLottery ssqLottery;
	LotteryBallContainerTableLayout redBallContainer;
	LotteryBallContainerTableLayout blueBallContainer;
	SsqSelectScrollView selectBallScrollView;
	ImageView pullDownView;
	LastestTenOpenCodeListView tenListView;
	BaseAdapter adapter;
	View tableTitle;
	View loadingHeader;
	TextView loadingHeaderTxt;
	View loadingHeaderAnim;
	ImageView loadingHeaderAnimImg;
	Animation loadingImgAnim;
	View netFailFooter;
	ArrayList<String[]> openCodes = new ArrayList<String[]>();
	boolean isLoaded = false; // tenListView是否已经load过数据
	ShowOpenCodeTask task;

	LotteryMultipleEditText edMultiple;
	Button randomBetBtn;
	Button addToSlipBtn;
	TextView msgBetting;
	ImageView btnMulMinus, btnMulPlus;
	Button btnBetting;
	TextView chooseballCartCount;
	Animation chooseballCountAnim;
	ImageView chooseballAnimImageView;
	Animation chooseballBetAnim;
	TextView nextOpenDate;
	boolean isLoadNextOpenDateSuccess = false;

	// the String[] in bettingSlips should only include four elements;
	// {betting_num, betting_count, betting_multiple, betting_money}
	public static ArrayList<String[]> bettingSlips = new ArrayList<String[]>();
	// LotteryBallTypeSQLiteOpenHelper sqliteOpenHelper;
	Handler handler;
	SensorManager sensorManager;
	Vibrator vibrator;
	long currentTime = 0;// 标记一个时间戳，用于控制摇一摇的时间差。

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selector_shuangseqiu_page);

		// setFitsSystemWindows(false)会让弹出软键盘布局自动调整失效，
		// 可重写根布局的fitSystemWindows(Rect
		// insets)（api<20）或onApplyWindowInsets(WindowInsets)（api>= 20）
		// 使setFitsSystemWindows(true)与setFitsSystemWindows(false)显示效果相同。
		updateStatusBarDisplay();
		inflater = getLayoutInflater();
		handler = new Handler(this);
		// sqliteOpenHelper = new LotteryBallTypeSQLiteOpenHelper(this,
		// CACHE_DATABASE_NAME, null, 1);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		multipleOnClickListener btnMulListener = new multipleOnClickListener(); // 各组件onClickListener

		nextOpenDate = (TextView) findViewById(R.id.next_lottery_date);
		nextOpenDate.setOnClickListener(btnMulListener);
		loadNextOpenDate(); // 加载下次开奖日期和开奖期数
		// initLastestTenOpenCode(); //For testing ExpandableListView

		ssqLottery = new SsqLottery(this);
		redBallContainer = (LotteryBallContainerTableLayout) findViewById(R.id.red_ball_container);
		blueBallContainer = (LotteryBallContainerTableLayout) findViewById(R.id.blue_ball_container);
		redBallContainer.addElementsForBallContainer(ssqLottery, RED_BALL_STYLE_ID, handler);
		blueBallContainer.addElementsForBallContainer(ssqLottery, BLUE_BALL_STYLE_ID, handler);

		selectBallScrollView = (SsqSelectScrollView) findViewById(R.id.select_ball);
		pullDownView = (ImageView) findViewById(R.id.selector_pull_icon);
		tenListView = (LastestTenOpenCodeListView) findViewById(R.id.ten_open_code_list);
		tableTitle = inflater.inflate(R.layout.ssq_lastest_ten_open_code_list_cell, null);
		// =============start============
		// 初始化加载开奖显示动画的组件
		loadingHeader = inflater.inflate(R.layout.ssq_lastest_ten_open_code_list_loading_header, null);
		loadingHeaderTxt = (TextView) loadingHeader.findViewById(R.id.loading_txt);
		loadingHeaderAnim = loadingHeader.findViewById(R.id.loading_anim);
		loadingHeaderAnimImg = (ImageView) loadingHeader.findViewById(R.id.loading_anim_img);
		loadingImgAnim = AnimationUtils.loadAnimation(this, R.anim.fragment_tab_notice_refresh_loading_anim);
		// ==============end=============
		netFailFooter = inflater.inflate(R.layout.ssq_lastest_ten_open_code_list_footer, null);
		netFailFooter.setOnClickListener(btnMulListener);
		tenListView.addFooterView(netFailFooter);
		tenListView.addHeaderView(tableTitle);
		tenListView.addHeaderView(loadingHeader);
		addAdapter(); // 为tenListView添加adapter
		setOnTouchListenerForScroll(); // 为ScrollView设置一个OnTouchListener,用于实现顶部“最近十次开奖List”的抽屉效果。

		randomBetBtn = (Button) findViewById(R.id.random_bet);
		randomBetBtn.setOnClickListener(btnMulListener);
		addToSlipBtn = (Button) findViewById(R.id.add_to_slip);
		addToSlipBtn.setOnClickListener(btnMulListener);

		edMultiple = (LotteryMultipleEditText) findViewById(R.id.ed_multiple);
		edMultiple.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					hideSoftInputMethod(v);
					return true;
				}
				return false;
			}
		});
		edMultiple.addTextChangedListener(new MTextWatcher());
		btnMulMinus = (ImageView) findViewById(R.id.multiple_minus);
		btnMulPlus = (ImageView) findViewById(R.id.multiple_plus);
		btnMulMinus.setOnClickListener(btnMulListener);
		btnMulMinus.setClickable(false);
		btnMulPlus.setOnClickListener(btnMulListener);

		// init bottom component
		msgBetting = (TextView) findViewById(R.id.msg_betting);
		btnBetting = (Button) findViewById(R.id.bet_btn);
		btnBetting.setOnClickListener(btnMulListener);
		// 初始化显示彩票注数的组件
		initChooseballCartCountView();
		// 初始化点击加入投注单时的动画组件
		initChooseballAnim();

		initBettingData();// 如果是通过SsqBettingSlipActivity启动，设置一些初始数据
	}

	private void initBettingData() {
		Intent intent = getIntent();
		if (intent != null && intent.getBooleanExtra("isSsqBettingSlipActivity", false)) {
			int position = intent.getIntExtra("Position", -1);
			if (position < 0) {
				System.out.println("position < 0");
				return;
			}
			String[] bettingContent = bettingSlips.get(position);
			addToSlipBtn.setVisibility(View.GONE);
			btnBetting.setText("确认修改");
			chooseballCartCount.setVisibility(View.GONE);

			String[] bettingNums = bettingContent[0].split("\\|");
			String[] redNums = bettingNums[0].split("\\s");
			for (String redNum : redNums) {
				if (!redNum.equals("")) {
					((LotteryBallAppearance) ssqLottery.getAppearanceStyle(RED_BALL_STYLE_ID))
							.updateSelectedBallState(redBallContainer.getBallView(Integer.parseInt(redNum)));
				}
			}
			String[] blueNums = bettingNums[1].split("\\s");
			for (String blueNum : blueNums) {
				if (!blueNum.equals("")) {
					((LotteryBallAppearance) ssqLottery.getAppearanceStyle(BLUE_BALL_STYLE_ID))
							.updateSelectedBallState(blueBallContainer.getBallView(Integer.parseInt(blueNum)));
				}
			}

			handler.sendEmptyMessage(0x125);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			View currentView = getCurrentFocus();
			if (currentView instanceof EditText) {
				int[] location = new int[2];
				selectBallScrollView.getLocationOnScreen(location);
				if (ev.getX() >= location[0] && ev.getX() <= location[0] + selectBallScrollView.getWidth()
						&& ev.getY() >= location[1] && ev.getY() <= location[1] + selectBallScrollView.getHeight()) {
					hideSoftInputMethod(currentView);
					return true;
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	public void hideSoftInputMethod(View v) {
		InputMethodManager imeManager = (InputMethodManager) v.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imeManager.isActive(v)) {
			imeManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			v.clearFocus();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case 0x123: // dismiss popup window
			// Bundle bundle = msg.getData();
			PopupWindow popup = (PopupWindow) msg.obj;
			if (popup != null && popup.isShowing()) {
				popup.dismiss();
			}
			break;
		case 0x124: // show popup window

			Object[] obj = (Object[]) msg.obj;
			LotteryBallAppearance appearance = (LotteryBallAppearance) obj[0];
			View v = (View) obj[1];
			appearance.showPopupWindow(v);
			break;
		case 0x125: // update the state of random_button
			if (ssqLottery.redBallSelectedList.isEmpty() && ssqLottery.blueBallSelectedList.isEmpty()) {
				randomBetBtn.setText(R.string.random_btn_bet);
			} else if (randomBetBtn.getText().equals(getResources().getString(R.string.random_btn_bet))) {
				randomBetBtn.setText(R.string.random_btn_clear);
			}
		case 0x126: // update the state of msgTotalMoney and msgBetting
					// TextView;
			int redBallCount = ssqLottery.redBallSelectedList.size();
			int blueBallCount = ssqLottery.blueBallSelectedList.size();
			if ((redBallCount < 6 || blueBallCount < 1)) {
				if (msgBetting.getText().toString().contains("\n")) {
					msgBetting.setText(R.string.ssq_bet_notes);
				}

			} else {
				// 投注数量=（红球个数的阶乘*蓝球个数）/（6！*（红球个数-6）！）
				BigDecimal bettingCount = computeBettingCount(redBallCount, blueBallCount);
				String edMultipleText = edMultiple.getText().toString();
				int bettingMultiple = edMultipleText.equals("") ? 1 : Integer.parseInt(edMultipleText);

				String bettingMsg = String.format(getResources().getString(R.string.msg_bet_format),
						bettingCount.multiply(BigDecimal.valueOf(2 * bettingMultiple)).toString(),
						bettingCount.toString(), bettingMultiple);
				// 使钱数高亮
				SpannableString bettingMsgBuilder = new SpannableString(bettingMsg);
				ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.orange));
				int end = bettingMsg.indexOf("\n");
				if (end > 0) {
					bettingMsgBuilder.setSpan(colorSpan, 0, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				msgBetting.setText(bettingMsgBuilder);
			}
			if (msgBetting.getCurrentTextColor() == Color.YELLOW) {
				msgBetting.setTextColor(getResources().getColor(R.color.white));
			}
			break;

		case 0x127:// popup connect net server error msg;
			Bundle data = msg.getData();
			String errMsg = "errMsg: " + data.getString("retMsg");
			Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show();
			break;

		case 0x128:
			Bundle data01 = msg.getData();
			String expect = data01.getString("expect");
			String openDate = data01.getString("openDate");
			String nextOpenExpectDate = String.format(getResources().getString(R.string.ssq_next_open_date_format),
					expect, openDate);
			nextOpenDate.setText(nextOpenExpectDate);
			isLoadNextOpenDateSuccess = true;
			break;

		case 0x129:
			ArrayList<String[]> openCodes = (ArrayList<String[]>) msg.obj;
			if (openCodes.size() > 0) {
				this.openCodes = openCodes;
				adapter.notifyDataSetChanged();
				tableTitle.setVisibility(View.VISIBLE);
				netFailFooter.setVisibility(View.GONE);
			} else {
				netFailFooter.setVisibility(View.VISIBLE);
				LayoutParams params = netFailFooter.getLayoutParams();
				params.height = getOpenCodeListMaxHeight();
				netFailFooter.setLayoutParams(params);
				netFailFooter.setClickable(true);
			}

			hideLoadingDisplay();
			break;

		default:
			break;
		}
		return true;
	}

	public void clearSelectedBallState() {
		int ballCount = ssqLottery.redBallSelectedList.size();
		for (int i = 0; i < ballCount; i++) {
			((LotteryBallAppearance) ssqLottery.getAppearanceStyle(RED_BALL_STYLE_ID))
					.updateSelectedBallState(redBallContainer.getBallView(ssqLottery.redBallSelectedList.get(0)));
		}
		ballCount = ssqLottery.blueBallSelectedList.size();
		for (int i = 0; i < ballCount; i++) {
			((LotteryBallAppearance) ssqLottery.getAppearanceStyle(BLUE_BALL_STYLE_ID))
					.updateSelectedBallState(blueBallContainer.getBallView(ssqLottery.blueBallSelectedList.get(0)));
		}
	}

	public void updateRandomBettedBallState(int[] randombettingNums) {

		for (int i = 0; i < randombettingNums.length - 1; i++) {
			((LotteryBallAppearance) ssqLottery.getAppearanceStyle(RED_BALL_STYLE_ID))
					.updateSelectedBallState(redBallContainer.getBallView(randombettingNums[i]));
		}
		((LotteryBallAppearance) ssqLottery.getAppearanceStyle(BLUE_BALL_STYLE_ID)).updateSelectedBallState(
				blueBallContainer.getBallView(randombettingNums[randombettingNums.length - 1]));
	}

	public String[] formatBettingContent(ArrayList<Integer> redBallList, ArrayList<Integer> blueBallList) {
		int redBallCount = redBallList.size();
		int blueBallCount = blueBallList.size();
		if (redBallCount >= 6 || blueBallCount >= 1) {
			selectedBallSort(redBallList, 0, redBallCount - 1);
			selectedBallSort(blueBallList, 0, blueBallCount - 1);
			String edMultipleText = edMultiple.getText().toString();
			// String[] bettingContent = new String[4];
			// {betting_num,
			// betting_count,
			// betting_multiple,
			// betting_money}
			BigDecimal bettingCount = computeBettingCount(redBallCount, blueBallCount);
			int bettingMultiple = edMultipleText.equals("") ? 1 : Integer.parseInt(edMultipleText);
			BigDecimal bettingMoney = bettingCount.multiply(BigDecimal.valueOf(2 * bettingMultiple));
			StringBuilder bettingNumStringBuilder = new StringBuilder();
			for (int i = 0; i < redBallCount; i++) {
				if (redBallList.get(i) < 10) {
					bettingNumStringBuilder.append("0" + redBallList.get(i) + " ");
				} else
					bettingNumStringBuilder.append(redBallList.get(i) + " ");
			}
			bettingNumStringBuilder.append("|");
			for (int i = 0; i < blueBallCount; i++) {
				if (blueBallList.get(i) < 10) {
					bettingNumStringBuilder.append(" 0" + blueBallList.get(i));
				} else
					bettingNumStringBuilder.append(" " + blueBallList.get(i));
			}
			String[] bettingContent = new String[] { bettingNumStringBuilder.toString(), bettingCount.toString(),
					String.valueOf(bettingMultiple), bettingMoney.toString() };
			return bettingContent;
		}
		return null;
	}

	public static void selectedBallSort(ArrayList<Integer> selectedBalls, int start, int end) {
		if (start >= end) {
			return;
		}
		int base = selectedBalls.get(start);
		int temp;
		int i = start, j = end;
		while (i <= j) {
			while (i < end && selectedBalls.get(i) < base) {
				i++;
			}
			while (j > start && base <= selectedBalls.get(j)) {
				j--;
			}
			if (i < j) {
				temp = selectedBalls.get(i);
				selectedBalls.set(i, selectedBalls.get(j));
				selectedBalls.set(j, temp);
				i++;
				j--;
			} else if (i == j) {
				i++;
				j--;
			}
		}
		if (i < end) {
			selectedBallSort(selectedBalls, i, end);
		}
		if (j > start) {
			selectedBallSort(selectedBalls, start, j);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		if (!btnBetting.getText().equals("确认修改")) {
			chooseballAnimImageView.clearAnimation();
			chooseballCartCount.clearAnimation();
			int slipSize = bettingSlips.size();
			if (slipSize <= 0) {
				chooseballCartCount.setVisibility(View.GONE);
			} else {
				chooseballCartCount.setVisibility(View.VISIBLE);
				if (slipSize >= 100) {
					chooseballCartCount.setText("...");
				} else {
					chooseballCartCount.setText(String.valueOf(slipSize));
				}
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// if (sqliteOpenHelper != null) {
		// sqliteOpenHelper.close();
		// }
		// if (bettingDatabaseThread.handler != null) {
		// bettingDatabaseThread.handler.removeCallbacksAndMessages(null);
		// }
	}

	/**
	 * 采用迭代实现阶乘
	 * 
	 * @return return n!, the type is BigDecimal
	 */
	public static BigDecimal factorial(int n) {
		BigDecimal resultValue = new BigDecimal(n);
		if (n == 1) {
			return resultValue;
		} else if (n <= 0) {
			return BigDecimal.ONE;
		} else {
			return resultValue.multiply(factorial(n - 1));
		}
	}

	// 投注数量=（红球个数的阶乘*蓝球个数）/（6！*（红球个数-6）！）
	public static BigDecimal computeBettingCount(int redBallCount, int blueBallCount) {
		if (redBallCount < 6 || blueBallCount < 1) {
			return null;
		}

		return factorial(redBallCount).divide(factorial(6)).divide(factorial(redBallCount - 6))
				.multiply(BigDecimal.valueOf(blueBallCount));

	}

	// 初始化显示投注单彩票注数的组件，并控制将其放在具体指定位置
	private void initChooseballCartCountView() {
		chooseballCartCount = (TextView) findViewById(R.id.chooseball_cart_count);
		ViewGroup.MarginLayoutParams params1 = (MarginLayoutParams) btnBetting.getLayoutParams();
		ViewGroup.MarginLayoutParams params2 = (MarginLayoutParams) chooseballCartCount.getLayoutParams();
		if (params1 == null || params2 == null) {
			System.out.println("error");
			return;
		}
		params2.rightMargin = ((RelativeLayout) btnBetting.getParent()).getPaddingEnd() - params2.width / 2;
		params2.bottomMargin = ((RelativeLayout) btnBetting.getParent()).getPaddingBottom() / 2 + params1.height
				- params2.height / 2;
		chooseballCartCount.setLayoutParams(params2);
	}

	// 初始化彩票加入投注单时动画的各组件
	private void initChooseballAnim() {
		chooseballCountAnim = AnimationUtils.loadAnimation(this, R.anim.chooseball_cart_count_animation);
		chooseballAnimImageView = (ImageView) findViewById(R.id.chooseball_buy_ani_icon);
		chooseballBetAnim = AnimationUtils.loadAnimation(this, R.anim.chooseball_bet_anim);
		chooseballBetAnim.setAnimationListener(new AnimationListener() {

			long startTime;

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				startTime = System.currentTimeMillis();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				long endTime = System.currentTimeMillis();
				if (chooseballCartCount.getVisibility() != View.VISIBLE) {
					chooseballCartCount.setVisibility(View.VISIBLE);
				}
				if (endTime - startTime >= 500) {
					chooseballCartCount.startAnimation(chooseballCountAnim);
					if (bettingSlips.size() >= 100) {
						chooseballCartCount.setText("...");
						return;
					}
					chooseballCartCount.setText(String.valueOf(bettingSlips.size()));
				}

			}
		});
	}

	private class multipleOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String mulText = edMultiple.getText().toString();
			switch (v.getId()) {
			case R.id.ssq_net_failue:
				// 如果加载最近十次开奖失败，click netFailFooter重新加载
				loadLatestTenOpenCode();
				v.setClickable(false);
				break;

			case R.id.next_lottery_date:
				if (!isLoadNextOpenDateSuccess) {
					// 如果下次开奖日期没有加载或加载不成功，
					// 那么click后重新加载
					loadNextOpenDate();
				}
				if (selectBallScrollView.getScrollY() != 0) {
					// 如果scrollView没有滑动到顶部，首先置顶
					selectBallScrollView.scrollTo(0, 0);
				}
				if (task != null) {
					// 之前如果有任务正在进行，取消
					task.cancel(true);
				}

				// 新执行一个task，作用于listView的展开和收缩
				LayoutParams params = tenListView.getLayoutParams();
				task = new ShowOpenCodeTask();
				if (params.height <= 0) {
					task.execute(1, params.height);
				} else {
					task.execute(-1, params.height);
				}
				break;

			case R.id.multiple_minus:
				int mulMinus = Integer.parseInt(mulText);
				edMultiple.setText(String.valueOf(--mulMinus));
				break;

			case R.id.multiple_plus:
				if (mulText.equals("")) {
					edMultiple.setText("2");
				} else {
					int mulPlus = Integer.parseInt(mulText);
					edMultiple.setText(String.valueOf(++mulPlus));
				}
				edMultiple.setSelection(edMultiple.getText().length());
				break;

			case R.id.random_bet:
				if (((Button) v).getText().equals(getResources().getString(R.string.random_btn_bet))) {
					int[] randomSelectedNums = SsqLottery.randomBetting();
					updateRandomBettedBallState(randomSelectedNums);
					((Button) v).setText(R.string.random_btn_clear);
				} else if (((Button) v).getText().equals(getResources().getString(R.string.random_btn_clear))) {
					clearSelectedBallState();
					ssqLottery.redBallSelectedList.clear();
					ssqLottery.blueBallSelectedList.clear();
					((Button) v).setText(R.string.random_btn_bet);
				}
				handler.sendEmptyMessage(0x126);
				break;

			case R.id.add_to_slip:
				int mRedBallCount = ssqLottery.redBallSelectedList.size();
				int mBlueBallCount = ssqLottery.blueBallSelectedList.size();
				if (mRedBallCount < 6 || mBlueBallCount < 1) {
					msgBetting.setTextColor(Color.YELLOW);
				} else {

					String[] bettingContent = formatBettingContent(ssqLottery.redBallSelectedList,
							ssqLottery.blueBallSelectedList);
					bettingSlips.add(0, bettingContent);

					// 数据储存在数据库中，自定义的Thread（LotteryBallTypeBettingDatabaseThread），通过新线程的handler来读取数据
					// if (bettingDatabaseThread.getState() == Thread.State.NEW)
					// {
					// bettingDatabaseThread.start();
					// System.out.println("new");
					// } else if (!bettingDatabaseThread.isAlive()) {
					// bettingDatabaseThread = new
					// LotteryBallTypeBettingDatabaseThread();
					// bettingDatabaseThread.start();
					// System.out.println("restart");
					// }
					// while (bettingDatabaseThread.handler == null) {
					// System.out.println("still main thread");
					// Thread.yield();
					// }
					// Message msg = Message.obtain();
					// msg.what = 0x1111;
					// msg.obj = sqliteOpenHelper;
					// Bundle data = new Bundle();
					// data.putString("edMultipleText", edMultipleText);
					// data.putIntegerArrayList("redBallList",
					// (ArrayList<Integer>)
					// ssqLottery.redBallSelectedList.clone());
					// data.putIntegerArrayList("blueBallList",
					// (ArrayList<Integer>)
					// ssqLottery.blueBallSelectedList.clone());
					// msg.setData(data);
					// bettingDatabaseThread.handler.sendMessage(msg);

					clearSelectedBallState();
					handler.sendEmptyMessage(0x125);

					chooseballAnimImageView.clearAnimation();
					chooseballAnimImageView.startAnimation(chooseballBetAnim);
				}

				break;

			case R.id.bet_btn:
				if (btnBetting.getText().equals("确认修改")) {
					int redBallCount = ssqLottery.redBallSelectedList.size();
					int blueBallCount = ssqLottery.blueBallSelectedList.size();
					int position = getIntent().getIntExtra("Position", 0);
					String[] olderBettingContent = bettingSlips.get(position);
					if (redBallCount >= 6 && blueBallCount >= 1) {
						String[] bettingContentModified = formatBettingContent(ssqLottery.redBallSelectedList,
								ssqLottery.blueBallSelectedList);
						bettingSlips.set(position, bettingContentModified);
						Intent intent = new Intent();
						intent.putExtra("isModified", true);
						intent.putExtra("BettingCount",
								Integer.parseInt(bettingContentModified[1]) - Integer.parseInt(olderBettingContent[1]));
						intent.putExtra("BettingMoney", new BigDecimal(bettingContentModified[3])
								.subtract(new BigDecimal(olderBettingContent[3])));
						ChooseSsqActivity.this.setResult(0, intent);
						ChooseSsqActivity.this.finish();
					} else if (redBallCount == 0 && blueBallCount == 0) {
						bettingSlips.remove(position);
						Intent intent = new Intent();
						intent.putExtra("isModified", true);
						intent.putExtra("BettingCount", -Integer.parseInt(olderBettingContent[1]));
						intent.putExtra("BettingMoney",
								BigDecimal.ZERO.subtract(new BigDecimal(olderBettingContent[3])));
						ChooseSsqActivity.this.setResult(0, intent);
						ChooseSsqActivity.this.finish();
					} else {
						msgBetting.setTextColor(Color.YELLOW);
					}
				} else {
					int redBallCount = ssqLottery.redBallSelectedList.size();
					int blueBallCount = ssqLottery.blueBallSelectedList.size();
					if (redBallCount >= 6 && blueBallCount >= 1) {
						String[] bettingContent = formatBettingContent(ssqLottery.redBallSelectedList,
								ssqLottery.blueBallSelectedList);
						bettingSlips.add(0, bettingContent);

						clearSelectedBallState();
						handler.sendEmptyMessage(0x125);

						chooseballAnimImageView.clearAnimation();
						Intent intent = new Intent(ChooseSsqActivity.this, SsqBettingSlipActivity.class);
						startActivity(intent);
					} else if (redBallCount == 0 && blueBallCount == 0 && bettingSlips.size() > 0) {
						chooseballAnimImageView.clearAnimation();
						Intent intent = new Intent(ChooseSsqActivity.this, SsqBettingSlipActivity.class);
						startActivity(intent);
					} else {
						msgBetting.setTextColor(Color.YELLOW);
					}
				}
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float[] values = event.values;
		if ((Math.abs(values[0]) > 15 || Math.abs(values[1]) > 15 || Math.abs(values[2]) > 15)
				&& System.currentTimeMillis() - currentTime > 1000) {
			vibrator.vibrate(200);
			clearSelectedBallState();
			updateRandomBettedBallState(SsqLottery.randomBetting());
			handler.sendEmptyMessage(0x125);
			currentTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public void back(View v) {
		onBackPressed();
	}

	// 实现状态栏的背景与APP内容色彩的平滑对接，此实现方式主要用于5.0以内，
	// 5.0以上有更好的接口支持
	public void updateStatusBarDisplay() {
		Window mWindow = getWindow();
		ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
		// mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		// mWindow.addFlags(Window.FEATURE_NO_TITLE);
		mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		View contentView = mContentView.getChildAt(0);
		// 只有当fitSystemWindows为true时，才能在软键盘弹出时适应屏幕的布局
		// android:windowSoftInputMode="adjustResize"才起效
		contentView.setFitsSystemWindows(true);
		int statusBarHeight = getStatusBarHeight();

		// ViewGroup contentParent = (ViewGroup) mContentView.getParent();
		// if (contentParent != null) {
		// View mview = new View(this);
		// LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
		// statusBarHeight);
		// mview.setLayoutParams(params);
		// mview.setBackgroundColor(getResources().getColor(R.color.red));
		// contentParent.addView(mview, 0);
		// }

		View mView = new View(this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, statusBarHeight);
		mView.setLayoutParams(params);
		mView.setBackgroundColor(getResources().getColor(R.color.red));
		mContentView.addView(mView, 0);
		MarginLayoutParams mlParams = (MarginLayoutParams) contentView.getLayoutParams();
		mlParams.topMargin = statusBarHeight;
		contentView.setLayoutParams(mlParams);
	}

	public int getStatusBarHeight() {
		int statusBarHeight = 0;
		// 源码/frameworks/base/core/res/res/values/dimens.xml中数据
		int statusbarId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (statusbarId > 0) {
			statusBarHeight = getResources().getDimensionPixelSize(statusbarId);
		}
		return statusBarHeight;
	}

	public int getNavigationBarHeight() {
		int height = 0;
		int navigationBarId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
		if (navigationBarId > 0) {
			height = getResources().getDimensionPixelSize(navigationBarId);
		}
		return height;
	}

	public void loadNextOpenDate() {
		new Thread() {
			String httpUrl = "http://apis.baidu.com/apistore/lottery/lotteryquery";
			String httpArgs = "lotterycode=ssq&recordcnt=1";

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String result = request(httpUrl, httpArgs);
				if (result.equals("")) {
					// nothing to do;
				} else {
					try {
						JSONObject JsonResult = new JSONObject(result);
						int errorId = JsonResult.getInt("errNum");
						String retMsg = JsonResult.getString("retMsg");
						if (errorId != 0 || !retMsg.equals("success")) {
							Message msg = handler.obtainMessage(0x127);
							Bundle data = new Bundle();
							data.putString("retMsg", retMsg);
							msg.setData(data);
							msg.sendToTarget();
						} else {
							JSONObject retData = JsonResult.getJSONObject("retData");
							JSONArray data = retData.getJSONArray("data");
							JSONObject firstData = data.getJSONObject(0);
							String expect = firstData.getString("expect");
							long openTimeStamp = firstData.getLong("openTimeStamp");
							String nextExpect = String.valueOf(Integer.parseInt(expect) + 1);
							SimpleDateFormat sdf = new SimpleDateFormat();
							sdf.applyLocalizedPattern("yyyy-MM-dd");

							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(openTimeStamp * 1000);
							int currentWeek = calendar.get(Calendar.DAY_OF_WEEK);
							switch (currentWeek) {
							case 1:// Sun
							case 3:// Tue
								openTimeStamp = (openTimeStamp + 2 * 24 * 60 * 60) * 1000;
								break;

							case 5:// Thu
								openTimeStamp = (openTimeStamp + 3 * 24 * 60 * 60) * 1000;
								break;
							default:
								break;
							}
							String nextOpenDate = sdf.format(openTimeStamp);
							Bundle bundle = new Bundle();
							bundle.putString("expect", nextExpect);
							bundle.putString("openDate", nextOpenDate);
							Message msg = handler.obtainMessage(0x128);
							msg.setData(bundle);
							msg.sendToTarget();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	public String request(String httpUrl, String httpArgs) {
		String result = "";
		String httpURLStr = httpUrl + "?" + httpArgs;
		HttpURLConnection con = null;
		BufferedReader reader = null;
		try {
			StringBuilder strBuilder = new StringBuilder();
			URL url = new URL(httpURLStr);
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("apikey", ResourceUtils.getApikey(this));
			con.setConnectTimeout(2000);
			con.connect();
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				strBuilder.append(line);
				strBuilder.append("\r\n");
			}
			result = strBuilder.toString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("MalformedURLException error:");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("IOException error:");
		} finally {
			if (con != null) {
				con.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}

		return result;
	}

	// //Test ExpandableListView
	// public void initLastestTenOpenCode(){
	// final String[] groupStr = new String[]{"test01", "test02"};
	// final String[][] childStr = new String[][]{{"test01_child01",
	// "test01_child02", "test01_child02", "test01_child02", "test01_child02",
	// "test01_child02", "test01_child02"}, {"test02_child01",
	// "test02_child02"}};
	// ExpandableListView expandableList =
	// (ExpandableListView)findViewById(R.id.last_ten_results);
	// ExpandableListAdapter expandableAdapter = new BaseExpandableListAdapter()
	// {
	//
	// @Override
	// public boolean isChildSelectable(int groupPosition, int childPosition) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	//
	// @Override
	// public boolean hasStableIds() {
	// // TODO Auto-generated method stub
	// return true;
	// }
	//
	// @Override
	// public View getGroupView(int groupPosition, boolean isExpanded, View
	// convertView, ViewGroup parent) {
	// // TODO Auto-generated method stub
	// if (convertView == null) {
	// TextView view = new TextView(getApplicationContext());
	// LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
	// LayoutParams.WRAP_CONTENT);
	// view.setPadding(10, 10, 10, 10);
	// view.setGravity(Gravity.CENTER);
	// view.setTextColor(getResources().getColor(R.color.black));
	// view.setLayoutParams(params);
	// convertView = view;
	// }
	// ((TextView)convertView).setText(getGroup(groupPosition));
	// return convertView;
	// }
	//
	// @Override
	// public long getGroupId(int groupPosition) {
	// // TODO Auto-generated method stub
	// return groupPosition;
	// }
	//
	// @Override
	// public int getGroupCount() {
	// // TODO Auto-generated method stub
	// return groupStr.length;
	// }
	//
	// @Override
	// public String getGroup(int groupPosition) {
	// // TODO Auto-generated method stub
	// return groupStr[groupPosition];
	// }
	//
	// @Override
	// public int getChildrenCount(int groupPosition) {
	// // TODO Auto-generated method stub
	// return childStr[groupPosition].length;
	// }
	//
	// @Override
	// public View getChildView(int groupPosition, int childPosition, boolean
	// isLastChild, View convertView,
	// ViewGroup parent) {
	// // TODO Auto-generated method stub
	// if (convertView == null) {
	// TextView view = new TextView(getApplicationContext());
	// LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
	// LayoutParams.WRAP_CONTENT);
	// view.setPadding(10, 10, 10, 10);
	// view.setGravity(Gravity.CENTER);
	// view.setTextColor(getResources().getColor(R.color.black));
	// view.setLayoutParams(params);
	// convertView = view;
	// }
	// ((TextView)convertView).setText(getChild(groupPosition, childPosition));
	// return convertView;
	// }
	//
	// @Override
	// public long getChildId(int groupPosition, int childPosition) {
	// // TODO Auto-generated method stub
	// return childPosition;
	// }
	//
	// @Override
	// public String getChild(int groupPosition, int childPosition) {
	// // TODO Auto-generated method stub
	// return childStr[groupPosition][childPosition];
	// }
	// };
	//
	// expandableList.setAdapter(expandableAdapter);
	// }

	public void addAdapter() {
		adapter = new BaseAdapter() {
			class ViewHolder {
				TextView expectNo;
				TextView openCodeView;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.ssq_lastest_ten_open_code_list_cell, parent, false);
					ViewHolder holder = new ViewHolder();
					holder.expectNo = (TextView) convertView.findViewById(R.id.expect_no);
					holder.openCodeView = (TextView) convertView.findViewById(R.id.open_code);
					convertView.setTag(holder);
				}
				ViewHolder holder = (ViewHolder) convertView.getTag();
				holder.expectNo.setText(getItem(position)[0]);
				holder.openCodeView.setText(getItem(position)[1]);

				switch (position % 2) {
				case 0:
					convertView.setBackgroundColor(getResources().getColor(R.color.lightCyan1));
					break;

				case 1:
					convertView.setBackgroundColor(getResources().getColor(R.color.lightCyan2));
					break;
				default:
					break;
				}
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public String[] getItem(int position) {
				// TODO Auto-generated method stub
				return openCodes.get(position);
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return openCodes.size();
			}
		};
		tenListView.setAdapter(adapter);
	}

	// 设置一个OnTouchListener,用于实现顶部“最近十次开奖List”的抽屉效果。
	public void setOnTouchListenerForScroll() {
		selectBallScrollView.setOnTouchListener(new OnTouchListener() {

			float[] location = null;
			float offsetY = 0;
			boolean isShowingOpenCode = false; // 在滑动scrollView前，首先确认openCode
												// list是否正处在显示状态，
												// if true，滑动只能控制openCode
												// list的打开与关闭
			int scrollDirection = 0;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
					// 当显示popupwindow时，手指产生move事件会由scrollView来处理，
					// 因此在此dismiss popupWindow。
					if (ssqLottery.getPopupWindow() != null && ssqLottery.getPopupWindow().isShowing()) {
						ssqLottery.getPopupWindow().dismiss();
						LotteryBallContainerTableLayout.popupDeque.clear();
					}
				}

				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (v.getScrollY() == 0) {
						if (location == null) {
							offsetY = 0;
						} else {
							offsetY = event.getY() - location[1];
						}

						if (offsetY > 0) {
							if (offsetY >= 1) {
								LayoutParams params = tenListView.getLayoutParams();
								if (params.height == getOpenCodeListMaxHeight()) {
									// nothing to do
								} else {
									scrollDirection = 1;
									if (task != null) {
										task.cancel(true);
									}
									if ((params.height + offsetY) >= getOpenCodeListMaxHeight()) {

										params.height = getOpenCodeListMaxHeight();
										pullDownView.setImageDrawable(
												getResources().getDrawable(R.drawable.chooseball_pull_icon_up));
									} else {
										params.height += offsetY;
									}
									tenListView.setLayoutParams(params);
								}
								if (tenListView.getVisibility() == View.GONE) {
									tenListView.setVisibility(View.VISIBLE);
								}

								// 为加载数据而提供的显示效果
								if (!isLoaded) {
									if (loadingHeader.getVisibility() == View.GONE) {
										loadingHeader.setVisibility(View.VISIBLE);
									}
									LayoutParams loadParams = loadingHeader.getLayoutParams();
									if ((loadParams.height + offsetY) >= getLoadingDisplayMaxHeight()) {
										showLoadingDisplay();
									} else {
										loadParams.height += offsetY;
										loadingHeader.setLayoutParams(loadParams);
									}
								}
								location[1] = event.getY();
							}
							return true;
						} else if (offsetY < 0) {

							if (tenListView.getVisibility() != View.GONE) {
								if (Math.abs(offsetY) >= 1) {
									scrollDirection = -1;
									if (task != null) {
										task.cancel(true);
									}
									LayoutParams params = tenListView.getLayoutParams();
									if ((params.height + offsetY) < 0) {
										params.height = 0;
										tenListView.setVisibility(View.GONE);
										pullDownView.setImageDrawable(
												getResources().getDrawable(R.drawable.chooseball_pull_icon_down));
									} else {
										params.height += offsetY;
									}
									tenListView.setLayoutParams(params);

									if (!isLoaded) {
										LayoutParams loadParams = loadingHeader.getLayoutParams();
										if ((loadParams.height + offsetY) < 0) {
											loadParams.height = 0;
											loadingHeader.setLayoutParams(loadParams);
										} else {
											loadParams.height += offsetY;
											loadingHeader.setLayoutParams(loadParams);
										}
									}

									location[1] = event.getY();
								}
								return true;
							} else if (isShowingOpenCode) {
								// nothing to do, prevent pass the touch event
								return true;
							}

						} else {
							if (location == null) {
								location = new float[2];
								location[1] = event.getY();
								isShowingOpenCode = tenListView.getVisibility() != View.GONE;
								return true;
							}
							if (tenListView.getVisibility() != View.GONE) {
								return true;
							}
						}
					} else {
						if (location != null) {
							location = null;
							scrollDirection = 0;
						}
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (location != null) {
						location = null;
						if (tenListView.getVisibility() != View.GONE) {
							LayoutParams params = tenListView.getLayoutParams();
							task = new ShowOpenCodeTask();
							task.execute(scrollDirection, params.height);
						}
						scrollDirection = 0;
						return true;
					}
				}
				return false;
			}
		});

	}

	public void loadLatestTenOpenCode() {
		new Thread() {

			String httpUrl = "http://apis.baidu.com/apistore/lottery/lotteryquery";
			String httpArgs = "lotterycode=ssq&recordcnt=10";

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ArrayList<String[]> openCodes = new ArrayList<String[]>();
				String result = request(httpUrl, httpArgs);
				if (result.equals("")) {
					// nothing to do;
				} else {
					try {
						JSONObject JsonResult = new JSONObject(result);
						int errorId = JsonResult.getInt("errNum");
						String retMsg = JsonResult.getString("retMsg");
						if (errorId != 0 || !retMsg.equals("success")) {
							Message msg = handler.obtainMessage(0x127);
							Bundle data = new Bundle();
							data.putString("retMsg", retMsg);
							msg.setData(data);
							msg.sendToTarget();
						} else {
							JSONObject retData = JsonResult.getJSONObject("retData");
							int count = retData.getInt("recordCnt");
							JSONArray datas = retData.getJSONArray("data");
							for (int i = 0; i < count; i++) {
								JSONObject data = datas.getJSONObject(i);
								String expect = data.getString("expect");
								String openCode = data.getString("openCode");
								String[] expectAndCode = new String[2];
								expectAndCode[0] = expect;
								expectAndCode[1] = openCode;
								openCodes.add(expectAndCode);
							}
							// ChooseSsqActivity.this.openCodes = openCodes;

						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Message msg = handler.obtainMessage(0x129, openCodes);
				msg.sendToTarget();
			}

		}.start();
	}

	public void showLoadingDisplay() {
		isLoaded = true;
		if (loadingHeader.getVisibility() == View.GONE) {
			loadingHeader.setVisibility(View.VISIBLE);
		}
		LayoutParams loadParams = loadingHeader.getLayoutParams();
		loadParams.height = getLoadingDisplayMaxHeight();
		loadingHeader.setLayoutParams(loadParams);
		loadingHeaderTxt.setVisibility(View.GONE);
		loadingHeaderAnim.setVisibility(View.VISIBLE);
		loadingHeaderAnimImg.startAnimation(loadingImgAnim);
		loadLatestTenOpenCode();
	}

	public void hideLoadingDisplay() {
		LayoutParams params = loadingHeader.getLayoutParams();
		params.height = 0;
		loadingHeader.setLayoutParams(params);
		loadingHeader.setVisibility(View.GONE);
		loadingHeaderTxt.setVisibility(View.GONE);
		loadingHeaderAnim.setVisibility(View.GONE);
		;
		loadingHeaderAnimImg.clearAnimation();
	}

	// 下拉动画效果
	class ShowOpenCodeTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			int count = params.length;
			int listHeight = 0;
			if (count == 2) {
				int listMaxHeight = getOpenCodeListMaxHeight();
				int orientation = params[0];
				listHeight = params[1];
				if (orientation > 0) {
					// 向下滑动
					while (listHeight < listMaxHeight && !isCancelled()) {
						listHeight = (listHeight + 10) < listMaxHeight ? (listHeight + 10) : listMaxHeight;
						this.publishProgress(listHeight);
						long time = System.currentTimeMillis();
						while (System.currentTimeMillis() - time < 4) {
							// 理论每4毫秒更新一次，
							// 这里不用Thread.sleep(4),是因为sleep会阻塞此线程，下次执行不确定是多久以后
							// 时间是不可控的。
						}
					}
				} else if (orientation < 0) {
					// 向上滑动
					while (listHeight > 0) {
						listHeight = (listHeight - 10) > 0 ? (listHeight - 10) : 0;
						publishProgress(listHeight);
						long time = System.currentTimeMillis();
						while (System.currentTimeMillis() - time < 4) {
							// 理论每4毫秒更新一次，
							// 这里不用Thread.sleep(4),是因为sleep会阻塞此线程，下次执行不确定是多久以后
							// 时间是不可控的。
						}
					}
				}
			}
			return listHeight;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			if (values != null) {
				if (tenListView.getVisibility() == View.GONE) {
					tenListView.setVisibility(View.VISIBLE);
				}
				LayoutParams params = tenListView.getLayoutParams();
				params.height = values[0];
				tenListView.setLayoutParams(params);

				if (!isLoaded) {
					if (loadingHeader.getVisibility() == View.GONE) {
						loadingHeader.setVisibility(View.VISIBLE);
					}
					LayoutParams loadParams = loadingHeader.getLayoutParams();
					if (values[0] >= getLoadingDisplayMaxHeight()) {
						showLoadingDisplay();
					} else {
						loadParams.height = values[0];
						loadingHeader.setLayoutParams(loadParams);
					}
				}
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if (result <= 0) {
				tenListView.setVisibility(View.GONE);
				pullDownView.setImageDrawable(getResources().getDrawable(R.drawable.chooseball_pull_icon_down));
			} else {
				pullDownView.setImageDrawable(getResources().getDrawable(R.drawable.chooseball_pull_icon_up));
			}
		}

	}

	public int getWindowHeight() {
		WindowManager wm = getWindowManager();
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);

		return metrics.heightPixels;
	}

	public float getDensity() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.density;
	}

	public int getOpenCodeListMaxHeight() {
		return (int) (Math.min(253 * getDensity(), getWindowHeight() / 2));
	}

	public int getLoadingDisplayMaxHeight() {
		return (int) Math.min(100 * getDensity(), getWindowHeight());
	}

	class MTextWatcher implements TextWatcher {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			// 限定EditText只能输入3个数字，对s的处理同时会触发此listener
			if (s.length() > 3) {
				s.replace(0, s.length(), "999");
			} else {
				if (s.length() == 0 || s.toString().equals("1")) {
					if (btnMulMinus.isClickable()) {
						btnMulMinus.setImageResource(R.drawable.bg_chooseball_bottom_btn_mul_left_gray);
						btnMulMinus.setClickable(false);
					}
				} else {
					if (!btnMulMinus.isClickable()) {
						btnMulMinus.setImageResource(R.drawable.bg_chooseball_bottom_btn_mul_left);
						btnMulMinus.setClickable(true);
					}
				}
				if (s.toString().equals("999")) {
					if (btnMulPlus.isClickable()) {
						btnMulPlus.setImageResource(R.drawable.bg_chooseball_bottom_btn_mul_right_gray);
						btnMulPlus.setClickable(false);
					}
				} else {
					if (!btnMulPlus.isClickable()) {
						btnMulPlus.setImageResource(R.drawable.bg_chooseball_bottom_btn_mul_right);
						btnMulPlus.setClickable(true);
					}
				}
				handler.sendEmptyMessage(0x126);
			}
		}
	}
}
