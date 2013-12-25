package com.anddeveloper.ru424242;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;

public class Briefcase extends Activity {
	static final int IDD_BACK = 101;
	
	SQLiteDatabase mDB;
	SQLiteDatabase mDBService;
	
	TextView mTxtName;
	TextView mTxtUnit;
	TextView mTxtDesc;
	TextView mTxtDate;
	TextView mTxtWorkTime;
	ImageView mIvAction;
	ImageView mIvDetails;
	Button mBtnBriefcase;
	
	ArrayList<View> mLayers;
	ArrayList<Object[]> mEnts;
	ArrayList<String> mExpDtlGro;
	ArrayList<ArrayList<Object[]>> mExpDtlChi;
	
	ListView mLvEnts;
	LinearLayout mLlDetails;
	ExpandableListView mExpDetails;
	ScrollView mSvDetails;
	
	MyListAdapter mMyListAdapter;
	MyExpListAdapter mExpAdapter;
	
	String mPhone;
	String mCurName;
	Integer mCurId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.briefcase);
		
		mDB = SQLiteDatabase.openDatabase(Parameters.DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		DBHelper helper = new DBHelper(this);
		mDBService = helper.getWritableDatabase();
		
		mTxtName = (TextView) findViewById(R.id.txtNameB);
		mTxtUnit = (TextView) findViewById(R.id.txtUnitB);
		mTxtDesc = (TextView) findViewById(R.id.txtDescB);
		mTxtDate = (TextView) findViewById(R.id.txtDateB);
		mTxtWorkTime = (TextView) findViewById(R.id.txtWorkTimeB);
		mIvAction = (ImageView) findViewById(R.id.ivActionB);
		mIvDetails = (ImageView) findViewById(R.id.ivDetailsB);
		mBtnBriefcase = (Button) findViewById(R.id.btnRemoveB);
		
		mExpDtlGro = new ArrayList<String>();
		mExpDtlChi = new ArrayList<ArrayList<Object[]>>();
		
		mLvEnts = (ListView) findViewById(R.id.lvEntsB);
		mLlDetails = (LinearLayout) findViewById(R.id.llDetailsB);
		mExpDetails = (ExpandableListView) findViewById(R.id.expDetailsB);
		mSvDetails = (ScrollView) findViewById(R.id.svDetailsB);
		
		mLayers = new ArrayList<View>();
		mLayers.add(mLvEnts);
		mLayers.add(mLlDetails);
		mLayers.add(mSvDetails);
		
		// основной лист
		mEnts = new ArrayList<Object[]>();
		mMyListAdapter = new MyListAdapter(this, getApplication());
		mLvEnts.setAdapter(mMyListAdapter);
		mLvEnts.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Object[] info = mEnts.get(position);
				mCurId = (Integer) info[0];
				mCurName = (String) info[1]; 
				mPhone = (String) info[3];
				mTxtName.setText((CharSequence) info[1]);
				mTxtUnit.setText((CharSequence) info[2]);
				mTxtWorkTime.setText((CharSequence) info[5]);
				Utils.setImage(mIvDetails, mCurId, (String) info[4], 0, getApplication(), 0, 0);
				
				if (Utils.inBriefcase(mCurId, mDBService))
					mBtnBriefcase.setText(R.string.strFromBriefcase);
				else
					mBtnBriefcase.setText(R.string.strToBriefcase);
				
				Object[] details = Utils.refreshEnterpriseInfo(mCurId, mDB, mDBService, getResources(), getApplication());
				mExpDtlGro = (ArrayList<String>) details[0];
				mExpDtlChi = (ArrayList<ArrayList<Object[]>>) details[1];
				mExpAdapter.setData(mExpDtlGro, mExpDtlChi);
				mExpAdapter.notifyDataSetChanged();
				
				Utils.setVisibility(mLlDetails, mLayers);
			}
		});
		
		// лист детализации компании
		mExpAdapter = new MyExpListAdapter(this, mExpDtlGro, mExpDtlChi);
		mExpDetails.setAdapter(mExpAdapter);
		mExpDetails.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				Object[] info = mExpDtlChi.get(groupPosition).get(childPosition);
				if (info.length > 1) {
					if (info.length == 2) {
						if (info[1] == "email") {
							Intent i = new Intent(Intent.ACTION_SEND);
							i.setType("text/plain");
							i.putExtra(Intent.EXTRA_EMAIL  , new String[]{(String) info[0]});
							try {
							    startActivity(Intent.createChooser(i, "Send mail..."));
							} catch (android.content.ActivityNotFoundException ex) {
							}						}
						else if (info[1] == "web") {
							StringTokenizer st = new StringTokenizer((String) info[0], " ");
							String url = st.nextToken();
							if (!url.startsWith("http://") && !url.startsWith("https://"))
								url = "http://" + url;
							
							Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
							startActivity(myIntent);
						}
						else if (info[1] == "phone") {
							Intent intent = new Intent(Intent.ACTION_CALL);
							Uri phone = Uri.parse("tel:" + Utils.getFirstPhone((String) info[0]));
							intent.setData(phone);
							startActivity(intent);
						}
					}
					else {
						mTxtDesc.setText((CharSequence) info[1]);
						mTxtDate.setText((CharSequence) info[2]);
						Utils.setImage(mIvAction, (Integer) info[3], (String) info[4], mExpDtlGro.get(groupPosition).equals(getResources().getString(R.string.strNews)) ? 1 : 2, getApplication(), 0, 0);
						
						Utils.setVisibility(mSvDetails, mLayers);
					}	
				}	
				
				return false;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		refreshBriefcase();
		mMyListAdapter.setData(mEnts);
		mMyListAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mDB.close();
		mDBService.close();
	}
	
	public void refreshBriefcase() {
		mEnts.clear();
		
		Cursor cursor = mDBService.query(DBHelper.TABLE_BRIEFCASE, null, null, null, null, null, null);
		
		String where = "(";
		while (cursor.moveToNext()) {
			if (!cursor.isFirst())
				where = where + ",";
			
			where = where + cursor.getInt(1);
		}
		where = where + ")";
		
		cursor.close();
		
		Cursor cursor2 = mDB.rawQuery("SELECT * FROM companies where id in " + where, null);
		
		while (cursor2.moveToNext()) { // ид, имя, юнит, телефон, лого, время работы 
			mEnts.add(new Object[] {cursor2.getInt(0), cursor2.getString(1), cursor2.getString(3), cursor2.getString(2), cursor2.getString(10), cursor2.getString(9)});
		}
		
		cursor2.close();
	}
	
	public void onClickCustom(View v) {
		switch (v.getId()) {
		case R.id.btnCallB:
			Intent intent = new Intent(Intent.ACTION_CALL);
			Uri phone = Uri.parse("tel:" + Utils.getFirstPhone(mPhone));
			intent.setData(phone);
			startActivity(intent);
			
			break;
		case R.id.btnRemoveB:
			String result = Utils.briefcase(mCurId, mCurName, mDBService, mBtnBriefcase); 
			if (result != "")
				Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			
			refreshBriefcase();
			mMyListAdapter.setData(mEnts);
			mMyListAdapter.notifyDataSetChanged();
			
			Utils.setVisibility(mLvEnts, mLayers);
			
			break;
		default:
			break;
		}
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
			if (mSvDetails.getVisibility() == View.VISIBLE) {
				Utils.setVisibility(mLlDetails, mLayers);
				
				return true;
			}
			else if (mLlDetails.getVisibility() == View.VISIBLE) {
				Utils.setVisibility(mLvEnts, mLayers);

				return true;
			}
			else if (mLvEnts.getVisibility() == View.VISIBLE) {
				showDialog(IDD_BACK);
				
				return true;
			}	
		}
		
	    return super.onKeyDown(keyCode, event);
	}
}
