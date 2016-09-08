package com.hy.mylottery;

import java.math.BigDecimal;

import com.hy.mylottery.lotteryType.SsqLottery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SsqBettingSlipActivity extends Activity {

	LayoutInflater inflater;
	public Handler mhandler; // deprecated

	TextView bettingMsgView;
	Button bettingBtn;
	ImageView bettingPeriodMinus, bettingPeriodPlus;
	EditText edPeriod;
	ImageView bettingMultipleMinus, bettingMultiplePlus;
	EditText edMultiple;

	Button randomOneBtn, randomFiveBtn, randomTenBtn;
	ListView bettingListView;
	BaseAdapter bettingListViewAdapter;
	View header;
	View noDataView;

	boolean isDestroy = false;;
	int bettingCounts;
	BigDecimal bettingMoneys = BigDecimal.ZERO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ssq_betting_slip);
		inflater = getLayoutInflater();

		bettingPeriodMinus = (ImageView) findViewById(R.id.multi_period_minus);
		bettingPeriodPlus = (ImageView) findViewById(R.id.multi_period_plus);
		edPeriod = (EditText) findViewById(R.id.ed_multi_period);
		bettingMultipleMinus = (ImageView) findViewById(R.id.multiple_minus);
		bettingMultiplePlus = (ImageView) findViewById(R.id.multiple_plus);
		edMultiple = (EditText) findViewById(R.id.ed_multiple);
		bettingMsgView = (TextView) findViewById(R.id.msg_betting);

		bettingListView = (ListView) findViewById(R.id.ssq_betting_list);
		initListData();

		bettingBtn = (Button) findViewById(R.id.bet_btn);

		// mhandler = new Handler() {
		// @Override
		// public void handleMessage(Message msg) {
		// // TODO Auto-generated method stub
		// switch (msg.what) {
		// case 0x222:
		// noDataView.setVisibility(msg.arg1);
		// break;
		// default:
		// break;
		// }
		// }
		// };
		// databaseHelper = new LotteryBallTypeSQLiteOpenHelper(this,
		// ChooseSsqActivity.CACHE_DATABASE_NAME, null, 1);

		// 初始化多倍选择bar，包括追加期数、投注倍数。
		initMultipleBar();

		initRandomBtn();
		// 更新最终选择后的金额、期数、注数、倍数。
		updateBettingMsg();
	}

	private void updateBettingMsg() {
		// bettingCounts = ChooseSsqActivity.bettingSlips.size();
		String bettingPeriod = edPeriod.getText().length() == 0 ? "1" : edPeriod.getText().toString();
		String bettingMultiple = edMultiple.getText().length() == 0 ? "1" : edMultiple.getText().toString();
		String bettingMoney = bettingMoneys.multiply(new BigDecimal(bettingPeriod))
				.multiply(new BigDecimal(bettingMultiple)).toString();

		String bettingMsgOriginal = String.format(getResources().getString(R.string.betting_slip_final_msg_format),
				bettingMoney, bettingPeriod, bettingCounts, bettingMultiple);
		int end = bettingMsgOriginal.indexOf("\n");
		SpannableString bettingMsg = new SpannableString(bettingMsgOriginal);
		ForegroundColorSpan moneySpan = new ForegroundColorSpan(getResources().getColor(R.color.orange));
		bettingMsg.setSpan(moneySpan, 0, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		bettingMsgView.setText(bettingMsg);
	}

	private void initMultipleBar() {
		OnClickListener multiClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String text;
				int count;
				switch (v.getId()) {
				case R.id.multi_period_minus:
					text = edPeriod.getText().toString();
					count = Integer.parseInt(text);
					edPeriod.setText(String.valueOf(--count));
					System.out.println("the count is " + count);
					break;
				case R.id.multiple_minus:
					text = edMultiple.getText().toString();
					count = Integer.parseInt(text);
					edMultiple.setText(String.valueOf(--count));
					break;
				case R.id.multi_period_plus:
					text = edPeriod.getText().toString();
					count = text.equals("") ? 1 : Integer.parseInt(text);
					edPeriod.setText(String.valueOf(++count));
					break;
				case R.id.multiple_plus:
					text = edMultiple.getText().toString();
					count = text.equals("") ? 1 : Integer.parseInt(text);
					edMultiple.setText(String.valueOf(++count));
					break;
				default:
					break;
				}
				edPeriod.setSelection(edPeriod.getText().length());
				edMultiple.setSelection(edMultiple.getText().length());
			}
		};
		bettingPeriodMinus.setOnClickListener(multiClickListener);
		bettingPeriodPlus.setOnClickListener(multiClickListener);
		bettingMultipleMinus.setOnClickListener(multiClickListener);
		bettingMultiplePlus.setOnClickListener(multiClickListener);
		bettingPeriodMinus.setClickable(false);
		bettingMultipleMinus.setClickable(false);

		OnKeyListener keyListener = new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					hideSoftInputMethod(v);
					return true;
				}
				return false;
			}
		};
		edPeriod.setOnKeyListener(keyListener);
		edMultiple.setOnKeyListener(keyListener);

		edPeriod.addTextChangedListener(new TextWatcher() {

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

				if (s.length() > 3) {
					s.replace(0, s.length(), "999");
				} else {
					if (s.length() == 0 || s.toString().equals("1")) {
						if (bettingPeriodMinus.isClickable()) {
							bettingPeriodMinus.setClickable(false);
							bettingPeriodMinus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_left_gray));
						}
					} else {
						if (!bettingPeriodMinus.isClickable()) {
							bettingPeriodMinus.setClickable(true);
							bettingPeriodMinus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_left));
						}
					}

					if (s.toString().equals("999")) {
						if (bettingPeriodPlus.isClickable()) {
							bettingPeriodPlus.setClickable(false);
							bettingPeriodPlus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_right_gray));
						}
					} else {
						if (!bettingPeriodPlus.isClickable()) {
							bettingPeriodPlus.setClickable(true);
							bettingPeriodPlus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_right));
						}
					}
				}
				updateBettingMsg();
			}
		});
		edMultiple.addTextChangedListener(new TextWatcher() {

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
				if (s.length() > 3) {
					s.replace(0, s.length(), "999");
				} else {
					if (s.length() == 0 || s.toString().equals("1")) {
						if (bettingMultipleMinus.isClickable()) {
							bettingMultipleMinus.setClickable(false);
							bettingMultipleMinus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_left_gray));
						}
					} else {
						if (!bettingMultipleMinus.isClickable()) {
							bettingMultipleMinus.setClickable(true);
							bettingMultipleMinus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_left));
						}
					}

					if (s.toString().equals("999")) {
						if (bettingMultiplePlus.isClickable()) {
							bettingMultiplePlus.setClickable(false);
							bettingMultiplePlus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_right_gray));
						}
					} else {
						if (!bettingMultiplePlus.isClickable()) {
							bettingMultiplePlus.setClickable(true);
							bettingMultiplePlus.setImageDrawable(
									getResources().getDrawable(R.drawable.bg_chooseball_bottom_btn_mul_right));
						}
					}
				}
				updateBettingMsg();
			}
		});
	}

	private void initRandomBtn() {
		randomOneBtn = (Button) findViewById(R.id.random_one);
		randomFiveBtn = (Button) findViewById(R.id.random_five);
		randomTenBtn = (Button) findViewById(R.id.random_ten);
		OnClickListener randomListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.random_one:
					randomBetting(1);
					break;

				case R.id.random_five:
					randomBetting(5);
					break;

				case R.id.random_ten:
					randomBetting(10);
					break;
				default:
					break;
				}
			}
		};
		randomOneBtn.setOnClickListener(randomListener);
		randomFiveBtn.setOnClickListener(randomListener);
		randomTenBtn.setOnClickListener(randomListener);
	}

	private void hideSoftInputMethod(View v) {
		InputMethodManager imeManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imeManager.isActive(v)) {
			imeManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			v.clearFocus();
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.dispatchTouchEvent(ev);
	}

	public void back(View v) {
		this.onBackPressed();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		isDestroy = true;
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == 0) {
			if (data != null && data.getBooleanExtra("isModified", false)) {
				bettingListViewAdapter.notifyDataSetChanged();
				bettingCounts += data.getIntExtra("BettingCount", 0);
				bettingMoneys = bettingMoneys.add((BigDecimal) data.getSerializableExtra("BettingMoney"));
				updateBettingMsg();
				if (ChooseSsqActivity.bettingSlips.size() <= 0) {
					noDataView.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void randomBetting(int repeat) {

		synchronized (ChooseSsqActivity.bettingSlips) {
			for (int i = 0; i < repeat && !isDestroy; i++) {
				int[] randomBetting = SsqLottery.randomBetting(); // 这里随机的号码是标准的有序号码（6红球
																	// + 1蓝球）
				StringBuilder randomNum = new StringBuilder();
				for (int j = 0; j < randomBetting.length; j++) {
					if (j == 6) {
						if (randomBetting[j] < 10) {
							randomNum.append("| 0" + randomBetting[j]);
						} else
							randomNum.append("| " + randomBetting[j]);
					} else {
						if (randomBetting[j] < 10) {
							randomNum.append("0" + randomBetting[j] + " ");
						} else
							randomNum.append(String.valueOf(randomBetting[j]) + " ");
					}
				}
				String[] bettingContent = new String[] { randomNum.toString(), "1", "1", "2" };
				ChooseSsqActivity.bettingSlips.add(0, bettingContent);
				bettingMoneys = bettingMoneys.add(BigDecimal.valueOf(2));
				bettingCounts++;
			}
			bettingListViewAdapter.notifyDataSetChanged();
			updateBettingMsg();
			if (noDataView.getVisibility() == View.VISIBLE) {
				noDataView.setVisibility(View.GONE);
			}
		}

	}

	private void initListData() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		final float density = metrics.density;

		View footer = inflater.inflate(R.layout.ssq_betting_slip_list_footer, null);
		noDataView = footer.findViewById(R.id.ssq_betting_slip_noData);
		header = inflater.inflate(R.layout.ssq_betting_slip_list_header, null);
		bettingListView.addHeaderView(header);
		bettingListView.addFooterView(footer);

		bettingListViewAdapter = new BaseAdapter() {
			class ConvertViewHolder {
				TextView bettingNumView;
				TextView bettingMsgView;
				ImageView deleteBtn;
			}

			ConvertViewHolder holder;

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				synchronized (ChooseSsqActivity.bettingSlips) {
					if (convertView == null) {
						holder = new ConvertViewHolder();
						convertView = inflater.inflate(R.layout.ssq_betting_slip_list_content, parent, false);
						holder.bettingNumView = (TextView) convertView.findViewById(R.id.ssq_betting_slip_list_num);
						holder.bettingMsgView = (TextView) convertView
								.findViewById(R.id.ssq_betting_slip_list_multiple_msg);
						holder.deleteBtn = (ImageView) convertView
								.findViewById(R.id.ssq_betting_slip_list_content_delete);
						convertView.setTag(holder);
					} else {
						holder = (ConvertViewHolder) convertView.getTag();
					}

					final String[] bettingContent = getItem(position);

					String bettingNum = bettingContent[0];
					String bettingCount = bettingContent[1];
					String bettingMultiple = bettingContent[2];
					String bettingMoney = bettingContent[3];

					SpannableString bettingNumSpan = new SpannableString(bettingNum);
					int redBallEndSite = bettingNum.indexOf("|");
					ForegroundColorSpan redSpan = new ForegroundColorSpan(
							getResources().getColor(R.color.lottery_ball_red));
					bettingNumSpan.setSpan(redSpan, 0, redBallEndSite, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ForegroundColorSpan blueSpan = new ForegroundColorSpan(
							getResources().getColor(R.color.lottery_ball_blue));
					bettingNumSpan.setSpan(blueSpan, redBallEndSite + 1, bettingNum.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					holder.bettingNumView.setText(bettingNumSpan);

					String bettingMsg = String.format(
							getResources().getString(R.string.ssq_betting_slip_list_multiple_msg_format),
							Integer.parseInt(bettingCount) > 1 ? "复" : "单", bettingCount, bettingMultiple,
							bettingMoney);
					holder.bettingMsgView.setText(bettingMsg);

					holder.deleteBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							synchronized (ChooseSsqActivity.bettingSlips) {

								bettingMoneys = bettingMoneys.subtract(new BigDecimal(getItem(position)[3]));
								bettingCounts += -1;
								ChooseSsqActivity.bettingSlips.remove(position);
								notifyDataSetChanged();
								updateBettingMsg();

								if (getCount() <= 0) {
									noDataView.setVisibility(View.VISIBLE);
								}
							}
						}
					});

					convertView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(SsqBettingSlipActivity.this, ChooseSsqActivity.class);
							intent.putExtra("Position", position);
							intent.putExtra("isSsqBettingSlipActivity", true);
							startActivityForResult(intent, 0);
						}
					});
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
				return ChooseSsqActivity.bettingSlips.get(position);
			}

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return ChooseSsqActivity.bettingSlips.size();
			}
		};
		bettingListView.setAdapter(bettingListViewAdapter);
		bettingListView.setOnTouchListener(new OnTouchListener() {

			float[] location;
			View headerContent = header.findViewById(R.id.header_content);
			ViewGroup.LayoutParams params = headerContent.getLayoutParams();
			int initHeaderHeight = params.height;
			int tempHeight;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (event.getAction() == MotionEvent.ACTION_MOVE) {

					if (bettingListView.getFirstVisiblePosition() == 0) {
						if (location == null) {
							location = new float[2];
							location[0] = event.getX();
							location[1] = event.getY();
						}
						float offsetY = event.getY() - location[1];
						if (offsetY > 0) {
							if (headerContent.getVisibility() == View.GONE) {
								headerContent.setVisibility(View.VISIBLE);
							}
							if (params.height <= 100 * density) {
								tempHeight = (int) (initHeaderHeight + offsetY);
								params.height = tempHeight;
								headerContent.setLayoutParams(params);
							} else if (params.height <= 150 * density) {
								params.height = (int) ((tempHeight + offsetY) / 2 > 150 * density ? 150 * density
										: (tempHeight + offsetY) / 2);
								headerContent.setLayoutParams(params);
							}

							return true;
						} else if (headerContent.getVisibility() != View.GONE) {
							params.height = initHeaderHeight;
							headerContent.setLayoutParams(params);
							headerContent.setVisibility(View.GONE);
							location = null;
						}
					} else if (location != null) {
						if (event.getY() - location[1] <= 0) {
							params.height = initHeaderHeight;
							headerContent.setLayoutParams(params);
							headerContent.setVisibility(View.GONE);
							location = null;
						}
					}

				} else if ((event.getAction() == MotionEvent.ACTION_CANCEL
						|| event.getAction() == MotionEvent.ACTION_UP) && location != null) {
					if (params.height - initHeaderHeight >= 50 * density) {
						params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
						headerContent.setLayoutParams(params);
						randomBetting(1);
						params.height = initHeaderHeight;
						headerContent.setLayoutParams(params);
					} else {
						params.height = initHeaderHeight;
						headerContent.setLayoutParams(params);
						headerContent.setVisibility(View.GONE);
					}
					location = null;
				}
				return false;
			}
		});

		int count = ChooseSsqActivity.bettingSlips.size();
		if (count <= 0) {
			noDataView.setVisibility(View.VISIBLE);
		} else {
			for (int i = 0; i < count; i++) {
				bettingMoneys = bettingMoneys.add(new BigDecimal(ChooseSsqActivity.bettingSlips.get(i)[3]));
				bettingCounts += Integer.parseInt(ChooseSsqActivity.bettingSlips.get(i)[1]);
			}
		}
	}
}
