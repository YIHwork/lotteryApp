package com.hy.mylottery.lotteryType;

import java.util.ArrayDeque;
import java.util.ArrayList;

import com.hy.mylottery.lotteryType.LotteryBallType.LotteryBallAppearance;
import com.hy.mylottery.lotteryType.LotteryBallType.LotteryBallAppearance.LotteryBallAppearanceHolder;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class LotteryBallContainerTableLayout extends TableLayout {

	private static final int COLUMN_COUNT_DEFAULT = 7;
	public static ArrayDeque<View> popupDeque = new ArrayDeque<View>();

	public LotteryBallContainerTableLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public LotteryBallContainerTableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public void addElementsForBallContainer(LotteryBallType ballLottery, int styleId, final Handler handler) {
		removeAllViews();
		final LotteryBallAppearance ballAppearance = ballLottery.getAppearanceStyle(styleId);
		ArrayList<Integer> bettingList = ballLottery.getBettingList(styleId);
		int ballCount = ballAppearance.getBallCount();
		int column = COLUMN_COUNT_DEFAULT;
		int row;
		if (ballCount % column > 0) {
			row = ballCount / column + 1;
		} else if (ballCount % column == 0) {
			row = ballCount / column;
		} else {
			Toast.makeText(getContext(), "row error, cannot <0", Toast.LENGTH_SHORT).show();
			return;
		}
		for (int i = 0; i < row; i++) {
			TableRow tableRow = new TableRow(getContext());
			for (int j = 0; j < Math.min(column, ballCount - i * column); j++) {
				int position = column * i + j;
				View contentView = ballAppearance.getView("" + ballAppearance.getBallNum(position));
				if (bettingList.size() != 0 && bettingList.contains(position)) {
					ballAppearance.updateSelectedBallState(contentView);
				}
				tableRow.addView(contentView);
				contentView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							// ballAppearance.preparePopupWindow(v);
							if (LotteryBallAppearance.popupWindow == null
									|| !LotteryBallAppearance.popupWindow.isShowing()) {
								ballAppearance.showPopupWindow(v);
							} else {
								popupDeque.add(v);
							}
							return true;
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							// 判断ACTION_UP的位置是否在点击的view中，
							// 如果没有，并且此时popupWindow依然处于showing状态，那么dismiss
							// popupWindow
							// 主要运用于touch出popupWindow，手指移出此view外后dismiss
							// popupWindow的场景;
							LotteryBallAppearanceHolder holder = LotteryBallAppearance.getHolderOfBallView(v);
							if (!(event.getX() < 0 || event.getX() > v.getWidth() || event.getY() < 0
									|| event.getY() > v.getHeight())) {
								v.performClick();
							}
							if (holder.ballPopup != null && holder.ballPopup.isShowing()) {
								Message msg = new Message();
								msg.what = 0x123; // popupWindow dismiss message
								msg.obj = holder.ballPopup;
								handler.sendMessage(msg);

								View mv = popupDeque.pollFirst();
								if (mv != null) {
									ballAppearance.showPopupWindow(mv);
								}
							} else {
								popupDeque.remove(v);
							}
							return true;
						}
						// ScrollView scroll =
						// (ScrollView)v.getParent().getParent().getParent().getParent().getParent();
						return true;
					}
				});

				contentView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(final View v) {
						// TODO Auto-generated method stub

						ballAppearance.updateSelectedBallState(v);
						handler.sendEmptyMessage(0x125);// update the state of
														// random_button
					}
				});
			}
			addView(tableRow);
		}
	}

	public View getBallView(int position) {
		int columnNum = position % COLUMN_COUNT_DEFAULT;
		int rowNum;
		if (columnNum > 0) {
			rowNum = position / COLUMN_COUNT_DEFAULT + 1;
		} else {
			rowNum = position / COLUMN_COUNT_DEFAULT;
			columnNum = COLUMN_COUNT_DEFAULT;
		}
		ViewGroup rowGroup = (ViewGroup) getChildAt(rowNum - 1);
		if (rowGroup != null) {
			return rowGroup.getChildAt(columnNum - 1);
		}
		return null;
	}
}
