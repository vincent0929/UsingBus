package com.vincent.bus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.busline.BusStationItem;

public class StationListAdapter extends BaseAdapter{

	private LayoutInflater inflater;
	private List<BusStationItem> busStationItems;
	
	public StationListAdapter(Context context,List<BusStationItem> busStationItems){
		inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.busStationItems=busStationItems;
	}
	
	@Override
	public int getCount() {
		return busStationItems.size();
	}

	@Override
	public BusStationItem getItem(int position) {
		return busStationItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout ll;
		if(convertView!=null){
			ll=(LinearLayout) convertView;
		}else{
			ll=(LinearLayout) inflater.inflate(R.layout.station_list_item, null);
		}
		
		TextView tvStationItemName=(TextView) ll.findViewById(R.id.tvStationItemName);
		TextView tvStationItemType=(TextView) ll.findViewById(R.id.tvStationItemType);
		TextView tvPassStationBusLineName=(TextView) ll.findViewById(R.id.tvPassStationBusLineName);
		
		String stationName=busStationItems.get(position).getBusStationName();
		tvStationItemName.setText(stationName);
		tvStationItemType.setText(stationName.
				subSequence(stationName.indexOf("(")+1, stationName.indexOf(")")));
		
		StringBuffer buffer=new StringBuffer();
		List<BusLineItem> list=busStationItems.get(position).getBusLineItems();
		for (BusLineItem busLineItem : list) {
			buffer.append(busLineItem.getBusLineName().
					subSequence(0, busLineItem.getBusLineName().indexOf("("))).append(",");
		}
		
		
		System.out.println("count:"+getCount());
		System.out.println("+++++++++++"+stationName+"++++++++++++++");
		System.out.println(buffer.toString());

		tvPassStationBusLineName.setText(buffer.substring(0, buffer.length()-1));
		
		return ll;
	}

}
