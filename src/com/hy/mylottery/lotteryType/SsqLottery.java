package com.hy.mylottery.lotteryType;

import java.util.ArrayList;
import java.util.Random;

import com.hy.mylottery.R;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SsqLottery extends LotteryBallType {
	private static final int SSQ_STYLE_COUNT = 2;
	private static final int RED_BALL_NUMS = 33;
	private static final int BLUE_BALL_NUMS = 16;
	private static final int RED_BALL_STYLE_ID = 0;
	private static final int BLUE_BALL_STYLE_ID = 1;
	private static int[] redBallNums, blueBallNums;
	public ArrayList<Integer> redBallSelectedList;
	public ArrayList<Integer> blueBallSelectedList;
	Context context;

	static {
		redBallNums = new int[RED_BALL_NUMS];
		blueBallNums = new int[BLUE_BALL_NUMS];
		for (int i = 0; i < redBallNums.length; i++) {
			if (i < 9) {
				redBallNums[i] = i + 1;
			} else {
				redBallNums[i] = i + 1;
			}

			if (i < blueBallNums.length) {
				blueBallNums[i] = redBallNums[i];
			}
		}
	}

	// 包含参数的构造器创建的SsqLottery实例包含样式资源
	public SsqLottery(Context context) {
		this.context = context;
		redBallSelectedList = new ArrayList<Integer>();
		blueBallSelectedList = new ArrayList<Integer>();
	}

	/**
	 * 获取styleId所对应的外观样式(双色球有两种球，红球和蓝球),只能通过此方法获取SsqAppearance实例；
	 * 
	 * @param styleId
	 *            0代表红球样式，1代表蓝球样式
	 */
	@Override
	public SsqAppearance getAppearanceStyle(int styleId) {
		styleId = styleId % SSQ_STYLE_COUNT;
		SsqAppearance appearance = (SsqAppearance) super.getAppearanceStyle(styleId);
		if (appearance == null) {
			appearance = new SsqAppearance(context, styleId);
			putAppearanceStyle(styleId, appearance);
		}
		return appearance;
	}

	@Override
	public ArrayList<Integer> getBettingList(int styleId) {
		// TODO Auto-generated method stub
		switch (styleId) {
		case RED_BALL_STYLE_ID:
			return redBallSelectedList;
		case BLUE_BALL_STYLE_ID:
			return blueBallSelectedList;
		default:
			break;
		}
		return null;
	}

	public static int[] randomBetting() {
		int[] bettingNum = new int[7];
		Random random = new Random();
		for (int i = 0; i < bettingNum.length - 1; i++) {
			int randomNum = random.nextInt(33) + 1;
			bettingNum[i] = randomNum;
			if (i > 0 && randomNum > bettingNum[i - 1]) {
				continue;
			}
			for (int j = 0; j < i; j++) {
				if (randomNum == bettingNum[j]) {
					i--;
					break;
				} else if (randomNum < bettingNum[j]) {
					for (int j2 = i; j2 >= j + 1; j2--) {
						bettingNum[j2] = bettingNum[j2 - 1];
					}
					bettingNum[j] = randomNum;
					break;
				}
			}
		}
		bettingNum[6] = random.nextInt(16) + 1;// blue ball
		return bettingNum;
	}

	@Override
	public void betting() {
		// TODO Auto-generated method stub

	}

	private class SsqAppearance extends LotteryBallAppearance {

		private int[] popupSizeAndOffsetRelativeToPressedView;

		private SsqAppearance(Context context, int styleId) {
			super(context, styleId);
			// TODO Auto-generated constructor stub
		}

		/**
		 * 加载此彩票球外观样式所需的资源
		 * 
		 * @param styleId
		 *            彩票球外观样式ID;双色球包含两种样式，红球和蓝球，0代表红球，1代表蓝球
		 */
		@Override
		public void loaddingStyle(int styleId) {
			switch (styleId) {
			case RED_BALL_STYLE_ID:
				setBallNums(redBallNums);
				setPopupBgDrawable(
						context.getResources().getDrawable(R.drawable.lottery_ball_pressed_red_big_bg_border));
				setSelectedBallBgDrawable(
						context.getResources().getDrawable(R.drawable.lottery_ball_pressed_red_bg_border));
				setUnselectedBallTextColor(context.getResources().getColor(R.color.lottery_ball_red));
				break;
			case BLUE_BALL_STYLE_ID:
				setBallNums(blueBallNums);
				setPopupBgDrawable(
						context.getResources().getDrawable(R.drawable.lottery_ball_pressed_blue_big_bg_border));
				setSelectedBallBgDrawable(
						context.getResources().getDrawable(R.drawable.lottery_ball_pressed_blue_bg_border));
				setUnselectedBallTextColor(context.getResources().getColor(R.color.lottery_ball_blue));
				break;
			default:
				Toast.makeText(context, "error style", Toast.LENGTH_SHORT).show();
				break;
			}
			setUnselectedBallBgDrawable(context.getResources().getDrawable(R.drawable.ball));
			setSelectedBallTextColor(context.getResources().getColor(R.color.white));
		}

		/**
		 * 显示popupWindow;
		 * 
		 * @param pressedView
		 *            点击的目标视图, 此view是通过LotteryBallAppearance.getView()获取的,the
		 *            view must previously execute setTag(holder).
		 */
		public void showPopupWindow(View pressedView) {
			// TODO Auto-generated method stub
			if (popupSizeAndOffsetRelativeToPressedView == null) {
				popupSizeAndOffsetRelativeToPressedView = new int[4];
				int popupWidth = (pressedView.getHeight() - pressedView.getPaddingLeft()
						- pressedView.getPaddingRight()) * 83 / 51;
				int popupHeight = (pressedView.getHeight() - pressedView.getPaddingTop()
						- pressedView.getPaddingBottom()) * 147 / 51;
				int popupLocationX = pressedView.getWidth() / 2 - popupWidth / 2;
				int popupLocationY = -popupHeight - pressedView.getPaddingBottom();
				popupSizeAndOffsetRelativeToPressedView[0] = popupWidth;
				popupSizeAndOffsetRelativeToPressedView[1] = popupHeight;
				popupSizeAndOffsetRelativeToPressedView[2] = popupLocationX;
				popupSizeAndOffsetRelativeToPressedView[3] = popupLocationY;
			}
			LotteryBallAppearanceHolder holder = getHolderOfBallView(pressedView);
			holder.ballPopup.setWidth(popupSizeAndOffsetRelativeToPressedView[0]);
			holder.ballPopup.setHeight(popupSizeAndOffsetRelativeToPressedView[1]);

			holder.ballPopup.showAsDropDown(pressedView, popupSizeAndOffsetRelativeToPressedView[2],
					popupSizeAndOffsetRelativeToPressedView[3]);
			popupWindow = holder.ballPopup;
		}

		/**
		 * 更新目标球视图的状态，unselect/selected;
		 * 
		 * @param pressedView
		 *            点击的目标视图, 此view是通过LotteryBallAppearance.getView()获取的,the
		 *            view must previously execute setTag(holder).
		 */
		@Override
		public void updateSelectedBallState(View pressedView) {
			// TODO Auto-generated method stub
			LotteryBallAppearanceHolder holder = getHolderOfBallView(pressedView);
			TextView ssqBall = holder.lotteryBallTextView;

			if (ssqBall.getTag().equals("unselect")) {
				ssqBall.setTextColor(getSelectedBallTextColor());
				ssqBall.setBackground(getSelectedBallBgDrawable());
				ssqBall.setTag("selected");
				SsqLottery.this.getBettingList(styleId).add(Integer.parseInt((String) ssqBall.getText()));
			} else {
				ssqBall.setTextColor(getUnselectedBallTextColor());
				ssqBall.setBackground(getUnselectedBallBgDrawable());
				ssqBall.setTag("unselect");
				SsqLottery.this.getBettingList(styleId).remove((Object) Integer.parseInt((String) ssqBall.getText()));
			}
		}

		@Override
		public View getView() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
