package com.vincent.bus.busLineSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.vincent.bus.R;
import com.vincent.bus.database.MyDataBase;

public class AtyBusLineSearchResult extends Activity implements
		OnBusLineSearchListener {

	private Button btnBackToAtyBusLineSearchQuery = null;
	private TextView tvBusLineName = null;
	private Button btnShowBusLineInMap = null;
	private ImageButton btnExchangeFirstAndLastStation = null;
	private TextView tvBusLineFirstStationName = null;
	private TextView tvBusLineLastStationName = null;
	private TextView tvFirstBusTime = null;
	private TextView tvLastBusTime = null;
	private TextView tvBusTicketPrice = null;
	private TextView tvBtnCollectBusLine=null;
	private ListView lvResult = null;

	private ArrayList<String> busStationsNameList = null;
	private MyAdapter adapter = null;

	private BusLineQuery busLineQuery = null;
	private BusStationQuery busStationQuery = null;
	private BusLineSearch busLineSearch = null;
	private BusStationSearch busStationSearch = null;
	private BusLineResult busLineResult = null;
	private BusStationResult busStationResult = null;

	private String busLineName = "";
	private String cityName="";
	private String busStationName="";
	
	public static final String BUSLINE_ID = "busLineID";	
	protected static final String CITY_NAME = "cityName";
	protected static final String BUS_STATION_NAME = "busStationName";
	protected static final String BUSLINE_NAME = "busLineName";
	protected static final String BUSLINE_ITEM = "busLineItem";

	private ProgressDialog progressDialog = null;
	
	private BusLineItem busLineItem=null;
	private BusLineItem oppositeBusLineItem=null;
	private BusLineItem currentBusLineItem=null;
	
	private MyDataBase db=null;
	private SQLiteDatabase dbWrite=null,dbRead=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_busline_search_result);
		
		init();
		
		cityName=getIntent().getStringExtra("cityName");
		busLineItem=getIntent().getParcelableExtra("busLineItem");
		System.out.println(busLineItem.getBusLineName());
		currentBusLineItem=busLineItem;
		showBusLineInfo(currentBusLineItem);
		
		searchBusLine(busLineItem.getBusLineName().substring(0, busLineItem.getBusLineName().indexOf("(")));
		
		progressDialog = ProgressDialog.show(this, null, "正在加载......");
		
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
		btnBackToAtyBusLineSearchQuery = (Button) findViewById(R.id.btnBackToAtyBusLineSearchQuery);
		String str="< 返回";
		btnBackToAtyBusLineSearchQuery.setText(str);
		btnBackToAtyBusLineSearchQuery.setOnClickListener(listener);
		tvBusLineName = (TextView) findViewById(R.id.tvBusLineName);
		btnShowBusLineInMap = (Button) findViewById(R.id.btnShowBusLineInMap);
		btnShowBusLineInMap.setOnClickListener(listener);
		btnExchangeFirstAndLastStation = (ImageButton) findViewById(R.id.btnExchangeFirstAndLastStation);
		btnExchangeFirstAndLastStation.setOnClickListener(listener);
		tvBusLineFirstStationName = (TextView) findViewById(R.id.tvBusLineFirstStationName);
		tvBusLineLastStationName = (TextView) findViewById(R.id.tvBusLineLastStationName);
		tvFirstBusTime = (TextView) findViewById(R.id.tvFirstBusTime);
		tvLastBusTime = (TextView) findViewById(R.id.tvLastBusTime);
		tvBusTicketPrice = (TextView) findViewById(R.id.tvBusTicketPrice);
		tvBtnCollectBusLine=(TextView) findViewById(R.id.tvBtnCollectBusLine);
		tvBtnCollectBusLine.setOnClickListener(listener);

		lvResult = (ListView) findViewById(R.id.lvBusStationsList);
		lvResult.setOnItemClickListener(itemClickListener);
		busStationsNameList = new ArrayList<String>();
		adapter = new MyAdapter(this);
		lvResult.setAdapter(adapter);
		
		db=new MyDataBase(this);
		dbWrite=db.getWritableDatabase();
		dbRead=db.getReadableDatabase();
	}

	private OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnBackToAtyBusLineSearchQuery:
				finishAty();
				break;
			case R.id.btnExchangeFirstAndLastStation:
				exchangeOriginAndTerminalStation();
				break;
			case R.id.btnShowBusLineInMap:
				Intent intent = new Intent(AtyBusLineSearchResult.this,AtyShowBusLineInMapView.class);
				Bundle bundle=new Bundle();
				bundle.putParcelable(BUSLINE_ITEM, currentBusLineItem);
				intent.putExtras(bundle);
				intent.putExtra(CITY_NAME, cityName);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in,R.anim.push_left_out);
				break;
			case R.id.tvBtnCollectBusLine:
			
				Cursor cursor=dbRead.rawQuery("select * from busLine where _id=?", 
						new String[]{currentBusLineItem.getBusLineId()});
				if(cursor.getCount()==0){
					ContentValues cv=new ContentValues();
					cv.put(MyDataBase.COLUMN_NAME_ID, currentBusLineItem.getBusLineId());
					cv.put(MyDataBase.COLUMN_NAME_BUSLINE_NAME, currentBusLineItem.getBusLineName());
					cv.put(MyDataBase.COLUMN_NAME_BUSLINE_ORIGIN_STATION, currentBusLineItem.getOriginatingStation());
					cv.put(MyDataBase.COLUMN_NAME_BUSLINE_TERMINAL_STATION, currentBusLineItem.getTerminalStation());
					dbWrite.insert(MyDataBase.TABLE_NAME_BUSLINE, null, cv);
					Toast.makeText(getApplicationContext(), "收藏成功", 500).show();
				}else{
					Toast.makeText(getApplicationContext(), "已收藏该线路", 500).show();
				}
				break;
			default:
				break;
			}
		}
	};
	
	private void exchangeOriginAndTerminalStation(){
		if(currentBusLineItem.equals(busLineItem)){
			currentBusLineItem=oppositeBusLineItem;
			showBusLineInfo(currentBusLineItem);
		}else{
			currentBusLineItem=busLineItem;
			showBusLineInfo(currentBusLineItem);
		}
	}
	
	private OnItemClickListener itemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			busStationName=busStationsNameList.get(position);
			
			Intent intent=new Intent(AtyBusLineSearchResult.this, AtySearchBusLineByBusStation.class);
			intent.putExtra(CITY_NAME, cityName);
			intent.putExtra(BUS_STATION_NAME, busStationName);
			intent.putExtra(BUSLINE_NAME, busLineName);
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		}
	};

	protected void searchBusLine(String busLineName) {
		busLineQuery = new BusLineQuery(busLineName, SearchType.BY_LINE_NAME,cityName);
		busLineSearch = new BusLineSearch(this, busLineQuery);
		busLineSearch.setOnBusLineSearchListener(this);
		busLineSearch.searchBusLineAsyn();
	}

	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if (rCode == 0) {
			if (result != null && result.getQuery() != null
					&& result.getQuery().equals(busLineQuery)) {
				if (result.getQuery().getCategory() == SearchType.BY_LINE_NAME) {
					if (result.getPageCount() > 0&& result.getBusLines() != null
							&& result.getBusLines().size() > 0) {
						busLineResult = result;
						for (BusLineItem busLine : busLineResult.getBusLines()) {
							if((busLine.getOriginatingStation().equals(busLineItem.getTerminalStation())||
									busLine.getTerminalStation().equals(busLineItem.getOriginatingStation()))&&
									busLine.getBusLineName().substring(0, busLine.getBusLineName().indexOf("(")).
									equals(busLineItem.getBusLineName().
											substring(0, busLineItem.getBusLineName().indexOf("(")))){
								oppositeBusLineItem=busLine;
							}
						}
					}
				} else {
					Toast.makeText(getApplicationContext(), "您查询的公交线路不存在", 500).show();
					finishAty();
				}
			}
		} else {
			Toast.makeText(getApplicationContext(), "查询出错", 500).show();
			finishAty();
		}
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	public String timeFormat(int i) {
		String s = "";
		if (i < 10) {
			s = "0" + i;
		} else {
			s = i + "";
		}
		return s;
	}

	public void finishAty() {
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
	}

	public void showBusLineInfo(BusLineItem busLine) {
		busLineName=busLine.getBusLineName().substring(0,busLine.getBusLineName().indexOf("("));
		tvBusLineName.setText(busLineName);
		tvBusLineFirstStationName.setText(busLine.getOriginatingStation());
		tvBusLineLastStationName.setText(busLine.getTerminalStation());
		tvFirstBusTime.setText(timeFormat(busLine.getFirstBusTime().getHours())
				+ ":" + timeFormat(busLine.getFirstBusTime().getMinutes()));
		tvLastBusTime.setText(timeFormat(busLine.getLastBusTime().getHours())
				+ ":" + timeFormat(busLine.getLastBusTime().getMinutes()));
		tvBusTicketPrice.setText(busLine.getBasicPrice() + "");

		busStationsNameList.clear();
		List<BusStationItem> busStationsList = busLine.getBusStations();
		for (BusStationItem busStationItem : busStationsList) {
			busStationsNameList.add(busStationItem.getBusStationName());
			adapter.notifyDataSetChanged();
		}
		lvResult.setSelection(0);
	}
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() > 0) {
			finishAty();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater = null;

		public MyAdapter(Context context) {
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return busStationsNameList.size();
		}

		@Override
		public String getItem(int position) {
			return busStationsNameList.get(position);
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
				ll = (LinearLayout) inflater.inflate(R.layout.busline_search_busstation_list_item, null);
			}

			TextView tvBusStationItemNum = (TextView) ll.findViewById(R.id.tvItemBusStationNum);
			TextView tvBusStationItemName = (TextView) ll.findViewById(R.id.tvItemBusStationName);

			String busStationItemNum = (position + 1) < 10 ? ("0" + (position + 1)): ((position + 1) + "");
			String busStationItemName = getItem(position);

			tvBusStationItemNum.setText(busStationItemNum);
			tvBusStationItemName.setText(busStationItemName);

			return ll;
		}

	}
}
