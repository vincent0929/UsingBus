package com.vincent.bus;

import com.amap.api.services.busline.BusStationItem;
import com.vincent.bus.routeSearch.AtyRouteSearch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

	public static final String ACTION = "com.vincent.bus.routeSearch.MyReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String temp = intent.getStringExtra("station");
		BusStationItem busStationItem = intent.getParcelableExtra("busStationItem");

		if (temp.equals("OriginStation")) {
			AtyRouteSearch.setOriginStation(busStationItem.getBusStationName());
//			AtyRouteSearch.setStartPositionLatitude(Double.valueOf(busStationItem.getLatLonPoint().getLatitude()));
//			AtyRouteSearch.setStartPositionLongitude(Double.valueOf(busStationItem.getLatLonPoint().getLongitude()));
			AtyRouteSearch.setStartPoint(busStationItem.getLatLonPoint());
		} else if (temp.equals("TerminalStation")) {
			AtyRouteSearch.setTerminalStation(busStationItem.getBusStationName());
//			AtyRouteSearch.setEndPositionLatitude(Double.valueOf(busStationItem.getLatLonPoint().getLatitude()));
//			AtyRouteSearch.setEndPositionLongitude(Double.valueOf(busStationItem.getLatLonPoint().getLongitude()));
			AtyRouteSearch.setEndPoint(busStationItem.getLatLonPoint());
		}
	}
}
