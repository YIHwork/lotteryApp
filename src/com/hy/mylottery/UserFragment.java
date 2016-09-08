package com.hy.mylottery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hy.Utils.UserExpandableListView;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserFragment extends Fragment implements OnClickListener {

	String titleName = "";
	ImageView settingView;
	RelativeLayout userContentView;
	ImageView userPortraitView;
	TextView usernameView;
	TextView userStatusView;
	TextView userMsgView;
	View luckyMoneyView;
	View luckyCountView;
	View unluckyCountView;
	UserExpandableListView bettedListView;

	public UserFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserFragment(String titleName) {
		// TODO Auto-generated constructor stub
		this();
		this.titleName = titleName;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_tab_user, container, false);
		settingView = (ImageView) rootView.findViewById(R.id.user_setting);
		settingView.setOnClickListener(this);

		userContentView = (RelativeLayout) rootView.findViewById(R.id.user_content);
		userPortraitView = (ImageView) rootView.findViewById(R.id.user_portrait);
		usernameView = (TextView) rootView.findViewById(R.id.username);
		userStatusView = (TextView) rootView.findViewById(R.id.user_status);
		userMsgView = (TextView) rootView.findViewById(R.id.user_msg);
		userContentView.setOnClickListener(this);

		luckyMoneyView = rootView.findViewById(R.id.lucky_money);
		ImageView luckyMoneyIcon = (ImageView) luckyMoneyView.findViewById(R.id.init_icon);
		TextView luckyMoney = (TextView) luckyMoneyView.findViewById(R.id.statistical);
		TextView luckyMoneySummary = (TextView) luckyMoneyView.findViewById(R.id.summary);
		luckyMoneySummary.setText("中奖金额");
		luckyCountView = rootView.findViewById(R.id.lucky_count);
		ImageView luckyCountIcon = (ImageView) luckyCountView.findViewById(R.id.init_icon);
		TextView luckyCount = (TextView) luckyCountView.findViewById(R.id.statistical);
		TextView luckyCountSummary = (TextView) luckyCountView.findViewById(R.id.summary);
		luckyCountSummary.setText("中奖次数");
		unluckyCountView = rootView.findViewById(R.id.not_lucky_count);
		ImageView unluckyCountIcon = (ImageView) unluckyCountView.findViewById(R.id.init_icon);
		TextView unluckyCount = (TextView) unluckyCountView.findViewById(R.id.statistical);
		TextView unluckyCountSummary = (TextView) unluckyCountView.findViewById(R.id.summary);
		unluckyCountSummary.setText("未中次数");

		bettedListView = (UserExpandableListView) rootView.findViewById(R.id.betted_list);
		BettedExpandableListAdapter adapter = new BettedExpandableListAdapter();

		// for test data
		for (int i = 0; i < 10; i++) {
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("LotteryName", "双色球");
			dataMap.put("BettedDate", "2016-02-21 19:30");
			dataMap.put("Expect", "第2016099期");
			dataMap.put("Code", "01,03,19,24,32,33+01");
			dataMap.put("IsLucky", "未中奖");
			adapter.historyDatas.add(dataMap);
		}
		bettedListView.setAdapter(adapter);
		return rootView;
	}

	class BettedExpandableListAdapter extends BaseExpandableListAdapter {

		String[] groupNames = new String[] { "本次投注", "最近投注" };
		ArrayList<Map<String, String>> currentDatas = new ArrayList<Map<String, String>>();
		ArrayList<Map<String, String>> historyDatas = new ArrayList<Map<String, String>>();

		class ChildHolder {
			TextView lotteryName;
			TextView bettedDate;
			TextView expect;
			TextView code;
			TextView isLucky;
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return groupNames.length;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			int count = 0;
			switch (groupPosition) {
			case 0:
				count = currentDatas.size();
				break;

			case 1:
				count = historyDatas.size();
				break;
			default:
				break;
			}
			return count;
		}

		@Override
		public String getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return groupNames[groupPosition];
		}

		@Override
		public Map<String, String> getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			switch (groupPosition) {
			case 0:
				return currentDatas.get(childPosition);

			case 1:
				return historyDatas.get(childPosition);

			default:
				break;
			}
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.user_history_betting_directory, parent,
						false);
			}
			TextView name = (TextView) convertView.findViewById(R.id.name);
			TextView more = (TextView) convertView.findViewById(R.id.more);
			switch (groupPosition) {
			case 0:
				name.setText(getGroup(groupPosition));
				more.setVisibility(View.GONE);
				break;

			case 1:
				name.setText(getGroup(groupPosition));
				more.setVisibility(View.VISIBLE);
				more.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						System.out.println("========more========");
					}
				});
			default:
				break;
			}
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.user_history_betting_cell, parent,
						false);
				ChildHolder holder = new ChildHolder();
				holder.lotteryName = (TextView) convertView.findViewById(R.id.lottery_name);
				holder.bettedDate = (TextView) convertView.findViewById(R.id.betted_date);
				holder.expect = (TextView) convertView.findViewById(R.id.lottery_expect);
				holder.code = (TextView) convertView.findViewById(R.id.lottery_code);
				holder.isLucky = (TextView) convertView.findViewById(R.id.is_lucky);
				convertView.setTag(holder);
			}

			ChildHolder holder = (ChildHolder) convertView.getTag();
			holder.lotteryName.setText(getChild(groupPosition, childPosition).get("LotteryName"));
			holder.bettedDate.setText(getChild(groupPosition, childPosition).get("BettedDate"));
			holder.expect.setText(getChild(groupPosition, childPosition).get("Expect"));
			holder.code.setText(getChild(groupPosition, childPosition).get("Code"));
			switch (groupPosition) {
			case 0:
				holder.isLucky.setText("等待开奖");
				break;

			case 1:
				holder.isLucky.setText("未中奖");
				break;

			default:
				break;
			}

			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.user_setting:
			Intent intent = new Intent(getActivity(), SettingActivity.class);
			startActivity(intent);
			break;

		case R.id.user_content:
			Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
			startActivityForResult(loginIntent, 11);
			break;

		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// super.onActivityResult(requestCode, resultCode, data);
		System.out.println("this is user fragment");
	}

}
