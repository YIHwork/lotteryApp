package com.hy.mylottery.lotteryType;

import android.util.SparseArray;
import android.view.View;

public abstract class BaseLotteryType {
	public String ruleDescription;
	private SparseArray<LotteryAppearance> appearances;// 用于保存此彩票票种的所有样式

	public BaseLotteryType() {
		// TODO Auto-generated constructor stub
		appearances = new SparseArray<LotteryAppearance>();
	}

	protected LotteryAppearance getAppearanceStyle(int styleId) {
		return appearances.get(styleId);
	}

	protected void putAppearanceStyle(int styleId, LotteryAppearance appearance) {
		appearances.put(styleId, appearance);
	}

	public View getView(int styleId) {
		return getAppearanceStyle(styleId).getView();
	}

	public abstract void betting();

	public static abstract class LotteryAppearance {

		public abstract View getView();

		public LotteryAppearance() {
			// TODO Auto-generated constructor stub
		}
	}
}
