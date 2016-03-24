package com.vincent.bus.busLineSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineResult;
import com.amap.api.services.busline.BusLineSearch;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusLineSearch.OnBusLineSearchListener;
import com.vincent.bus.BusLineListAdapter;
import com.vincent.bus.MainActivity;
import com.vincent.bus.R;
import com.vincent.bus.database.MyDataBase;

public class AtyShowCollectBusLines extends Activity implements OnBusLineSearchListener{
	
	private Button btnAtyShowColletBusLines;
	private ListView lvCollectBusLines;
	private BusLineListAdapter adapter=null;
	private List<BusLineItem> busLineItems=null;
	
	private Button btnAtyShowCollectBusLinesBackTo=null;
	
	private Cursor cursor=null;
	private MyDataBase db=null;
	private SQLiteDatabase dbRead=null;
	
	private BusLineQuery busLineQuery=null;
	private BusLineSearch busLineSearch=null;
	private BusLineResult busLineResult=null;
	
	private ProgressDialog progressDialog=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_show_collect_buslines);
		
		db=new MyDataBase(this);
		dbRead=db.getReadableDatabase();
		
		btnAtyShowCollectBusLinesBackTo=(Button) findViewById(R.id.btnAtyShowCollectBusLinesBackTo);
		btnAtyShowCollectBusLinesBackTo.setText("< 返回");
		btnAtyShowCollectBusLinesBackTo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
			}
		});
		
		lvCollectBusLines=(ListView) findViewById(R.id.lvColletBusLines);
		busLineItems=new ArrayList<BusLineItem>();
		adapter=new BusLineListAdapter(this, busLineItems);
		lvCollectBusLines.setAdapter(adapter);
		
		lvCollectBusLines.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

				progressDialog=ProgressDialog.show(AtyShowCollectBusLines.this, null, "正在加载......");
				
				handler.sendEmptyMessageDelayed(0, 10000);
				searchBusLine(position);
			}
		});
		
		cursor = dbRead.rawQuery("select * from busLine", null);
		while (cursor.moveToNext()) {
			BusLineItem busLineItem = new BusLineItem();
			busLineItem.setBusLineId(cursor.getString(cursor.getColumnIndex(MyDataBase.COLUMN_NAME_ID)));
			busLineItem.setBusLineName(cursor.getString(cursor.getColumnIndex(MyDataBase.COLUMN_NAME_BUSLINE_NAME)));
			busLineItem.setOriginatingStation(cursor.getString(cursor.getColumnIndex(MyDataBase.COLUMN_NAME_BUSLINE_ORIGIN_STATION)));
			busLineItem.setTerminalStation(cursor.getString(cursor.getColumnIndex(MyDataBase.COLUMN_NAME_BUSLINE_TERMINAL_STATION)));
			
			System.out.println("busId:"+cursor.getString(cursor.getColumnIndex(MyDataBase.COLUMN_NAME_ID)));
			
			busLineItems.add(busLineItem);
			adapter.notifyDataSetChanged();
		}
	}
	
	private void searchBusLine(int position){
		busLineQuery=new BusLineQuery(busLineItems.get(position).getBusLineId(), 
				SearchType.BY_LINE_ID, MainActivity.getCityName());
		busLineSearch=new BusLineSearch(this, busLineQuery);
		busLineSearch.setOnBusLineSearchListener(this);
		busLineSearch.searchBusLineAsyn();
	}

	@Override
	public void onBusLineSearched(BusLineResult result, int rCode) {
		if(rCode==0){
			if(result!=null&&result.getQuery()!=null&&
					result.getQuery().equals(busLineQuery)){
				if(result.getQuery().getCategory()==SearchType.BY_LINE_ID){
					if(result.getPageCount()>0&&result.getBusLines()!=null&&
							result.getBusLines().size()>0){
						progressDialog.dismiss();
						
						Intent intent=new Intent(AtyShowCollectBusLines.this,AtyBusLineSearchResult.class);
						Bundle bundle=new Bundle();
						bundle.putParcelable("busLineItem", result.getBusLines().get(0));
						intent.putExtras(bundle);
						intent.putExtra("cityName", MainActivity.getCityName());
						startActivity(intent);
						overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
					}
				}
			}else{
				Toast.makeText(getApplicationContext(), "查询出错", 500).show();
				if(progressDialog.isShowing()){
					progressDialog.dismiss();
				}
			}
		}
	}
	
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(progressDialog.isShowing()==true){
				Toast.makeText(getApplicationContext(), "网络不畅或网络连接中断", 500).show();
				progressDialog.dismiss();
			}
		};
	};
}
