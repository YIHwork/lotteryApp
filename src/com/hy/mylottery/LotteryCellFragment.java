package com.hy.mylottery;

import java.util.Map;

import com.hy.Utils.ResourceUtils;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LotteryCellFragment extends Fragment {

	/* 购票大厅tab的显示fragment，即各个票种选号的入口 */
	String titleName = "";
	GridView lotteryGrid;
	int windowWidth;
	Map<Object, Object> lotteries;

	public LotteryCellFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LotteryCellFragment(String titleName) {
		// TODO Auto-generated constructor stub
		this();
		this.titleName = titleName;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		lotteries = ResourceUtils.getAllLotteries();
		/*
		 * 获取屏幕大小 方法一,此方式并不一定代表显示的实际原始大小（本机分辨率），设备通常有屏幕修饰（如状态栏）
		 */
		WindowManager wm = getActivity().getWindowManager();
		Point outSize = new Point();
		wm.getDefaultDisplay().getSize(outSize);
		windowWidth = outSize.x;
		/*
		 * 方法二 WindowManager wms =
		 * (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE)
		 * ; DisplayMetrics metrics = new DisplayMetrics();
		 * wms.getDefaultDisplay().getMetrics(metrics); windowWidth =
		 * metrics.widthPixels; windowHetght = metrics.heightPixels;
		 */
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_ticket_lobby, container, false);
		TextView titleText = (TextView) rootView.findViewById(R.id.hall_title_name);
		titleText.setText(titleName);

		BaseAdapter cellAdapter = new BaseAdapter() {

			class ConvertViewHolder {
				ImageView lotteryIcon;
				TextView lotteryName;
				TextView lotteryMsg;
			}

			int count = -1;

			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				count = lotteries.size();
				return count + (3 - count % 3);
			}

			@Override
			public Object[] getItem(int position) {
				// TODO Auto-generated method stub
				Object[] valuses = lotteries.values().toArray();
				return (Object[]) valuses[position];
				// return (Object[]) lotteries.get(position+1);
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.lottery_cell, parent, false);
					ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
					layoutParams.height = windowWidth / 3;
					convertView.setLayoutParams(layoutParams);

					ConvertViewHolder holder = new ConvertViewHolder();
					holder.lotteryIcon = (ImageView) convertView.findViewById(R.id.cell_icon);
					holder.lotteryName = (TextView) convertView.findViewById(R.id.cell_name);
					holder.lotteryMsg = (TextView) convertView.findViewById(R.id.cell_desc);
					convertView.setTag(holder);
				}
				ConvertViewHolder holder = (ConvertViewHolder) convertView.getTag();
				if (position > count - 1) {
					// holder.lotteryIcon.setImageResource(R.drawable.default_lottery_icon);
					holder.lotteryIcon.setImageDrawable(new ColorDrawable(Color.alpha(0)));
					holder.lotteryName.setText("");
					holder.lotteryMsg.setText("");
				} else {
					holder.lotteryIcon.setImageResource((Integer) getItem(position)[1]);
					holder.lotteryName.setText((String) getItem(position)[0]);
					String msg;
					if (((String) getItem(position)[0]).equals("双色球")) {
						msg = "Good luck!";
					} else {
						msg = "暂停销售";
					}
					holder.lotteryMsg.setText(msg);
				}
				// convertView.setOnClickListener(new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// // TODO Auto-generated method stub
				// if
				// (((ConvertViewHolder)v.getTag()).lotteryName.getText().equals("双色球"))
				// {
				// Intent intent = new Intent(getActivity(),
				// ChooseSsqActivity.class);
				// startActivity(intent);
				// }else {
				// Toast.makeText(getActivity(), "未实现，请点击“双色球”体验",
				// Toast.LENGTH_SHORT).show();
				// }
				// }
				// });

				return convertView;
			}
		};

		lotteryGrid = (GridView) rootView.findViewById(R.id.lottery_grid);
		lotteryGrid.setAdapter(cellAdapter);
		lotteryGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (position <= lotteries.size() - 1) {
					View lotteryNameView = ((LinearLayout) view).getChildAt(1);
					if (lotteryNameView instanceof TextView) {
						if (((TextView) lotteryNameView).getText().equals("双色球")) {
							Intent intent = new Intent(getActivity(), ChooseSsqActivity.class);
							startActivity(intent);
						} else {
							Toast.makeText(getActivity(), "未实现，请点击“双色球”体验", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});
		return rootView;
	}
}
