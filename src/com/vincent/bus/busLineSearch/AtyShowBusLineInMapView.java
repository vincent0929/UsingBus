package com.vincent.bus.busLineSearch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.model.BitmapDescriptorCreator;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.overlay.BusLineOverlay;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.vincent.bus.R;
import com.vincent.bus.R.anim;
import com.vincent.bus.R.drawable;
import com.vincent.bus.R.id;
import com.vincent.bus.R.layout;

public class AtyShowBusLineInMapView extends Activity implements
		SensorEventListener, LocationSource, AMapLocationListener {

	private MapView mapView = null;
	private AMap aMap = null;
	private Button btnBackToAtyBusLineSearchResult = null;

	private String cityName="";
	private BusLineItem busLineItem=null;
	
	private Marker myGPSMarker;
	
	private OnLocationChangedListener locationChangedListener=null;
	private LocationManagerProxy myAMapLocationManager=null;
	
	private SensorManager mySensorManager=null;
	private Sensor mySensor=null;
	
	private long lastTime;
	private float myAngle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_show_busline_in_mapview);
		mapView = (MapView) findViewById(R.id.mapBusLine);
		mapView.onCreate(savedInstanceState);
		
		btnBackToAtyBusLineSearchResult = (Button) findViewById(R.id.btnAtyShowBusLineInMapViewBackToAtyBusLineSearchResult);
		String str="< 返回";
		btnBackToAtyBusLineSearchResult.setText(str);
		btnBackToAtyBusLineSearchResult.setOnClickListener(listener);

		mySensorManager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mySensor=mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		initAMap();

		cityName=getIntent().getStringExtra(AtyBusLineSearchResult.CITY_NAME);
		busLineItem=getIntent().getParcelableExtra(AtyBusLineSearchResult.BUSLINE_ITEM);
		
		showBusLineRoute();
	}

	private void initAMap() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}	
	}

	private void setUpMap() {
		myGPSMarker = aMap.addMarker((new MarkerOptions()).icon(BitmapDescriptorFactory.
				fromResource(R.drawable.location_marker)));
		aMap.setLocationSource(this);
		aMap.getUiSettings().setMyLocationButtonEnabled(true);
		aMap.setMyLocationEnabled(true);
	}
	
	private void showBusLineRoute(){
		BusLineOverlay busLineOverlay = new BusLineOverlay(this,aMap, busLineItem);
		busLineOverlay.removeFromMap();
		busLineOverlay.addToMap();
		busLineOverlay.zoomToSpan();
	}
	
	private OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnAtyShowBusLineInMapViewBackToAtyBusLineSearchResult:
				finish();
				overridePendingTransition(R.anim.push_left_in,R.anim.push_right_out);
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
		registerSensorListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
		deactivate();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}
	
	private void registerSensorListener() {
		mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void unRegisterSensorListener(){
		mySensorManager.unregisterListener(this, mySensor);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(System.currentTimeMillis()-lastTime<100){
			return;
		}
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ORIENTATION:
			float x=event.values[0];
			x+=getScreenRotationOnPhone(this);
			x%=360.0f;
			if(x<-180){
				x+=360.0f;
			}else if(x>180){
				x-=360.0f;
			}
			if(Math.abs(myAngle-90+x)<3.0f){
				break;
			}
			myAngle=x;
			if(myGPSMarker!=null){
				myGPSMarker.setRotateAngle(-myAngle);
				aMap.invalidate();
			}
			lastTime=System.currentTimeMillis();
			break;
		default:
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		locationChangedListener=listener;
		if(myAMapLocationManager==null){
			myAMapLocationManager=LocationManagerProxy.getInstance(this);
			myAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		locationChangedListener=null;
		if(myAMapLocationManager!=null){
			myAMapLocationManager.removeUpdates(this);
			myAMapLocationManager.destroy();
		}
		myAMapLocationManager=null;
		unRegisterSensorListener();
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
		if(locationChangedListener!=null&&location!=null){
			myGPSMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
			myGPSMarker.setAnchor(0.5f, 0.5f);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() > 0) {
			finish();
			overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	public static int getScreenRotationOnPhone(Context context){
		final Display display=((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		switch (display.getRotation()) {
		case 0:
			return 0;
		case 90:
			return 90;
		case 180:
			return 180;
		case 270:
			return -90;
		default:
			return 0;
		}
	}
}
