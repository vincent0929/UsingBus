package com.vincent.bus.busLineSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
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
import com.vincent.bus.MainActivity;
import com.vincent.bus.R;

public class AtyShowBusLineNearby extends Activity implements
		AMapLocationListener, OnPoiSearchListener ,OnBusStationSearchListener,OnBusLineSearchListener{
	
	protected static final String CITY_NAME = "cityName";
	protected static final String BUSLINE_ITEM = "busLineItem";

	private String cityName="";
	
	private Button btnAtyShowBusLineNearbyBackToAtyBusLineSearch=null;
	private ListView lvNearbyBusLineList=null;
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
	
	private BusLineQuery busLineQuery=null;
	private BusLineSearch busLineSearch=null;
	private BusLineResult busLineResult=null;
	
	private List<BusLineNearbyListItem> busLinesNearbyList=null;
	private BusLineNearbyListAdapter adapter=null;
	
	private List<BusStationItem> busStationItems=null;
	private List<BusLineItem> busLineItems=null;
	private int poiNum=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_show_busline_nearby);
		
		cityName=getIntent().getStringExtra(AtyBusLineSearch.CITY_NAME);

		btnAtyShowBusLineNearbyBackToAtyBusLineSearch=
				(Button) findViewById(R.id.btnAtyShowBusLineNearbyBackToAtyBusLineSearch);		
		btnAtyShowBusLineNearbyBackToAtyBusLineSearch.setText("< 返回");
		btnAtyShowBusLineNearbyBackToAtyBusLineSearch.setOnClickListener(listener);
		lvNearbyBusLineList=(ListView) findViewById(R.id.lvNearbyBusLineList);
		lvNearbyBusLineList.setOnItemClickListener(itemClickListener);
		
		locationManager=LocationManagerProxy.getInstance(this);
		locationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10, this);
		
		busLinesNearbyList=new ArrayList<BusLineNearbyListItem>();
		adapter=new BusLineNearbyListAdapter(this);
		lvNearbyBusLineList.setAdapter(adapter);
		
		progressDialog=ProgressDialog.show(this, null, "正在加载......");
		progressDialog.show();
		handler.sendEmptyMessageDelayed(0,10000);
		
		busStationItems=new ArrayList<BusStationItem>();
		busLineItems=new ArrayList<BusLineItem>();
	}
	
	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnAtyShowBusLineNearbyBackToAtyBusLineSearch:
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
			Intent intent=new Intent(AtyShowBusLineNearby.this,AtyBusLineSearchResult.class);
			intent.putExtra(CITY_NAME, cityName);
			Bundle bundle=new Bundle();
			bundle.putParcelable(BUSLINE_ITEM, adapter.getItem(position));
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		}
	};
	
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
	
	private void searchBusLineByBusStation(String BusLineId){
		busLineQuery=new BusLineQuery(BusLineId, SearchType.BY_LINE_ID, cityName);
		busLineSearch=new BusLineSearch(this, busLineQuery);
		busLineSearch.setOnBusLineSearchListener(this);
		busLineSearch.searchBusLineAsyn();
	}

	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
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

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if(rCode==0){
			if (result != null && result.getQuery() != null
					&& result.getQuery().equals(poiQuery)) {
				poiItems = result.getPois();
				poiNum=poiItems.size();
				
				if (poiItems.size() > 0) {
					
					System.out.println("++++++++++++++++++");
					for (final PoiItem poiItem : poiItems) {
						searchBusStationByPoiItem(poiItem.getTitle());
//						new Thread(new Runnable() {
//
//							@Override
//							public void run() {
//								searchBusStationByPoiItem(poiItem.getTitle());					
//							}
//						}).start();
						System.out.println("poiItem:" + poiItem.getTitle());
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
	public void onBusStationSearched(final BusStationResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&
					result.getQuery().equals(busStationQuery)){
				
//				busStationItems.add(result.getBusStations().get(0));
				
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						System.out.println("++++++++++++++++++++++++");
//						for (BusStationItem item : result.getBusStations()) {
//							System.out.println(item.getBusStationName());
//						} 
//						System.out.println("------------------------");
//						for(BusLineItem  busLineItem:result.getBusStations().get(0).getBusLineItems()){
//							currentBusStationName=result.getQuery().getQueryString();
////							searchBusLineByBusStation(busLineItem.getBusLineId());
//							Message msg=new Message();
//							msg.what=1000;
//							msg.obj=busLineItem.getBusLineId();
//							handler.sendMessage(msg);
//						}		
//					}
//				}).start();
				
				System.out.println("++++++++++++++++++++++++");
				for (BusStationItem item : result.getBusStations()) {
					System.out.println(item.getBusStationName());
				} 
				System.out.println("------------------------");
				for(BusLineItem  busLineItem:result.getBusStations().get(0).getBusLineItems()){
					currentBusStationName=result.getQuery().getQueryString();
					searchBusLineByBusStation(busLineItem.getBusLineId());
				}		

			}else{
				if(progressDialog.isShowing()){
					progressDialog.dismiss();
					stopLocation();
				}
			}
		}
	}

	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&result.getQuery().equals(busLineQuery)){
				if(result.getPageCount()>0&&result.getBusLines().size()>0){	
					
					System.out.println("++++++++++++++++++++++++");
					for (BusLineItem item : result.getBusLines()) {
						System.out.println(item.getBusLineName());
					}
					System.out.println("------------------------");
					
					BusLineItem item=result.getBusLines().get(0);
					BusLineNearbyListItem busLineNearbyListItem=
							new BusLineNearbyListItem(item, currentBusStationName);
					busLinesNearbyList.add(busLineNearbyListItem);
					adapter.notifyDataSetInvalidated();
				}
				if(progressDialog.isShowing()){
					progressDialog.dismiss();
					stopLocation();
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finishAty();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	public void finishAty(){
		if(progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
	}
	
	private class BusLineNearbyListItem{
		private BusLineItem busLineItem=null;
		private String busStationNearbyName="";
		public BusLineItem getBusLineItem() {
			return busLineItem;
		}
		public void setBusLineItem(BusLineItem busLineItem) {
			this.busLineItem = busLineItem;
		}
		public String getBusStationNearbyName() {
			return busStationNearbyName;
		}
		public void setBusStationNearbyName(String busStationNearbyName) {
			this.busStationNearbyName = busStationNearbyName;
		}
		public BusLineNearbyListItem(BusLineItem item,String name){
			this.busLineItem=item;
			this.busStationNearbyName=name;
		}
	}
	
	private class BusLineNearbyListAdapter extends BaseAdapter{
		
		private LayoutInflater inflater=null;
		
		public BusLineNearbyListAdapter(Context context){
			inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			return busLinesNearbyList.size();
		}
		
		@Override
		public BusLineItem getItem(int position) {
			return busLinesNearbyList.get(position).getBusLineItem();
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
						R.layout.busline_nearby_list_item, null);
			}
			TextView tvBusLineNearbyName = (TextView) ll.findViewById(R.id.tvBuslineNearbyName);
			TextView tvBusStationNearbyName = (TextView) ll.findViewById(R.id.tvBusStationNearbyName);
			TextView tvBusLineNearbyTerminalStation = 
					(TextView) ll.findViewById(R.id.tvBusLineNearbyTerminalStationName);
			String busLineName = busLinesNearbyList.get(position).getBusLineItem().getBusLineName();
			tvBusLineNearbyName.setText(busLineName.substring(0,busLineName.indexOf("(")));
			tvBusStationNearbyName.setText(busLinesNearbyList.get(position).getBusStationNearbyName());
			tvBusLineNearbyTerminalStation.setText(busLinesNearbyList.
					get(position).getBusLineItem().getTerminalStation());
			
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
			case 1000:
//				searchBusStationByPoiItem((String)msg.obj);
				searchBusLineByBusStation((String)msg.obj);
				break;
			default:
				break;
			}
		};
	};
} 
