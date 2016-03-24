package com.vincent.bus.busLineSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.busline.BusStationQuery;
import com.amap.api.services.busline.BusStationResult;
import com.amap.api.services.busline.BusStationSearch;
import com.amap.api.services.busline.BusStationSearch.OnBusStationSearchListener;
import com.vincent.bus.MainActivity;
import com.vincent.bus.R;
import com.vincent.bus.stationSearch.AtyShowStationInMapView;

public class AtySearchBusLineByBusStation extends Activity implements 
	OnBusStationSearchListener,OnBusLineSearchListener{
	
	protected static final String BUSLINE_ITEM = "busLineItem";
	protected static final String CITY_NAME = "cityName";
	public static final String BUS_STATION_NAME = "busStationName";
	protected static final String SWITCH_ACTIVITY = "switchActivity";
	protected static final String STATION = "station";
	protected static final String BUS_STATION_ITEM = "busStationItem";
	
	private ProgressDialog progressDialog=null;
	private Button btnBackToAtyBusLineSearchResult=null;
	private TextView tvBusStationName=null;
	private Button btnShowBusStationInMap=null;
	private TextView tvSetBusStationToOrigin=null;
	private TextView tvSetBusStationToDestination=null;
	private ListView lvBusLinesContainBusStation=null;
	private MyAdapter adapter=null;

	private BusStationQuery busStationQuery=null;
	private BusStationSearch busStationSearch=null;
	private BusStationResult busStationResult=null;
	private BusStationItem busStationItem=null;
	
	private BusLineQuery busLineQuery=null;
	private BusLineSearch busLineSearch=null;
	private BusLineResult busLineResult=null;
	private BusLineItem busLineItem=null;
	
	private String cityName="";
	private String busStationName="";
	private String busLineName="";
	
	private List<BusLineItem> busLineContainBusStationlist=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_show_busline_contain_busstation);
		
		cityName=getIntent().getStringExtra(AtyBusLineSearchResult.CITY_NAME);
		busStationName=getIntent().getStringExtra(AtyBusLineSearchResult.BUS_STATION_NAME);
		busLineName=getIntent().getStringExtra(AtyBusLineSearchResult.BUSLINE_NAME);
		
		init();
		
		searchBusLinesByBusStation();
		
		handler.sendEmptyMessageDelayed(0, 5000);
	}
	
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(progressDialog.isShowing()==true){
				Toast.makeText(getApplicationContext(), "网络不畅或网络连接中断", 500).show();
				finishAty();
			}
		};
	};

	private void init() {
		progressDialog=ProgressDialog.show(this, null, "正在加载......");
		btnBackToAtyBusLineSearchResult=(Button) 
				findViewById(R.id.btnAtySearchBusLineByBusStationBackToAtyBusLineSearchResult);
		btnBackToAtyBusLineSearchResult.setOnClickListener(listener);
		tvBusStationName=(TextView) findViewById(R.id.tvBusStationName);
		btnShowBusStationInMap=(Button) findViewById(R.id.btnShowBusStationInMap);
		btnShowBusStationInMap.setOnClickListener(listener);
		tvSetBusStationToOrigin=(TextView) findViewById(R.id.tvSetBusStationToOrigin);
		tvSetBusStationToOrigin.setOnClickListener(listener);
		tvSetBusStationToDestination=(TextView) findViewById(R.id.tvSetBusStationToDestination);
		tvSetBusStationToDestination.setOnClickListener(listener);
		lvBusLinesContainBusStation=(ListView) findViewById(R.id.lvBusLinesContainBusStation);
		lvBusLinesContainBusStation.setOnItemClickListener(itemClickListener);
		adapter=new MyAdapter(this);
		busLineContainBusStationlist=new ArrayList<BusLineItem>();
		lvBusLinesContainBusStation.setAdapter(adapter);
		
		String str="< ";
		str+=busLineName;
		btnBackToAtyBusLineSearchResult.setText(str);
		tvBusStationName.setText(busStationName);
	}
	
	public OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent;
			Bundle bundle;
			switch (v.getId()) {
			case R.id.btnAtySearchBusLineByBusStationBackToAtyBusLineSearchResult:
				finishAty();
				break;
			case R.id.tvSetBusStationToOrigin:
				intent=new Intent(AtySearchBusLineByBusStation.this,MainActivity.class);
				intent.putExtra(SWITCH_ACTIVITY, "AtyRouteSearch");
				intent.putExtra(STATION, "OriginStation");
				bundle=new Bundle();
				bundle.putParcelable(BUS_STATION_ITEM, busStationItem);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
				break;
			case R.id.tvSetBusStationToDestination:
				intent=new Intent(AtySearchBusLineByBusStation.this,MainActivity.class);
				intent.putExtra(SWITCH_ACTIVITY, "AtyRouteSearch");
				intent.putExtra(STATION, "TerminalStation");
				bundle=new Bundle();
				bundle.putParcelable(BUS_STATION_ITEM, busStationItem);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
				break;
			case R.id.btnShowBusStationInMap:
				intent=new Intent(AtySearchBusLineByBusStation.this,AtyShowStationInMapView.class);
				bundle=new Bundle();
				bundle.putParcelable("stationItem", busStationItem);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				break;
			default:
				break;
			}
		}
	};
	
	private OnItemClickListener itemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			searchBusLine(busLineContainBusStationlist.get(position).getBusLineId());
		}
	};
	
	private void searchBusLinesByBusStation(){
		busStationQuery=new BusStationQuery(busStationName, cityName);
		busStationSearch=new BusStationSearch(this, busStationQuery);
		busStationSearch.setOnBusStationSearchListener(this);
		busStationSearch.searchBusStationAsyn();
	}
	
	private void searchBusLine(String busLineId){
		busLineQuery=new BusLineQuery(busLineId, SearchType.BY_LINE_ID, cityName);
		busLineSearch=new BusLineSearch(this, busLineQuery);
		busLineSearch.setOnBusLineSearchListener(this);
		busLineSearch.searchBusLineAsyn();
	}

	@Override
	public void onBusStationSearched(BusStationResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getPageCount()>0&&
					result.getBusStations()!=null&&result.getBusStations().size()>0){
				busStationResult=result;
				busStationItem=busStationResult.getBusStations().get(0);
				busLineContainBusStationlist.clear();
				busLineContainBusStationlist.addAll(busStationItem.getBusLineItems());
				
				for (BusLineItem item : busStationItem.getBusLineItems()) {
					System.out.println(item.getOriginatingStation()+"+"+item.getTerminalStation());
				}
				
				progressDialog.dismiss();
				adapter.notifyDataSetInvalidated();
			}
		}else{
			Toast.makeText(getApplicationContext(), "查询出错", 500).show();
			finishAty();
		}
	}
	

	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&result.getQuery().equals(busLineQuery)){
				if(result.getPageCount()>0&&result.getBusLines()!=null&&result.getBusLines().size()>0){
					busLineResult=result;
					busLineItem=busLineResult.getBusLines().get(0);
					showBusLine();
				}
			}else{
				Toast.makeText(getApplicationContext(), "查询出错", 500).show();
				finishAty();
			}
		}
	}
	
	private void showBusLine() {
		Intent intent=new Intent(AtySearchBusLineByBusStation.this, AtyBusLineSearchResult.class);
		Bundle bundle=new Bundle();
		bundle.putParcelable(BUSLINE_ITEM, busLineItem);
		intent.putExtras(bundle);
		intent.putExtra(CITY_NAME, cityName);
		startActivity(intent);
		overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
	}

	public void finishAty(){
		if(progressDialog.isShowing()==true){
			progressDialog.dismiss();
		}
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
	}
	
	private class MyAdapter extends BaseAdapter{
		
		private LayoutInflater inflater=null; 
		
		public MyAdapter(Context context){
			inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return busLineContainBusStationlist.size()>0?busLineContainBusStationlist.size():0;
		}

		@Override
		public Object getItem(int position) {
			return busLineContainBusStationlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout ll=null;
			if(convertView!=null){
				ll=(LinearLayout) convertView; 
			}else{
				ll=(LinearLayout) inflater.inflate(R.layout.busline_contain_busstation_list_item, null);
			}
			
			String busLineName=busLineContainBusStationlist.get(position).getBusLineName().substring(0, busLineContainBusStationlist.get(position).getBusLineName().indexOf("("));
			String busLineOriginStationName=busLineContainBusStationlist.get(position).getOriginatingStation();
			String busLineTerminalStationName = busLineContainBusStationlist.get(position).getTerminalStation();
			
			TextView tvBusLinesContainBusStationItemName = (TextView) ll.findViewById(R.id.tvBusLinesContainBusStationItemName);
			TextView tvBusLinesContainBusStationItemFirstStationName = (TextView) ll.findViewById(R.id.tvBusLinesContainBusStationItemFirstStaionName);
			TextView tvBusLinesContainBusStationItemLastStationName = (TextView) ll.findViewById(R.id.tvBusLinesContainBusStationItemLastStationName);
			tvBusLinesContainBusStationItemName.setText(busLineName);
			tvBusLinesContainBusStationItemFirstStationName.setText(busLineOriginStationName);
			tvBusLinesContainBusStationItemLastStationName.setText(busLineTerminalStationName);

			return ll;
		}	
	}
}
