package com.vincent.bus;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiSearch;

public class PoisListAdapter extends BaseAdapter {

	private List<PoiItem> poiItems=null;
	private LayoutInflater inflater=null;
	
	public PoisListAdapter(Context context,List<PoiItem> poiItems){
		inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.poiItems=poiItems;
	}
	
	@Override
	public int getCount() {
		return poiItems.size()>0?poiItems.size():0;
	}

	@Override
	public PoiItem getItem(int position) {
		return poiItems.get(position);
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
			ll=(LinearLayout) inflater.inflate(R.layout.bus_station_list_item, null);
		}
		
		TextView tvStartStationItemName=(TextView) ll.findViewById(R.id.tvStartStationItemName);
		TextView tvStartStationItemType=(TextView) ll.findViewById(R.id.tvStartStationItemType);
		TextView tvPassStartStationBusLineName=(TextView) ll.findViewById(R.id.tvPassStartStationBusLineName);
		
		String strStartStationItemName=getItem(position).getTitle();
		String strStartStationItemType=getItem(position).getTypeDes();
		if (!strStartStationItemType.isEmpty()) {
			String[] strs = strStartStationItemType.split(";");
			strStartStationItemType = strs[strs.length - 1];
		}
		
		String strPassStartStationBusLineName=getItem(position).toString();
		
		tvStartStationItemName.setText(strStartStationItemName);
		tvStartStationItemType.setText("-"+strStartStationItemType);
		tvPassStartStationBusLineName.setText(strPassStartStationBusLineName);
		
		return ll;
	}

}
