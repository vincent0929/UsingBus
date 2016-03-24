package com.vincent.bus;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;

public class PathListAdapter extends BaseAdapter {

	private ArrayList<BusPath> busPaths = null;
	private LayoutInflater inflater = null;

	public PathListAdapter(Context context, ArrayList<BusPath> busPaths) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.busPaths = busPaths;
	}

	@Override
	public int getCount() {
		return busPaths.size();
	}

	@Override
	public BusPath getItem(int position) {
		return busPaths.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout ll = null;
		BusPath path = busPaths.get(position);
//		if (convertView != null) {
//			ll = (LinearLayout) convertView;
//		} else {
			ll = (LinearLayout) inflater.inflate(R.layout.path_list_item, null);
//		}
		TextView tvPathsListItem = (TextView) ll.findViewById(R.id.tvPathsListItem);
		TextView tvPathNum = (TextView) ll.findViewById(R.id.tvPathNum);
		LinearLayout llPathListItemBusLines = (LinearLayout) ll.findViewById(R.id.llPathListItemBusLines);
		TextView tvAllTime = (TextView) ll.findViewById(R.id.tvAllTime);
		TextView tvAllDistance = (TextView) ll.findViewById(R.id.tvAllDistance);
		TextView tvAllWalkDistance = (TextView) ll.findViewById(R.id.tvAllWalkDistance);

		tvPathNum.setText((position + 1) + "");

		String lastBusLineName="";
		
		for (int i=0;i<path.getSteps().size();i++) {
			BusStep step=path.getSteps().get(i);

			String tempBusLineName = "";

			TextView tvBusLine = (TextView) inflater.inflate(R.layout.path_list_item_busline_textview, null);
			if (step.getBusLine() != null) {
				tempBusLineName = step.getBusLine().getBusLineName();
				System.out.println("tempBusLineName:"+tempBusLineName);
			} else {
				continue;
			}
			
			if (!tempBusLineName.equals(lastBusLineName)) {
				lastBusLineName = tempBusLineName;
				tempBusLineName = tempBusLineName.substring(0,tempBusLineName.indexOf("("));
				tvBusLine.setText(tempBusLineName);
				llPathListItemBusLines.addView(tvBusLine);
			}else{
				tvBusLine=null;
			}

			if (i != path.getSteps().size() - 2) {
				ImageView tvArrowRight = (ImageView) inflater.inflate(R.layout.path_list_item_arrow_right, null);
				tvArrowRight.setLayoutParams(new LayoutParams(30, 30));
				llPathListItemBusLines.addView(tvArrowRight);
			}
		}

		tvAllTime.setText(path.getDuration() / 60 + "分钟");
		tvAllDistance.setText(path.getDistance() / 1000.0 + "公里");
		tvAllWalkDistance.setText("步行" + path.getWalkDistance() + "米");

		StringBuffer sBuffer = new StringBuffer();
		
		System.out.println("&&&&&&&&&&&&&&&&&&");
		for (BusStep step : path.getSteps()) {
			
			System.out.println("---------------------------");
			System.out.println("步行："+step.getWalk().getDistance());
			if (step.getBusLine() != null) {
				System.out.println("乘坐："+step.getBusLine().getBusLineName());
			}
			System.out.println("+++++++++++++++++");
			
			if (step != path.getSteps().get(0)) {
				sBuffer.append(",");
			}
			sBuffer.append("行走" + step.getWalk().getDistance() + "米");
			if (step.getBusLine() != null) {
				sBuffer.append(",然后乘坐" + step.getBusLine().getBusLineName());
			}
		}
		tvPathsListItem.setText(sBuffer.toString());
		
		System.out.println("&&&&&&&&&&&&&&&&&&");

		return ll;
	}

}
