package com.anddeveloper.ru424242;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class MyExpListAdapter extends BaseExpandableListAdapter {
	Context mContext;
	ArrayList<String> mDataGro;
	ArrayList<ArrayList<Object[]>> mDataChi;
	
	public MyExpListAdapter(Context mContext, ArrayList<String> mDataGro,
			ArrayList<ArrayList<Object[]>> mDataChi) {
		super();
		this.mContext = mContext;
		this.mDataGro = mDataGro;
		this.mDataChi = mDataChi;
	}

	public void setData(ArrayList<String> dataGro, ArrayList<ArrayList<Object[]>> dataChi) {
		mDataGro = dataGro;
		mDataChi = dataChi;
	}
	
	public TextView getView() {
		TextView view = new TextView(mContext);
		view.setPadding(50, 0, 0, 0);
		view.setGravity(Gravity.CENTER_VERTICAL);
		view.setMinHeight(50);
		//view.setMaxHeight(40);
		view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		view.setTextColor(mContext.getResources().getColor(R.color.black));
		return view;
	}
	
	@Override
	public String getChild(int groupPosition, int childPosition) {
		return String.valueOf(mDataChi.get(groupPosition).get(childPosition)[0]);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = getView();
		}
		Object[] info = mDataChi.get(groupPosition).get(childPosition);
		if (info.length > 1) {
			((TextView)convertView).setTextColor(mContext.getResources().getColor(R.color.link));
		}
		else {
			((TextView)convertView).setTextColor(mContext.getResources().getColor(R.color.black));
		}
		((TextView)convertView).setText(getChild(groupPosition, childPosition));
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mDataChi.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mDataGro.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mDataGro.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		TextView view = getView();
		view.setText(mDataGro.get(groupPosition));
		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
