package com.anddeveloper.ru424242;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class News extends Activity implements OnItemClickListener {
	static final int IDD_BACK = 101;
	
	ArrayList<Object[]> mData;
	
	SQLiteDatabase mDB;
	
	TextView mTxtNewsDate;
	TextView mTxtNewsDescription;
	ImageView mIvNewsDetail;
	
	ListView mLvNews;
	ScrollView mSvNewsDetail;

	ArrayList<View> mLayers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news);
		
		mTxtNewsDate = (TextView) findViewById(R.id.txtNewsDate);
		mTxtNewsDescription = (TextView) findViewById(R.id.txtNewsDescription);
		mIvNewsDetail = (ImageView) findViewById(R.id.ivNewsDetail);
		
		mLvNews = (ListView) findViewById(R.id.lvNewsCity);
		mSvNewsDetail = (ScrollView) findViewById(R.id.svNewDetail);
		
		DBHelper helper = new DBHelper(this);
		mDB = helper.getWritableDatabase();
		
		Cursor cursor = mDB.query("news", null, "id = 99999 and isnews = 1", null, null, null, null);
		
		mData = new ArrayList<Object[]>();
		while (cursor.moveToNext()) {
			mData.add(new String[] {cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)});
		}
		
		cursor.close();
		
		MyCursorAdapter adapter = new MyCursorAdapter(this, mData);
		mLvNews.setAdapter(adapter);
		mLvNews.setOnItemClickListener(this);
		
		mLayers = new ArrayList<View>();
		mLayers.add(mLvNews);
		mLayers.add(mSvNewsDetail);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mDB.close();
	}

	public class MyCursorAdapter extends BaseAdapter {
		Context mContext;
		ArrayList<Object[]> mData;
		LayoutInflater mInflater;
		
		public MyCursorAdapter(Context context, ArrayList<Object[]> mData) {
			super();
			mContext = context;
			this.mData = mData;
			mInflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public String[] getItem(int position) {
			return (String[]) mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListHolder listholder;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.enterprise_dtl, null);
				
				listholder = new ListHolder();
				
				listholder.name = (TextView) convertView.findViewById(R.id.txtName);
				listholder.image = (ImageView) convertView.findViewById(R.id.ivImage);
				
				convertView.setTag(listholder);
			}
			else {
				listholder = (ListHolder) convertView.getTag();
			}
			
			Object[] info = getItem(position);
			listholder.name.setText((CharSequence) info[0]);
			if (!((String) info[1]).equals(""))
				Utils.setImage(listholder.image, 99999, (String) info[3], 1, getApplication(), 120, 75);
			
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Object[] info = mData.get(position);
		
		mTxtNewsDate.setText((CharSequence) info[2]);
		mTxtNewsDescription.setText((CharSequence) info[1]);
		
		if (Utils.checkConnect(getApplication()))
			Utils.setImage(mIvNewsDetail, 99999, (String) info[3], 1, getApplication(), 0, 0);
		
		mLvNews.setVisibility(View.GONE);
		mSvNewsDetail.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case IDD_BACK:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.strBack);
			builder.setPositiveButton(R.string.strYes, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			builder.setNegativeButton(R.string.strNo, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			
			builder.setCancelable(false);
			return builder.create();
		default:
			return null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mSvNewsDetail.getVisibility() == View.VISIBLE) {
				Utils.setVisibility(mLvNews, mLayers);
				
				return true;
			}
			else if (mLvNews.getVisibility() == View.VISIBLE) {
				showDialog(IDD_BACK);
				
				return true;
			}	
		}
		
	    return super.onKeyDown(keyCode, event);
	}
}
