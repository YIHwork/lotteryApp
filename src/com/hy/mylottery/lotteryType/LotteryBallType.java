package com.hy.mylottery.lotteryType;

import java.util.ArrayList;

import com.hy.mylottery.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public abstract class LotteryBallType extends BaseLotteryType {

	public static int[] randomBetting() {
		return null;
	}

	public PopupWindow getPopupWindow() {
		return LotteryBallAppearance.popupWindow;
	}

	@Override
	public LotteryBallAppearance getAppearanceStyle(int styleId) {
		// TODO Auto-generated method stub
		return (LotteryBallAppearance) super.getAppearanceStyle(styleId);
	}

	public abstract ArrayList<Integer> getBettingList(int styleId);

	public static abstract class LotteryBallAppearance extends LotteryAppearance {

		protected LayoutInflater inflater;
		protected Context context;
		public int styleId;
		private int[] ballNums;
		private Drawable popupBgDrawable, unselectedBallBgDrawable, selectedBallBgDrawable;
		private int unselectedBallTextColor, selectedBallTextColor;
		public static PopupWindow popupWindow;

		public LotteryBallAppearance(Context context, int styleId) {
			// TODO Auto-generated constructor stub
			this.context = context;
			this.styleId = styleId;
			loaddingStyle(styleId);
			// inflater = LayoutInflater.from(context);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int[] getBallNums() {
			return ballNums;
		}

		public void setBallNums(int[] ballNums) {
			this.ballNums = ballNums;
		}

		public int getBallNum(int position) {
			return ballNums[position];
		}

		public int getBallCount() {
			return ballNums.length;
		}

		public Drawable getPopupBgDrawable() {
			return popupBgDrawable;
		}

		public void setPopupBgDrawable(Drawable popupBgDrawable) {
			this.popupBgDrawable = popupBgDrawable;
		}

		public Drawable getUnselectedBallBgDrawable() {
			return unselectedBallBgDrawable;
		}

		public void setUnselectedBallBgDrawable(Drawable unselectedBallBgDrawable) {
			this.unselectedBallBgDrawable = unselectedBallBgDrawable;
		}

		public Drawable getSelectedBallBgDrawable() {
			return selectedBallBgDrawable;
		}

		public void setSelectedBallBgDrawable(Drawable selectedBallBgDrawable) {
			this.selectedBallBgDrawable = selectedBallBgDrawable;
		}

		public int getUnselectedBallTextColor() {
			return unselectedBallTextColor;
		}

		public void setUnselectedBallTextColor(int unselectedBallTextColor) {
			this.unselectedBallTextColor = unselectedBallTextColor;
		}

		public int getSelectedBallTextColor() {
			return selectedBallTextColor;
		}

		public void setSelectedBallTextColor(int selectedBallTextColor) {
			this.selectedBallTextColor = selectedBallTextColor;
		}

		/**
		 * 获取外观对应的默认视图，R.layout.selector_ssq_cell为默认视图的布局文件，不同的票种可以在此基础上扩展
		 */
		public View getView(String ballNum) {
			// TODO Auto-generated method stub
			View appearanceView = inflater.inflate(R.layout.selector_ssq_cell, null);
			TextView ssqBall = (TextView) appearanceView.findViewById(R.id.ssq_cell);
			ssqBall.setText(ballNum);
			ssqBall.setTextColor(getUnselectedBallTextColor());
			PopupWindow ballPopup = new PopupWindow(getPopupView(ballNum));
			// ballPopup.setAnimationStyle(0);

			LotteryBallAppearanceHolder holder = new LotteryBallAppearanceHolder();
			holder.lotteryBallTextView = ssqBall;
			holder.ballPopup = ballPopup;
			appearanceView.setTag(holder);

			return appearanceView;
		}

		/**
		 * 获取默认popup view
		 */
		private View getPopupView(String ballNum) {
			// TODO Auto-generated method stub
			View popupView = inflater.inflate(R.layout.selector_ssq_cell_popup, null);
			TextView ssqPopupTxt = (TextView) popupView.findViewById(R.id.ssq_cell_popup);
			ssqPopupTxt.setText(ballNum);
			ssqPopupTxt.setTextColor(getSelectedBallTextColor());
			ssqPopupTxt.setBackground(getPopupBgDrawable());
			popupView.setTag(ssqPopupTxt);
			return popupView;
		}

		/**
		 * 获取彩球view的holder，
		 * 
		 * @param ballView
		 *            is the return value of this.getView(), the view must
		 *            previously execute setTag(holder)
		 */
		public static LotteryBallAppearanceHolder getHolderOfBallView(View ballView) {
			return (LotteryBallAppearanceHolder) ballView.getTag();
		}

		public abstract void loaddingStyle(int styleId);

		public abstract void showPopupWindow(View pressedView);

		public abstract void updateSelectedBallState(View pressedView);

		/**
		 * 此类为LotteryBallAppearance的holder类，主要为每个 lottery ball view辅助持有一些field
		 */
		public class LotteryBallAppearanceHolder {
			public TextView lotteryBallTextView;
			public PopupWindow ballPopup;
			public View popupView;
			public int[] popupLocation = new int[2];

			public LotteryBallAppearanceHolder() {
				// TODO Auto-generated constructor stub
			}
		}
	}
}
