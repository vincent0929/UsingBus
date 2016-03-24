package com.vincent.bus.routeSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.mapcore2d.et;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.vincent.bus.PoisListAdapter;
import com.vincent.bus.R;
import com.vincent.bus.R.anim;
import com.vincent.bus.R.id;
import com.vincent.bus.R.layout;

public class AtyRouteStartPositionSelect extends Activity implements OnPoiSearchListener{

	protected static final String START_POSITION_NAME = "startPositionName";
	protected static final String START_POSITION_LATITUDE = "startPositionLatitude";
	protected static final String START_POSITION_LONGITUDE="startPositionLongitude";
	protected static final int REQUST_CODE_SELECT_START_POINT_IN_MAPVIEW = 0;
	
	private EditText etStartPositionName=null;
	private TextView tvSelectStartPositionInMapView=null;
	private ListView lvStartPositons=null;
	private PoisListAdapter adapter=null;
	
	private PoiSearch.Query poiSearchQuery=null;
	private PoiSearch poiSearch=null;
	private PoiResult poiResult=null;
	private List<PoiItem> poiItems=null;
	
	private String cityName="";
	
	private ProgressDialog progressDialog=null;
	
	private InputMethodManager imm=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_route_start_position_select);
		
		initView();
		
		cityName=getIntent().getStringExtra(AtyRouteSearch.CITY_NAME);
	}

	private void initView() {;
		etStartPositionName=(EditText) findViewById(R.id.etStartPositionName);
		etStartPositionName.addTextChangedListener(watcher);
		tvSelectStartPositionInMapView=(TextView) findViewById(R.id.tvSelectStartPositionInMapView);
		tvSelectStartPositionInMapView.setText("地图选点");
		tvSelectStartPositionInMapView.setOnClickListener(listener);
		lvStartPositons=(ListView) findViewById(R.id.lvStartPositions);
		lvStartPositons.setOnItemClickListener(itemClickListener);
		poiItems=new ArrayList<PoiItem>();
		
		imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	private TextWatcher watcher=new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!etStartPositionName.getText().toString().isEmpty()) {
				searchStartPosition(etStartPositionName.getText().toString().trim());
			}
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			
		}
	};
	
	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tvSelectStartPositionInMapView:
				Intent intent=new Intent(AtyRouteStartPositionSelect.this,AtySelectPointInMap.class);
				startActivityForResult(intent, REQUST_CODE_SELECT_START_POINT_IN_MAPVIEW);
				break;
			default:
				break;
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUST_CODE_SELECT_START_POINT_IN_MAPVIEW:
			if(resultCode==RESULT_OK){
				LatLonPoint point=data.getParcelableExtra("point");
				Intent intent=new Intent();
				Bundle bundle=new Bundle();
				bundle.putParcelable("startPoint", point);
				intent.putExtras(bundle);
				intent.putExtra("startPositionName", "("+point.getLatitude()+","+point.getLongitude()+")");
				setResult(RESULT_OK, intent);
				finish();
			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	};
	
	private OnItemClickListener itemClickListener=new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent=new Intent();
			PoiItem poiItem=adapter.getItem(position);
			intent.putExtra(START_POSITION_NAME, poiItem.getTitle());
//			intent.putExtra(START_POSITION_LATITUDE, poiItem.getLatLonPoint().getLatitude());
//			intent.putExtra(START_POSITION_LONGITUDE, poiItem.getLatLonPoint().getLongitude());
			Bundle bundle=new Bundle();
			bundle.putParcelable("startPoint", poiItem.getLatLonPoint());
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			overridePendingTransition(R.anim.push_center, R.anim.push_bottom_out);
		}
	};
	
	private void searchStartPosition(String startPositionName){
		poiSearchQuery=new PoiSearch.Query(startPositionName, cityName);
		poiSearch=new PoiSearch(this, poiSearchQuery);
		poiSearch.setOnPoiSearchListener(this);
		poiSearch.searchPOIAsyn();
	}

	@Override
	public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		
	}

	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&
					result.getQuery().equals(poiSearchQuery)){
				if(result.getPageCount()>0&&result.getPois()!=null&&
						result.getPois().size()>0){
					poiResult=result;
					poiItems.clear();
					poiItems=poiResult.getPois();
					adapter=new PoisListAdapter(this, poiItems);
					lvStartPositons.setAdapter(adapter);
					adapter.notifyDataSetInvalidated();
					imm.hideSoftInputFromWindow(etStartPositionName.getWindowToken(), 0);
				}
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			overridePendingTransition(R.anim.push_center, R.anim.push_bottom_out);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
