package com.vincent.bus;

import android.app.TabActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.amap.api.services.busline.BusStationItem;
import com.vincent.bus.busLineSearch.AtyBusLineSearch;
import com.vincent.bus.routeSearch.AtyRouteSearch;
import com.vincent.bus.stationSearch.AtyStationSearch;
import com.vincent.bus.stationSearch.AtyStationSearchQuery;

public class MainActivity extends TabActivity {

	private static final int REQUST_CODE_SELECT_CITY = 0;
	private static final String STATION = "station";
	private static final String BUS_STATION_ITEM = "busStationItem";
	private static TabHost tabHost = null;
	private LinearLayout llBtnSelectCity = null;
	private static String cityName = "沈阳";

	private Intent tabIntent1, tabIntent2, tabIntent3;

	private String changeFlag = "";
	private String stationType = "";
	private BusStationItem busStationItem;

	private MyReceiver receiver;

	public static String getCityName() {
		return cityName;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		llBtnSelectCity = (LinearLayout) findViewById(R.id.llBtnSelectCity);
		llBtnSelectCity.setOnClickListener(listener);
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		tabHost.setCurrentTabByTag("huancheng");

		tabIntent1 = new Intent(this, AtyRouteSearch.class);
		tabIntent2 = new Intent(this, AtyBusLineSearch.class);
		tabIntent3 = new Intent(this, AtyStationSearch.class);

		tabHost.addTab(tabHost.newTabSpec("huancheng").setIndicator(addTabView("换乘", R.drawable.huancheng_icon)).setContent(tabIntent1));
		tabHost.addTab(tabHost.newTabSpec("xianlu").setIndicator(addTabView("线路", R.drawable.xianlu_icon)).setContent(tabIntent2));
		tabHost.addTab(tabHost.newTabSpec("zhandain").setIndicator(addTabView("站点", R.drawable.zhandian_icon)).setContent(tabIntent3));

		receiver = new MyReceiver();
	}

	@Override
	protected void onResume() {
		registerReceiver(receiver, new IntentFilter(MyReceiver.ACTION));
		changeFlag = getIntent().getStringExtra("switchActivity");
		stationType = getIntent().getStringExtra("station");
		busStationItem = getIntent().getParcelableExtra("busStationItem");
		if (changeFlag != null) {
			switchAcitivity(changeFlag);
		}
		if (stationType != null) {
			sendMessageToAtyRouteSearch();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(receiver);
		super.onPause();
	}

	public View addTabView(String text, int i) {
		View view = getLayoutInflater().inflate(R.layout.tab_indicator, null);
		ImageView iv = (ImageView) view.findViewById(R.id.tabIcon);
		iv.setImageResource(i);
		TextView tv = (TextView) view.findViewById(R.id.tabTitle);
		tv.setText(text);
		return view;
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, AtySelectCityName.class);
			startActivityForResult(intent, REQUST_CODE_SELECT_CITY);
			overridePendingTransition(R.anim.push_bottom_in, R.anim.push_center);
		}
	};

	private void switchAcitivity(String changeFlag) {
		if (changeFlag != null) {
			System.out.println("changeFlag=" + changeFlag);
			System.out.println("AtyBusLineSearch:" + AtyBusLineSearch.class.getName());

			if (changeFlag.equals(AtyRouteSearch.class.getName())) {
				tabHost.setCurrentTab(0);
			} else if (changeFlag.equals(AtyBusLineSearch.class.getName())) {
				tabHost.setCurrentTab(1);
			} else if (changeFlag.equals(AtyStationSearchQuery.class.getName())) {
				tabHost.setCurrentTab(2);
			}
		}
	}

	public void sendMessageToAtyRouteSearch() {
		Intent intent = new Intent(MyReceiver.ACTION);
		intent.putExtra(STATION, stationType);
		Bundle bundle = new Bundle();
		bundle.putParcelable(BUS_STATION_ITEM, busStationItem);
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}
}
