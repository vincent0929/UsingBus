package com.vincent.bus.stationSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.vincent.bus.R;
import com.vincent.bus.busLineSearch.AtyBusLineSearch;
import com.vincent.bus.busLineSearch.AtyBusLineSearchResult;
import com.vincent.bus.busLineSearch.AtySearchBusLineByBusStation;

public class AtyShowStationNearby extends Activity implements
	AMapLocationListener, OnPoiSearchListener ,OnBusStationSearchListener{
	
	protected static final String CITY_NAME = "cityName";
	
	private String cityName="";
	
	private Button btnAtyShowStationNearbyBackTo=null;
	private ListView lvNearbyStationList=null;
	private ProgressDialog progressDialog=null;

	private LocationManagerProxy locationManager=null;
	private LatLonPoint myLocationLatLonPoint=null;
	
	private PoiSearch.Query poiQuery=null;
	private PoiSearch.SearchBound poiSearchBound=null;
	private PoiSearch poiSearch=null;
	private PoiResult poiResult=null;
	private List<PoiItem> poiItems=null;
	
	private BusStationQuery busStationQuery=null;
	private BusStationSearch busStationSearch=null;
	private BusStationResult busStationResult=null;
	private String currentBusStationName="";
	
	private List<BusStationItem> stationsNearbyList=null;
	private StationNearbyListAdapter adapter=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_show_station_nearby);
		
		cityName=getIntent().getStringExtra(AtyBusLineSearch.CITY_NAME);
		
		btnAtyShowStationNearbyBackTo=
				(Button) findViewById(R.id.btnAtyShowStationNearbyBackTo);		
		btnAtyShowStationNearbyBackTo.setText("< 返回");
		btnAtyShowStationNearbyBackTo.setOnClickListener(listener);
		lvNearbyStationList=(ListView) findViewById(R.id.lvNearbyStationList);
		lvNearbyStationList.setOnItemClickListener(itemClickListener);
		
		locationManager=LocationManagerProxy.getInstance(this);
		locationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10, this);
		
		stationsNearbyList=new ArrayList<BusStationItem>();
		adapter=new StationNearbyListAdapter(this);
		lvNearbyStationList.setAdapter(adapter);
		
		progressDialog=ProgressDialog.show(this, null, "正在加载......");
		progressDialog.show();
		handler.sendEmptyMessageDelayed(0,10000);
	}
	
	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnAtyShowStationNearbyBackTo:
				finishAty();
				break;

			default:
				break;
			}
		}
	};
	
	private OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent=new Intent(AtyShowStationNearby.this,AtySearchBusLineByBusStation.class);
			intent.putExtra(CITY_NAME, cityName);
			intent.putExtra("busStationName", adapter.getItem(position).getBusStationName());
			intent.putExtra("busLineName", "< 返回");
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		}
	};
	
	public void finishAty(){
		if(progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
	}
	
	private void stopLocation() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
			locationManager.destory();
		}
		locationManager = null;
	}
	
	private void searchNearbyBusStations(){ 
		poiQuery=new PoiSearch.Query("", "公交车站", cityName);
		poiSearchBound=new PoiSearch.SearchBound(myLocationLatLonPoint,1000,true);
		poiSearch=new PoiSearch(this, poiQuery);
		poiSearch.setBound(poiSearchBound);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	private void searchBusStationByPoiItem(String name) {
		busStationQuery=new BusStationQuery(name, cityName);
		busStationSearch=new BusStationSearch(this, busStationQuery);
		busStationSearch.setOnBusStationSearchListener(this);
		busStationSearch.searchBusStationAsyn();
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onBusStationSearched(BusStationResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&
					result.getQuery().equals(busStationQuery)){
				System.out.println("++++++++++++++++++++++++");
				for (BusStationItem item : result.getBusStations()) {
					stationsNearbyList.add(item);
					adapter.notifyDataSetChanged();
				}
				System.out.println("------------------------");		
			}else{
				if(progressDialog.isShowing()){
					progressDialog.dismiss();
					stopLocation();
				}
			}
		}
	}
	
	
	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if(rCode==0){
			if (result != null && result.getQuery() != null
					&& result.getQuery().equals(poiQuery)) {
				poiItems = result.getPois();
				if (poiItems.size() > 0) {
					
					System.out.println("++++++++++++++++++");
					for (PoiItem poiItem : poiItems) {
						searchBusStationByPoiItem(poiItem.getTitle());
						System.out.println("poiItem:"+poiItem.getTitle());
					}
					System.out.println("-------------------");
				}else{
					if(progressDialog.isShowing()){
						progressDialog.dismiss();
						stopLocation();
					}
				}
			}
		}
	}
	
	
	@Override
	public void onLocationChanged(AMapLocation location) {
		if(location!=null){
			myLocationLatLonPoint=new LatLonPoint(location.getLatitude(), location.getLongitude());
			searchNearbyBusStations();
			locationManager.removeUpdates(this);
			stopLocation();
		}
	}
	
	private class StationNearbyListAdapter extends BaseAdapter{
		
		private LayoutInflater inflater=null;
		
		public StationNearbyListAdapter(Context context){
			inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return stationsNearbyList.size();
		}
		
		@Override
		public BusStationItem getItem(int position) {
			return stationsNearbyList.get(position);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout ll = null;
			if (convertView != null) {
				ll = (LinearLayout) convertView;
			} else {
				ll = (LinearLayout) inflater.inflate(
						R.layout.station_nearby_list_item, null);
			}
			TextView tvStationNearbyName = (TextView) ll.findViewById(R.id.tvStationNearbyName);
			TextView tvBusLinesPassStation = (TextView) ll.findViewById(R.id.tvBusLinesPassStation);
			String stationName = stationsNearbyList.get(position).getBusStationName();
			List<BusLineItem> busLinesPassStation=stationsNearbyList.get(position).getBusLineItems();
			StringBuffer buffer=new StringBuffer();
			for (int i=0;i<stationsNearbyList.size();i++) {
				String busLineName=busLinesPassStation.get(i).getBusLineName();
				buffer.append(busLineName.substring(0, busLineName.indexOf("(")));
				if(i!=stationsNearbyList.size()-1){
					buffer.append(",");
				}
			}
			tvBusLinesPassStation.setText(buffer.toString());
			
			return ll;
		}
	}

	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if(progressDialog.isShowing()==true){
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), "网络不畅或网络连接中断", 500).show();
					finishAty();
				}
				break;
			default:
				break;
			}
		};
	};
}
