package com.anddeveloper.ru424242;

import android.content.Context;
import android.widget.ExpandableListView;

public class MyExpListView extends ExpandableListView {
	private static final int HEIGHT = 50;
	int mRows;

	public MyExpListView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), mRows*HEIGHT); 
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	public void setRows(int rows) {
		mRows = rows;
	}
}
