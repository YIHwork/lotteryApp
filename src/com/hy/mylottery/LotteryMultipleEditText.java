package com.hy.mylottery;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class LotteryMultipleEditText extends EditText {

	Context context;

	public LotteryMultipleEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public LotteryMultipleEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	public LotteryMultipleEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	// ������뷨isActive����ôKeyEvent��Ϣ���ȱ��ɷ���IME����
	// �����������IME������Ϣ֮ǰ�ػ����Ϣ������Ҫ��дdispatchKeyEventPreIme��
	// ���紦��back��Ϣ��ͨ��dispatchKeyEventPreIme�Ϳ�����IME�ر�֮ǰ����KEYCODE_BACK
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			InputMethodManager imm = (InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm.isActive(this)) {
				boolean isHideSuccess = imm.hideSoftInputFromWindow(this.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
				this.clearFocus();
				if (isHideSuccess) {
					return true;
				}
			}
		}
		return super.dispatchKeyEventPreIme(event);
	}

}
