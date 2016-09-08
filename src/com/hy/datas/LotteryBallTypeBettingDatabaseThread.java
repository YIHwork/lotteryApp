package com.hy.datas;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.hy.mylottery.ChooseSsqActivity;
import com.hy.mylottery.R;
import com.hy.mylottery.SsqBettingSlipActivity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LotteryBallTypeBettingDatabaseThread extends Thread {

	public Handler handler;
	Handler.Callback mcallback;

	public LotteryBallTypeBettingDatabaseThread() {
		// TODO Auto-generated constructor stub
		System.out.println("new thread");
		mcallback = new SsqDefaultHandlerCallback();
	}

	public LotteryBallTypeBettingDatabaseThread(Handler.Callback callback) {
		this();
	}

	public LotteryBallTypeBettingDatabaseThread(Runnable runnable) {
		super(runnable);
		// TODO Auto-generated constructor stub
		mcallback = new SsqDefaultHandlerCallback();
	}

	public LotteryBallTypeBettingDatabaseThread(String threadName) {
		super(threadName);
		// TODO Auto-generated constructor stub
		mcallback = new SsqDefaultHandlerCallback();
	}

	public LotteryBallTypeBettingDatabaseThread(Runnable runnable, String threadName) {
		super(runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public LotteryBallTypeBettingDatabaseThread(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
		// TODO Auto-generated constructor stub
	}

	public LotteryBallTypeBettingDatabaseThread(ThreadGroup group, String threadName) {
		super(group, threadName);
		// TODO Auto-generated constructor stub
		mcallback = new SsqDefaultHandlerCallback();
	}

	public LotteryBallTypeBettingDatabaseThread(ThreadGroup group, Runnable runnable, String threadName) {
		super(group, runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public LotteryBallTypeBettingDatabaseThread(ThreadGroup group, Runnable runnable, String threadName,
			long stackSize) {
		super(group, runnable, threadName, stackSize);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();
		handler = new Handler(mcallback);
		Looper.loop();
	}

	private class SsqDefaultHandlerCallback implements Handler.Callback {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0x1111:
				LotteryBallTypeSQLiteOpenHelper sqliteOpenHelper = (LotteryBallTypeSQLiteOpenHelper) msg.obj;
				Bundle data = msg.getData();
				ArrayList<Integer> redBallList = data.getIntegerArrayList("redBallList");
				ArrayList<Integer> blueBallList = data.getIntegerArrayList("blueBallList");
				int redBallCount = redBallList.size();
				int blueBallCount = blueBallList.size();
				String edMultipleText = data.getString("edMultipleText");

				BigDecimal bettingCount = ChooseSsqActivity.factorial(redBallCount)
						.divide(ChooseSsqActivity.factorial(6)).divide(ChooseSsqActivity.factorial(redBallCount - 6))
						.multiply(BigDecimal.valueOf(blueBallCount));
				int bettingMultiple = edMultipleText.equals("") ? 1 : Integer.parseInt(edMultipleText);
				BigDecimal bettingMoney = bettingCount.multiply(BigDecimal.valueOf(2 * bettingMultiple));
				ChooseSsqActivity.selectedBallSort(redBallList, 0, redBallCount - 1);
				ChooseSsqActivity.selectedBallSort(blueBallList, 0, blueBallCount - 1);
				StringBuilder bettingBallStringBuilder = new StringBuilder();
				for (int i = 0; i < redBallCount; i++) {
					if (redBallList.get(i) < 10) {
						bettingBallStringBuilder.append("0" + redBallList.get(i) + " ");
					} else
						bettingBallStringBuilder.append(redBallList.get(i) + " ");
				}
				bettingBallStringBuilder.append("|");
				for (int i = 0; i < blueBallCount; i++) {
					if (blueBallList.get(i) < 10) {
						bettingBallStringBuilder.append(" 0" + blueBallList.get(i));
					} else
						bettingBallStringBuilder.append(" " + blueBallList.get(i));
				}
				sqliteOpenHelper.getWritableDatabase().execSQL(
						"insert into " + sqliteOpenHelper.CACHE_TABLE_NAME + " values(null, ?, ?, ?, ?)", new Object[] {
								bettingBallStringBuilder, bettingCount, bettingMultiple, bettingMoney.toString() });
				break;

			case 0x1112:
				SsqBettingSlipActivity slipActivity = (SsqBettingSlipActivity) msg.obj;
				final LinearLayout mContainer = new LinearLayout(slipActivity);
				mContainer.setOrientation(LinearLayout.VERTICAL);
				Handler resultHandler = slipActivity.mhandler;
				LotteryBallTypeSQLiteOpenHelper databaseHelper = new LotteryBallTypeSQLiteOpenHelper(slipActivity,
						ChooseSsqActivity.CACHE_DATABASE_NAME, null, 1);
				Cursor result = databaseHelper.getReadableDatabase()
						.rawQuery("select * from " + databaseHelper.CACHE_TABLE_NAME, null);
				System.out.println("the count is " + result.getCount());
				if (result.getCount() <= 0) {
					System.out.println("the getCount <= 0");
					Message resultMsg = Message.obtain(resultHandler);
					resultMsg.what = 0x222;
					resultMsg.arg1 = View.VISIBLE;
					resultMsg.sendToTarget();
				} else {
					while (result.moveToNext()) {
						System.out.println("the cursor");
						final View rootView = showInBettingList(slipActivity, result.getInt(0), result.getString(1),
								result.getInt(2), result.getInt(3), result.getString(4));
						mContainer.addView(rootView);

					}

					Message resultMsg = new Message();
					resultMsg.what = 0x223;
					resultMsg.obj = mContainer;
					resultHandler.sendMessage(resultMsg);
				}
				result.close();
				databaseHelper.close();
				System.out.println("0x1112");
				break;
			case 0x113:
				System.out.println("0x113");
				System.out.println("the current thread " + currentThread().getId());
				// android.os.Process.sendSignal(android.os.Process.myPid(),
				// android.os.Process.SIGNAL_KILL);
				break;
			default:
				break;
			}
			return true;
		}
	}

	private View showInBettingList(Context context, int id, String bettingNum, int bettingCount, int bettingMultiple,
			String bettingMoney) {
		View rootView = LayoutInflater.from(context).inflate(R.layout.ssq_betting_slip_list_content, null);
		TextView bettingNumView = (TextView) rootView.findViewById(R.id.ssq_betting_slip_list_num);
		TextView bettingMultipleMsg = (TextView) rootView.findViewById(R.id.ssq_betting_slip_list_multiple_msg);

		SpannableStringBuilder bettingNumString = new SpannableStringBuilder(bettingNum);
		int redBallEndSite = bettingNum.indexOf("|");

		ForegroundColorSpan foregroundColor = new ForegroundColorSpan(
				context.getResources().getColor(R.color.lottery_ball_red));
		bettingNumString.setSpan(foregroundColor, 0, redBallEndSite, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		foregroundColor = new ForegroundColorSpan(context.getResources().getColor(R.color.lottery_ball_blue));
		bettingNumString.setSpan(foregroundColor, redBallEndSite + 1, bettingNumString.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		bettingNumView.setText(bettingNumString);

		rootView.setTag(id);
		String bettingMultipleMsgString = String.format(
				context.getResources().getString(R.string.ssq_betting_slip_list_multiple_msg_format),
				bettingCount > 1 ? "¸´" : "µ¥", String.valueOf(bettingCount), String.valueOf(bettingMultiple),
				bettingMoney);
		bettingMultipleMsg.setText(bettingMultipleMsgString);
		return rootView;
	}
}
