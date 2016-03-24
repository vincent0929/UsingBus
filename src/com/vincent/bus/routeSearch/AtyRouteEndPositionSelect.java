package com.vincent.bus.routeSearch;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AtyRouteEndPositionSelect extends Activity implements OnPoiSearchListener{
	protected static final String END_POSITION_NAME = "endPositionName";
	protected static final String END_POSITION_LATITUDE = "endPositionLatitude";
	protected static final String END_POSITION_LONGITUDE="endPositionLongitude";
	protected static final int REQUST_CODE_SELECT_END_POINT_IN_MAPVIEW = 1;

	private EditText etEndPositionName=null;
	private TextView tvSelectEndPositionInMapView=null;
	private ListView lvEndPositons=null;
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
		setContentView(R.layout.aty_route_end_position_select);
		
		initView();
		
		cityName=getIntent().getStringExtra(AtyRouteSearch.CITY_NAME);
	}
	
	private void initView() {
		etEndPositionName=(EditText) findViewById(R.id.etEndPositionName);
		etEndPositionName.addTextChangedListener(watcher);
		tvSelectEndPositionInMapView=(TextView) findViewById(R.id.tvSelectEndPositionInMapView);
		tvSelectEndPositionInMapView.setText("地图选点");
		tvSelectEndPositionInMapView.setOnClickListener(listener);
		lvEndPositons=(ListView) findViewById(R.id.lvEndPositions);
		lvEndPositons.setOnItemClickListener(itemClickListener);
		poiItems=new ArrayList<PoiItem>();
		
		imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	private TextWatcher watcher=new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (!etEndPositionName.getText().toString().isEmpty()) {
				searchEndPosition(etEndPositionName.getText().toString().trim());
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
			case R.id.tvSelectEndPositionInMapView:
				Intent intent=new Intent(AtyRouteEndPositionSelect.this,AtySelectPointInMap.class);
				startActivityForResult(intent, REQUST_CODE_SELECT_END_POINT_IN_MAPVIEW);
				break;
			default:
				break;
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUST_CODE_SELECT_END_POINT_IN_MAPVIEW:
			if(resultCode==RESULT_OK){
				LatLonPoint point=data.getParcelableExtra("point");
				Intent intent=new Intent();
				Bundle bundle=new Bundle();
				bundle.putParcelable("endPoint", point);
				intent.putExtras(bundle);
				intent.putExtra("endPositionName", "("+point.getLatitude()+","+point.getLongitude()+")");
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
			intent.putExtra(END_POSITION_NAME, adapter.getItem(position).getTitle());
//			intent.putExtra(END_POSITION_LATITUDE, adapter.getItem(position).getLatLonPoint().getLatitude());
//			intent.putExtra(END_POSITION_LONGITUDE,adapter.getItem(position).getLatLonPoint().getLongitude());
			Bundle bundle=new Bundle();
			bundle.putParcelable("endPoint", adapter.getItem(position).getLatLonPoint());
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
			overridePendingTransition(R.anim.push_center, R.anim.push_bottom_out);
		}
	};
	
	private void searchEndPosition(String endPositionName){
		poiSearchQuery=new PoiSearch.Query(endPositionName, cityName);
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
					lvEndPositons.setAdapter(adapter);
					adapter.notifyDataSetInvalidated();
					imm.hideSoftInputFromWindow(etEndPositionName.getWindowToken(), 0);
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

