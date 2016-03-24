package com.vincent.bus.routeSearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.vincent.bus.R;

public class AtySelectPointInMap extends Activity {

	private Button btnAtySelectPointInMapBackTo = null, btnSelectPointInMap = null;
	private Button btnGetPoint = null;
	private MapView mapView = null;
	private AMap aMap = null;

	private Marker marker = null;

	private int width;
	private int height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_select_point_in_map);
		mapView = (MapView) findViewById(R.id.mapSelectPoint);
		mapView.onCreate(savedInstanceState);

		btnAtySelectPointInMapBackTo = (Button) findViewById(R.id.btnAtySelectPointInMapBackTo);
		btnAtySelectPointInMapBackTo.setText("< 返回");
		btnSelectPointInMap = (Button) findViewById(R.id.btnSelectPointInMap);
		btnSelectPointInMap.setOnClickListener(listener);
		btnGetPoint = (Button) findViewById(R.id.btnGetPoint);
		btnGetPoint.setOnClickListener(listener);

		initAMap();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		mapView.onResume();		
		super.onResume();
	}

	@Override
	protected void onPause() {
		mapView.onPause();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mapView.onDestroy();
		super.onDestroy();
	}
	
	

//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//		super.onWindowFocusChanged(hasFocus);
//		if(hasFocus){
//			width=mapView.getWidth();
//			height=mapView.getHeight();
//			toString();
//		}
//	}

	private void initAMap() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	private void setUpMap() {
		marker = aMap.addMarker((new MarkerOptions()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		marker.setAnchor(0.5f, 1.0f);
		marker.setDraggable(false);
		marker.setPositionByPixels(getWindowManager().getDefaultDisplay().getWidth()/2, 
				getWindowManager().getDefaultDisplay().getHeight()/2);
	}

	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnAtySelectPointInMapBackTo:
				setResult(RESULT_CANCELED);
				finishAty();
				break;
			case R.id.btnGetPoint:
				LatLonPoint point=new LatLonPoint(
						marker.getPosition().latitude, marker.getPosition().longitude);
				Intent intent=new Intent();
				Bundle bundle=new Bundle();
				bundle.putParcelable("point", point);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
				break;
			default:
				break;
			}
		}
	};

	private void finishAty() {
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
	}
}
