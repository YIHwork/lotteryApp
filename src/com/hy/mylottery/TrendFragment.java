package com.hy.mylottery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hy.Utils.ResourceUtils;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrendFragment extends Fragment {

	String titleName = "";
	Map<Object, Object> lotteries;
	List<Map<String, Object>> listItems;

	public TrendFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TrendFragment(String titleName) {
		// TODO Auto-generated constructor stub
		this();
		this.titleName = titleName;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		lotteries = ResourceUtils.getAllLotteries();
		listItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < lotteries.size(); i++) {
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put("name", ((Object[]) lotteries.get(i + 1))[0]);
			listItem.put("desc", "Good luck");
			listItem.put("icon", ((Object[]) lotteries.get(i + 1))[1]);
			// listItem.put("display", android.R.drawable.ic_media_play);
			listItems.add(listItem);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.fragment_tab_trend, container, false);
		TextView titleNameText = (TextView) rootView.findViewById(R.id.trend_title_name);
		titleNameText.setText(titleName);
		ListView trendList = (ListView) rootView.findViewById(R.id.trend_list);
		SimpleAdapter trendListAdapter = new SimpleAdapter(getActivity(), listItems, R.layout.trend_cell,
				new String[] { "name", "desc", "icon" },
				new int[] { R.id.trend_cell_name, R.id.trend_cell_desc, R.id.trend_cell_icon });
		trendList.setAdapter(trendListAdapter);
		trendList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(), "ªπŒ¥ µœ÷", Toast.LENGTH_SHORT).show();
			}
		});
		return rootView;
	}

}
