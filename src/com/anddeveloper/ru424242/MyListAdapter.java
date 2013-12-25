package com.anddeveloper.ru424242;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListAdapter extends BaseAdapter {
	Context mContext;
	Application mApp;
	LayoutInflater mInflater;
	ArrayList<Object[]> mData;
	
	public MyListAdapter(Context context, Application app) {
		mContext = context;
		mApp = app;
		mData = new ArrayList<Object[]>();
		mInflater = (LayoutInflater) context.getSystemService(Enterprise.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(ArrayList<Object[]> data) {
		mData = data;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListHolder listholder;
		
		Object[] info = mData.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.enterprise_dtl, null);
			
			listholder = new ListHolder();
			
			listholder.name = (TextView) convertView.findViewById(R.id.txtName);
			listholder.unit = (TextView) convertView.findViewById(R.id.txtUnit);
			listholder.image = (ImageView) convertView.findViewById(R.id.ivImage);
			
			convertView.setTag(listholder);
		}
		else {
			listholder = (ListHolder) convertView.getTag();
		}
		
		listholder.name.setText((CharSequence) info[1]);
		listholder.unit.setText((CharSequence) info[2]);
		Utils.setImage(listholder.image, (Integer) info[0], (String) info[4], 0, mApp, 120, 75);
		
		return convertView;
	}
}