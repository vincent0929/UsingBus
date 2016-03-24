package com.vincent.bus.routeSearch;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.vincent.bus.R;
import com.vincent.bus.R.anim;
import com.vincent.bus.R.id;
import com.vincent.bus.R.layout;

public class AtyRouteSearch extends Activity implements OnRouteSearchListener{

	private String cityName="沈阳";
	
	protected static final int REQUST_CODE_START = 0;
	protected static final int REQUST_CODE_END= 1;
	protected static final String CITY_NAME = "cityName";
	protected static final String BUS_PATHS = "busPaths";

	private static EditText etRouteStartPositionName = null;
	private static EditText etRouteEndPositionName = null;
	private ImageButton iBtnExchangOriginAndTerminalPosition = null;
	private TextView tvBtnSearchRoute = null;
	private LinearLayout llRouteSearch=null;
	
	private static String startPositionName="";
	private static String endPositionName="";
//	private static double startPositionLatitude;
//	private static double startPositionLongitude;
//	private static double endPositionLatitude;
//	private static double endPositionLongitude;
	private static LatLonPoint startPoint;
	private static LatLonPoint endPoint;	
	

	private BusRouteQuery busRouteQuery=null;
	private RouteSearch routeSearch=null;
	private BusRouteResult busRouteResult=null;
	private ArrayList<BusPath> busPaths=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_route_search);
		
		initView();
		busPaths=new ArrayList<BusPath>();
	}
	
	public static void setOriginStation(String busStationName){
		startPositionName=busStationName;
		etRouteStartPositionName.setText(startPositionName);
	}
	
	public static void setTerminalStation(String busStationName){
		endPositionName=busStationName;
		etRouteEndPositionName.setText(endPositionName);
	}
	
//	public static void setStartPositionLatitude(double latitude) {
//		startPositionLatitude = latitude;
//	}
//
//	public static void setStartPositionLongitude(double longitude) {
//		startPositionLongitude = longitude;
//	}
//
//	public static void setEndPositionLatitude(double latitude) {
//		endPositionLatitude = latitude;
//	}
//
//	public static void setEndPositionLongitude(double longitude) {
//		endPositionLongitude = longitude;
//	}

	public static void setStartPoint(LatLonPoint startPoint) {
		AtyRouteSearch.startPoint = startPoint;
	}
	
	public static void setEndPoint(LatLonPoint endPoint) {
		AtyRouteSearch.endPoint = endPoint;
	}
	
	@Override
	protected void onResume() {
		llRouteSearch.setFocusable(true);
		llRouteSearch.setFocusableInTouchMode(true);
		llRouteSearch.clearFocus();

		super.onResume();
	}

	private void initView() {
		etRouteStartPositionName = (EditText) findViewById(R.id.etRouteStartPositionName);
		etRouteEndPositionName = (EditText) findViewById(R.id.etRouteEndPositionName);
		etRouteStartPositionName.setText("");
		etRouteEndPositionName.setText("");
		if (startPositionName!="") {
			etRouteStartPositionName.setText(startPositionName);
		}
		if (endPositionName!="") {
			etRouteEndPositionName.setText(endPositionName);
		}
		etRouteStartPositionName.setOnFocusChangeListener(foucusChangeListener);
		etRouteEndPositionName.setOnFocusChangeListener(foucusChangeListener);
		iBtnExchangOriginAndTerminalPosition = (ImageButton) findViewById(R.id.iBtnExchangeStartAndEndPosition);
		iBtnExchangOriginAndTerminalPosition.setOnClickListener(listener);
		tvBtnSearchRoute = (TextView) findViewById(R.id.tvBtnSearchRoute);
		tvBtnSearchRoute.setOnClickListener(listener);
		llRouteSearch=(LinearLayout) findViewById(R.id.llRouteSearch);
	}

	private OnFocusChangeListener foucusChangeListener=new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			Intent intent=null;
			switch (v.getId()) {
			case R.id.etRouteStartPositionName:
				if (hasFocus) {
					intent = new Intent(AtyRouteSearch.this,AtyRouteStartPositionSelect.class);
					intent.putExtra(CITY_NAME, cityName);
					startActivityForResult(intent, REQUST_CODE_START);
					overridePendingTransition(R.anim.push_bottom_in,R.anim.push_center);
				}
				break;
			case R.id.etRouteEndPositionName:
				if (hasFocus) {
					intent = new Intent(AtyRouteSearch.this,AtyRouteEndPositionSelect.class);
					intent.putExtra(CITY_NAME, cityName);
					startActivityForResult(intent, REQUST_CODE_END);
					overridePendingTransition(R.anim.push_bottom_in,R.anim.push_center);
				}
				break;

			default:
				break;
			}
			
		}
	};
	
	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.iBtnExchangeStartAndEndPosition:
				startPositionName = etRouteStartPositionName.getText().toString();
				etRouteStartPositionName.setText(etRouteEndPositionName.getText().toString());
				etRouteEndPositionName.setText(startPositionName);
				break;
			case R.id.tvBtnSearchRoute:
				searchRoute();
				break;
			default:
				break;
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUST_CODE_START:
			if(resultCode==RESULT_OK){
				startPositionName=data.getStringExtra(AtyRouteStartPositionSelect.START_POSITION_NAME);
//				startPositionLatitude=data.getDoubleExtra(AtyRouteStartPositionSelect.START_POSITION_LATITUDE, 0);
//				startPositionLongitude=data.getDoubleExtra(
//						AtyRouteStartPositionSelect.START_POSITION_LONGITUDE,0);
				etRouteStartPositionName.setText(startPositionName);
				startPoint=data.getParcelableExtra("startPoint");
			}
			break;
		case REQUST_CODE_END:
			if(resultCode==RESULT_OK){
				endPositionName=data.getStringExtra(AtyRouteEndPositionSelect.END_POSITION_NAME);
//				endPositionLatitude=data.getDoubleExtra(AtyRouteEndPositionSelect.END_POSITION_LATITUDE,0);
//				endPositionLongitude=data.getDoubleExtra(AtyRouteEndPositionSelect.END_POSITION_LONGITUDE,0);
				etRouteEndPositionName.setText(endPositionName);
				endPoint=data.getParcelableExtra("endPoint");
			}
			break;
		default:
			break;
		}
	};
	
	private void searchRoute(){
		if(startPositionName.isEmpty()){
			Toast.makeText(getApplicationContext(), "起始点输入不能为空", 500).show();
			return;
		}
		if(startPositionName.isEmpty()){
			Toast.makeText(getApplicationContext(), "终点输入不能为空", 500).show();
			return;
		}
		if(startPositionName.equals(endPositionName)){
			Toast.makeText(getApplicationContext(), "起点与终点距离很近，请步行前往", 500).show();
			return;
		}
		
		RouteSearch.FromAndTo fromAndTo=new RouteSearch.FromAndTo(startPoint,endPoint);
		busRouteQuery=new BusRouteQuery(fromAndTo, RouteSearch.BusDefault, cityName, 1);
		routeSearch=new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		routeSearch.calculateBusRouteAsyn(busRouteQuery);
	}


	@Override
	public void onBusRouteSearched(BusRouteResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getPaths()!=null&&result.getPaths().size()>0){
				busRouteResult=result;			
						
//				for (BusPath path : busRouteResult.getPaths()) {
//					for (BusStep step : path.getSteps()) {
//						String busLineName="";
//						if(step.getBusLine()!=null){
//							busLineName=step.getBusLine().getBusLineName();
//						}else{
//							busLineName="空";
//						}
//						float walkDistance=step.getWalk().getDistance();
//						if(walkDistance<=0){
//							walkDistance=0;
//						}
//					}
//				}
				
				for(BusPath path:busRouteResult.getPaths()){
					busPaths.add(path);
				}
				Intent intent=new Intent(AtyRouteSearch.this,AtyRouteSearchResult.class);
				Bundle bundle=new Bundle();
				bundle.putParcelableArrayList(BUS_PATHS, busPaths);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
			}
		}
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult arg0, int arg1) {
		
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult arg0, int arg1) {
		
	}
	
}
