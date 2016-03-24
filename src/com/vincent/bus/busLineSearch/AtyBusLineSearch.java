package com.vincent.bus.busLineSearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vincent.bus.R;


public class AtyBusLineSearch extends Activity {
	private EditText etBusLineName=null;
	private Button btnSearchBusLine=null;
	private TextView tvBtnSearchBusLineNearby=null;
	private TextView tvBtnShowCollectBusLines=null;
	
	public static final String BUSLINE_NAME="buslineName";
	public static final int SEARCH_BUSLINE_REQUST_CODE=0;
	public static final String CITY_NAME = "cityName";
	
	private String cityName="沈阳";

	private InputMethodManager imm=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_busline_search);
		
		etBusLineName=(EditText) findViewById(R.id.etBusLineName);
		btnSearchBusLine=(Button) findViewById(R.id.btnSearchBusLine);
		btnSearchBusLine.setOnClickListener(listener);
		tvBtnSearchBusLineNearby=(TextView) findViewById(R.id.tvBtnSearchBusLineNearby);
		tvBtnSearchBusLineNearby.setOnClickListener(listener);
		tvBtnShowCollectBusLines=(TextView) findViewById(R.id.tvBtnShowCollectBusLines);
		tvBtnShowCollectBusLines.setOnClickListener(listener);
		
		imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent=null;
			switch (v.getId()) {
			case R.id.btnSearchBusLine:
				if (!etBusLineName.getText().toString().isEmpty()) {
					intent = new Intent(AtyBusLineSearch.this,AtyBusLineSearchQuery.class);
					intent.putExtra(BUSLINE_NAME, etBusLineName.getText().toString().trim());
					intent.putExtra(CITY_NAME, cityName);
					startActivity(intent);
					overridePendingTransition(R.anim.push_right_in,R.anim.push_left_out);
					imm.hideSoftInputFromWindow(etBusLineName.getWindowToken(),0);
				}else{
					Toast.makeText(getApplicationContext(), "请输入查询的公交线路的名称", 500).show();
				}
				break;
			case R.id.tvBtnSearchBusLineNearby:
				intent=new Intent(AtyBusLineSearch.this,AtyShowBusLineNearby.class);
				intent.putExtra(CITY_NAME, cityName);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				break;
			case R.id.tvBtnShowCollectBusLines:
				intent=new Intent(AtyBusLineSearch.this,AtyShowCollectBusLines.class);
				startActivity(intent);
				overridePendingTransition(R.anim.push_bottom_in, R.anim.push_center);
				break;
			default:
				break;
			}
		}	
	};
}
