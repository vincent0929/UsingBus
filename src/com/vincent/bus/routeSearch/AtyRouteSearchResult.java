package com.vincent.bus.routeSearch;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.vincent.bus.MainActivity;
import com.vincent.bus.PathListAdapter;
import com.vincent.bus.R;

public class AtyRouteSearchResult extends Activity {
	
	protected static final String PATH = "path";
	private Button btnBackToAtyRouteSearch=null;
	private TextView tvBtnSortPathByLessTime=null;
	private TextView tvBtnSortPathByLessBusLine=null;
	private TextView tvBtnSortPathByLessWalkDistance=null;
	private ListView lvPathsList=null;
	private ArrayList<BusPath> busPaths=null;
	private PathListAdapter adapter=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_route_search_result);
		
		busPaths=getIntent().getParcelableArrayListExtra(AtyRouteSearch.BUS_PATHS);
		
		initView();
		
		sortPathsByTime();
	}

	private void initView() {
		btnBackToAtyRouteSearch=(Button) findViewById(R.id.btnBackToAtyRouteSearch);
		btnBackToAtyRouteSearch.setText("< 返回");
		tvBtnSortPathByLessTime=(TextView) findViewById(R.id.tvBtnSortPathByLessTime);
		tvBtnSortPathByLessBusLine=(TextView) findViewById(R.id.tvBtnSortPathByLessBusLine);
		tvBtnSortPathByLessWalkDistance=(TextView) findViewById(R.id.tvBtnSortPathByLessWalkDistance);
		btnBackToAtyRouteSearch.setOnClickListener(listener);
		tvBtnSortPathByLessTime.setOnClickListener(listener);
		tvBtnSortPathByLessBusLine.setOnClickListener(listener);
		tvBtnSortPathByLessWalkDistance.setOnClickListener(listener);
		lvPathsList=(ListView) findViewById(R.id.lvPathsList);
		lvPathsList.setOnItemClickListener(itemClickListener);		
		adapter=new PathListAdapter(this , busPaths);
		lvPathsList.setAdapter(adapter);
		lvPathsList.setOnItemClickListener(itemClickListener);
	}
	
	private OnClickListener listener=new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnBackToAtyRouteSearch:
				finish();
				overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
				break;
			case R.id.tvBtnSortPathByLessTime:
				sortPathsByTime();
				break;
			case R.id.tvBtnSortPathByLessBusLine:
				sortPathsByChangeBusTimes();
				break;
			case R.id.tvBtnSortPathByLessWalkDistance:
				sortPathByWalkDistance();
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
			Intent intent=new Intent(AtyRouteSearchResult.this,AtyShowRouteInMapView.class);
			Bundle bundle=new Bundle();
			bundle.putParcelable(PATH, busPaths.get(position));
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
		}
	};
	
	private void sortPathsByTime() {
		tvBtnSortPathByLessTime.setTextColor(Color.rgb(00, 122, 255));
		tvBtnSortPathByLessBusLine.setTextColor(Color.rgb(92, 92, 92));
		tvBtnSortPathByLessWalkDistance.setTextColor(Color.rgb(92, 92, 92));
		
		BusPath path=new BusPath();
		for(int i=0;i<busPaths.size();i++){
			for(int j=i+1;j<busPaths.size();j++){
				long pathIDutarion=busPaths.get(i).getDuration();
				long pathJDuration=busPaths.get(j).getDuration();		
				if(pathIDutarion>pathJDuration){
					path=busPaths.get(i);
					busPaths.set(i, busPaths.get(j));
					busPaths.set(j, path);
					adapter.notifyDataSetInvalidated();
				}
			}
		}
	}
	
	private void sortPathsByChangeBusTimes() {		
		tvBtnSortPathByLessBusLine.setTextColor(Color.rgb(00, 122, 255));
		tvBtnSortPathByLessTime.setTextColor(Color.rgb(92, 92, 92));
		tvBtnSortPathByLessWalkDistance.setTextColor(Color.rgb(92, 92, 92));
		
		BusPath path = new BusPath();
		for (int i = 0; i < busPaths.size(); i++) {
			for (int j = i + 1; j < busPaths.size(); j++) {
				int pathIBusLineNum = getPathBusLineNum(busPaths.get(i));
				int pathJBusLineNum = getPathBusLineNum(busPaths.get(j));
				if (pathIBusLineNum > pathJBusLineNum) {
					path = busPaths.get(i);
					busPaths.set(i,busPaths.get(j));
					busPaths.set(j, path);
					adapter.notifyDataSetInvalidated();
				}
			}
		}
	}
	
	private int getPathBusLineNum(BusPath path){
		int num=0;
		for (BusStep step : path.getSteps()) {
			if(step.getBusLine()!=null){
				num++;
			}
		}
		return num;
	}
	
	private void sortPathByWalkDistance() {
		tvBtnSortPathByLessWalkDistance.setTextColor(Color.rgb(00, 122, 255));
		tvBtnSortPathByLessBusLine.setTextColor(Color.rgb(92, 92, 92));
		tvBtnSortPathByLessTime.setTextColor(Color.rgb(92, 92, 92));
				
		BusPath path = new BusPath();
		for (int i = 0; i < busPaths.size(); i++) {
			for (int j = i + 1; j < busPaths.size(); j++) {
				float pathIWalkDistance=busPaths.get(i).getWalkDistance();
				float pathJWalkDistance=busPaths.get(j).getWalkDistance();
				if (pathIWalkDistance > pathJWalkDistance) {
					path = busPaths.get(i);
					busPaths.set(i,busPaths.get(j));
					busPaths.set(j, path);
					adapter.notifyDataSetInvalidated();
				}
			}
		}
	}
}
