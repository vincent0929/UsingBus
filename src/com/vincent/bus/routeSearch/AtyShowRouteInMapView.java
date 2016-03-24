package com.vincent.bus.routeSearch;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.overlay.BusRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.vincent.bus.MainActivity;
import com.vincent.bus.R;

public class AtyShowRouteInMapView extends Activity implements SensorEventListener, LocationSource, AMapLocationListener {

	private Button btnAtyShowRouteInMapViewBack;
	private MapView mapView;
	private AMap aMap;

	private String cityName = "";
	private BusPath busPath;

	private Marker myGPSMarker;

	private OnLocationChangedListener locationChangedListener = null;
	private LocationManagerProxy myAMapLocationManager = null;

	private SensorManager mySensorManager = null;
	private Sensor mySensor = null;

	private long lastTime;
	private float myAngle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_show_route_in_mapview);
		mapView = (MapView) findViewById(R.id.mapRoute);
		mapView.onCreate(savedInstanceState);

		btnAtyShowRouteInMapViewBack = (Button) findViewById(
				R.id.btnAtyShowRouteInMapViewBackToAtyRouteSearchResult);
		btnAtyShowRouteInMapViewBack.setOnClickListener(listener);
		String str = "< 返回";
		btnAtyShowRouteInMapViewBack.setText(str);

		mySensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		initAMap();

		cityName = MainActivity.getCityName();
		busPath = getIntent().getParcelableExtra("path");

		showRoute();
	}

	private void initAMap() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	private void setUpMap() {
		myGPSMarker = aMap.addMarker((new MarkerOptions()).icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker)));
		aMap.setLocationSource(this);
		aMap.getUiSettings().setMyLocationButtonEnabled(true);
		aMap.setMyLocationEnabled(true);
	}

	private void showRoute() {	
		LatLonPoint routeOrigin=busPath.getSteps().get(0).getWalk().getOrigin();
		LatLonPoint routeDestination=busPath.getSteps().get(busPath.getSteps().size()-1).getWalk().getDestination();
		BusRouteOverlay busRouteOverlay=new BusRouteOverlay(this, aMap, busPath, routeOrigin, routeDestination);
		busRouteOverlay.removeFromMap();
		busRouteOverlay.addToMap();
		busRouteOverlay.zoomToSpan();
	}

	private OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnAtyShowRouteInMapViewBackToAtyRouteSearchResult:
				finish();
				overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
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

	private void unRegisterSensorListener() {
		mySensorManager.unregisterListener(this, mySensor);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (System.currentTimeMillis() - lastTime < 100) {
			return;
		}
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ORIENTATION:
			float x = event.values[0];
			x += getScreenRotationOnPhone(this);
			x %= 360.0f;
			if (x < -180) {
				x += 360.0f;
			} else if (x > 180) {
				x -= 360.0f;
			}
			if (Math.abs(myAngle - 90 + x) < 3.0f) {
				break;
			}
			myAngle = x;
			if (myGPSMarker != null) {
				myGPSMarker.setRotateAngle(-myAngle);
				aMap.invalidate();
			}
			lastTime = System.currentTimeMillis();
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
		locationChangedListener = listener;
		if (myAMapLocationManager == null) {
			myAMapLocationManager = LocationManagerProxy.getInstance(this);
			myAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	@Override
	public void deactivate() {
		locationChangedListener = null;
		if (myAMapLocationManager != null) {
			myAMapLocationManager.removeUpdates(this);
			myAMapLocationManager.destroy();
		}
		myAMapLocationManager = null;
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
		if (locationChangedListener != null && location != null) {
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

	public static int getScreenRotationOnPhone(Context context) {
		final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
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
