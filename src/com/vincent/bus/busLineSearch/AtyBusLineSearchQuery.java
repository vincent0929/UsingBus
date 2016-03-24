package com.vincent.bus.busLineSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.vincent.bus.R;
import com.vincent.bus.BusLineListAdapter;

public class AtyBusLineSearchQuery extends Activity implements OnBusLineSearchListener{
	

	protected static final String BUSLINE_ITEM = "busLineItem";
	protected static final String CITY_NAME = "cityName";
	private String busLineName="";
	private String busLineId="";
	private String oppsiteBusLineId="";
	private String cityName="";
	
	private BusLineQuery busLineQuery=null;
	private BusLineSearch busLineSearch=null;
	private BusLineResult busLineResult=null;
	
	private Button btnBackToAtyBusLineSearch=null;
	private ListView lvBusLinesList=null;
	private List<BusLineItem> busLineItems=null;
	private BusLineListAdapter adapter=null;
	
	private ProgressDialog progressDialog=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_busline_search_query);
		
		busLineName=getIntent().getStringExtra(AtyBusLineSearch.BUSLINE_NAME);
		cityName=getIntent().getStringExtra(AtyBusLineSearch.CITY_NAME);

		btnBackToAtyBusLineSearch=(Button) findViewById(R.id.btnBackToAtyBusLineSearch);
		String str="< 返回";
		btnBackToAtyBusLineSearch.setText(str);
		btnBackToAtyBusLineSearch.setOnClickListener(listener);
		lvBusLinesList=(ListView) findViewById(R.id.lvBusLinesList);
		lvBusLinesList.setOnItemClickListener(itemClickListener);
		busLineItems=new ArrayList<BusLineItem>();
		adapter=new BusLineListAdapter(this,busLineItems);
		lvBusLinesList.setAdapter(adapter);
		
		searchBusLine();
		
		progressDialog=ProgressDialog.show(this, null, "正在加载......");
		
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

	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnBackToAtyBusLineSearch:
				finishAty();
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
			Intent intent=new Intent(AtyBusLineSearchQuery.this,AtyBusLineSearchResult.class);
			Bundle bundle=new Bundle();
			bundle.putParcelable(BUSLINE_ITEM, busLineItems.get(position));
			intent.putExtras(bundle);
			intent.putExtra(CITY_NAME, cityName);
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		}
	};
	
	private int getOppositeBusLineId(int position) {
			if (position % 2 == 0) {
				return position+1;
			} else {
				return position-1;
			}
	}
	
	private void searchBusLine(){
		busLineQuery=new BusLineQuery(busLineName, SearchType.BY_LINE_NAME, cityName);
		busLineSearch=new BusLineSearch(this, busLineQuery);
		busLineSearch.setOnBusLineSearchListener(this);
		busLineSearch.searchBusLineAsyn();
	}

	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&
					result.getQuery().equals(busLineQuery)){
				if(result.getQuery().getCategory()==SearchType.BY_LINE_NAME){
					if(result.getPageCount()>0&&result.getBusLines()!=null&&
							result.getBusLines().size()>0){
						busLineItems.addAll(result.getBusLines());
						progressDialog.dismiss();
						adapter.notifyDataSetChanged();
					}else{		
						Toast.makeText(getApplicationContext(), "您查询的公交线路不存在", 500).show();
						finishAty();
					}
				}
			}else{
				Toast.makeText(getApplicationContext(), "查询出错", 500).show();
				finishAty();
			}
		}
	}
	
	public void finishAty(){
		if(progressDialog.isShowing()){
			progressDialog.dismiss();
		}
		finish();
		overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
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

	
}
