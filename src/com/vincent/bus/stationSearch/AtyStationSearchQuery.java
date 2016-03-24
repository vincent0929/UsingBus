package com.vincent.bus.stationSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.vincent.bus.R;
import com.vincent.bus.StationListAdapter;
import com.vincent.bus.busLineSearch.AtySearchBusLineByBusStation;

public class AtyStationSearchQuery extends Activity implements OnBusStationSearchListener{
	
	private String cityName;
	private String stationName;
	
	private BusStationQuery busStationQuery=null;
	private BusStationSearch busStationSearch=null;
	private BusStationResult busStationResult=null;
	private String currentBusStationName="";
	private List<BusStationItem> busStationItems;
	
	private ProgressDialog progressDialog=null;
	
	private Button btnAtyStationSearchQueryBackToAtyStationSearch;
	private ListView lvStation;
	private StationListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_station_search_query);
		
		cityName=getIntent().getStringExtra(AtyStationSearch.CITY_NAME);
		stationName=getIntent().getStringExtra(AtyStationSearch.STATION_NAME);
		
		handler.sendEmptyMessageDelayed(0, 10000);
		
		initView();
		searchBusStation(stationName);
		progressDialog=ProgressDialog.show(this, null, "正在加载......");
	}
	
	void initView(){
		progressDialog=new ProgressDialog(AtyStationSearchQuery.this);
		progressDialog.setMessage("正在加载......");
		
		btnAtyStationSearchQueryBackToAtyStationSearch=
				(Button) findViewById(R.id.btnAtyStationSearchQueryBackToAtyStationSearch);
		btnAtyStationSearchQueryBackToAtyStationSearch.setOnClickListener(listener);
		lvStation=(ListView) findViewById(R.id.lvStation);
		lvStation=(ListView) findViewById(R.id.lvStation);
		lvStation.setOnItemClickListener(itemListener);
		busStationItems=new ArrayList<BusStationItem>();
		adapter=new StationListAdapter(AtyStationSearchQuery.this, busStationItems);
		lvStation.setAdapter(adapter);
	}

	private void searchBusStation(String name) {
		busStationQuery=new BusStationQuery(name, cityName);
		busStationSearch=new BusStationSearch(this, busStationQuery);
		busStationSearch.setOnBusStationSearchListener(this);
		busStationSearch.searchBusStationAsyn();
	}
	
	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			finish();
			overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
		}
	};
	
	private OnItemClickListener itemListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent intent=new Intent(AtyStationSearchQuery.this,AtySearchBusLineByBusStation.class);
			intent.putExtra("cityName", cityName);
			intent.putExtra("busStationName", busStationItems.get(arg2).getBusStationName());
			intent.putExtra("busLineName", "返回");
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		}
	};

	@Override
	public void onBusStationSearched(BusStationResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getQuery() != null && result.getQuery().equals(busStationQuery)) {
				 progressDialog.dismiss();
				busStationResult=result;
				busStationItems.addAll(busStationResult.getBusStations());
				adapter.notifyDataSetChanged();
				
			} else {
				progressDialog.dismiss();
			}
		}
	}
	
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if(progressDialog.isShowing()){
					progressDialog.cancel();
					Toast.makeText(getApplicationContext(), "网络不畅或网络连接中断", 500).show();
					finish();
					overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
				}
				break;
			default:
				break;
			}
		};
	};

}
