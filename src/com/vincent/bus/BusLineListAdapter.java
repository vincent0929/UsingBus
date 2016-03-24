package com.vincent.bus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.busline.BusLineItem;

public class BusLineListAdapter extends BaseAdapter{
	
	private LayoutInflater inflater=null;
	private List<BusLineItem> busLineItems=null;
	
	public BusLineListAdapter(Context context,List<BusLineItem> busLineItems){
		inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.busLineItems=busLineItems;
	}

	@Override
	public int getCount() {
		return busLineItems.size()>0?busLineItems.size():0;
	}

	@Override
	public Object getItem(int position) {
		return busLineItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout ll=null;
		if(convertView!=null){
			ll=(LinearLayout) convertView;
		}else{
			ll=(LinearLayout) inflater.inflate(R.layout.busline_list_item, null);
		}
		String BusLineName=busLineItems.get(position).getBusLineName()
				.substring(0, busLineItems.get(position).getBusLineName().indexOf("("));
		String BusLineFirstStationName=busLineItems.get(position).getOriginatingStation();
		String BusLineLastStationName=busLineItems.get(position).getTerminalStation();
		TextView tvBusLineName=(TextView) ll.findViewById(R.id.tvItemBusLineName);
		TextView tvBusLineFirstStation=(TextView) ll.findViewById(R.id.tvItemFirstStationName);
		TextView tvBusLineLastStation=(TextView) ll.findViewById(R.id.tvItemLastStationName);
		tvBusLineName.setText(BusLineName);
		tvBusLineFirstStation.setText(BusLineFirstStationName);
		tvBusLineLastStation.setText(BusLineLastStationName);
		
		return ll;
	}
	
}