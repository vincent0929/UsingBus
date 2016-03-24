package com.vincent.bus.stationSearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.offlinemap.City;
import com.vincent.bus.R;

public class AtyStationSearch extends Activity {
	
	protected static final String STATION_NAME = "stationName";
	protected static final String CITY_NAME = "cityName";
	
	private String cityName="沈阳";
	
	private EditText etStationName;
	private Button btnSearchStation;
	private TextView tvBtnSearchStationNearby;
	private TextView tvBtnShowCollectStations;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_station_search);
		
		initView();
	}
	
	private void initView() {
		etStationName=(EditText) findViewById(R.id.etStationName);
		btnSearchStation=(Button) findViewById(R.id.btnSearchStation);
		btnSearchStation.setOnClickListener(listener);
		tvBtnSearchStationNearby=(TextView) findViewById(R.id.tvBtnSearchStationNearby);
		tvBtnSearchStationNearby.setOnClickListener(listener);
		tvBtnShowCollectStations=(TextView) findViewById(R.id.tvBtnShowCollectStations);
		tvBtnShowCollectStations.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new View.OnClickListener() {
		
		Intent intent;
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnSearchStation:
				if (!etStationName.getText().toString().trim().isEmpty()) {
					intent = new Intent(AtyStationSearch.this, AtyStationSearchQuery.class);
					intent.putExtra(STATION_NAME, etStationName.getText().toString());
					intent.putExtra(CITY_NAME, cityName);
					startActivity(intent);
					overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				}else{
					Toast.makeText(getApplicationContext(), "站点名不能为空", 500).show();
				}
				break;
			case R.id.tvBtnSearchStationNearby:
				Intent intent=new Intent(AtyStationSearch.this,AtyShowStationNearby.class);
				intent.putExtra(CITY_NAME, cityName);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				break;
			case R.id.tvBtnShowCollectStations:
				break;
			default:
				break;
			}	
		}
	};
}
