package com.anddeveloper.ru424242;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Search extends BaseDrawerActivity {
	static final int IDD_BACK = 101;
	
	SQLiteDatabase mDB;
	SQLiteDatabase mDBService;
	
	AutoCompleteTextView mActvSearch;
	RadioGroup mRgSearch;
	InputMethodManager mImm;
	
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
	LinearLayout mLlEnts;
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
		setContentView(R.layout.search);

		mDB = SQLiteDatabase.openDatabase(Parameters.DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		DBHelper helper = new DBHelper(this);
		mDBService = helper.getWritableDatabase();
		
		mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mDB = SQLiteDatabase.openDatabase(Parameters.DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		
		mTxtName = (TextView) findViewById(R.id.txtNameS);
		mTxtUnit = (TextView) findViewById(R.id.txtUnitS);
		mTxtDesc = (TextView) findViewById(R.id.txtDescS);
		mTxtDate = (TextView) findViewById(R.id.txtDateS);
		mIvAction = (ImageView) findViewById(R.id.ivActionS);
		mTxtWorkTime = (TextView) findViewById(R.id.txtWorktimeS);
		mIvDetails = (ImageView) findViewById(R.id.ivDetailsS);
		mBtnBriefcase = (Button) findViewById(R.id.btnAddS);
		
		mExpDtlGro = new ArrayList<String>();
		mExpDtlChi = new ArrayList<ArrayList<Object[]>>();
		
		mRgSearch = (RadioGroup) findViewById(R.id.rgSearch);
		
		mLvEnts = (ListView) findViewById(R.id.lvEntsS);
		mLlEnts = (LinearLayout) findViewById(R.id.llEntsS);
		mLlDetails = (LinearLayout) findViewById(R.id.llDetailsS);
		mExpDetails = (ExpandableListView) findViewById(R.id.expDetailsS);
		mSvDetails = (ScrollView) findViewById(R.id.svDetailsS);
		
		mLayers = new ArrayList<View>();
		mLayers.add(mLlEnts);
		mLayers.add(mLlDetails);
		mLayers.add(mSvDetails);
		
		mActvSearch = (AutoCompleteTextView) findViewById(R.id.actvSearch);
		mActvSearch.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View dialog, int keyCode, KeyEvent event) {
				String search = mActvSearch.getText().toString();
				
				if (keyCode == KeyEvent.KEYCODE_ENTER && search.length() > 2) {
					mImm.hideSoftInputFromWindow(mActvSearch.getWindowToken(), 0);
					startSearch(search);
					
					return true;
				}
				
				return false;
			}
		});
		mActvSearch.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				mImm.hideSoftInputFromWindow(mActvSearch.getWindowToken(), 0);
				startSearch(mActvSearch.getText().toString());
			}
			
		});

		setAutocomplete(0);
		
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
				if (mCurId == - 1)
					return;
				
				mCurName = (String) info[1]; 
				mPhone = (String) info[3];
				mTxtWorkTime.setText((CharSequence) info[5]);
				mTxtName.setText((CharSequence) info[1]);
				mTxtUnit.setText((CharSequence) info[2]);
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
	protected void onDestroy() {
		super.onDestroy();
		
		mDB.close();
		mDBService.close();
	}
	
	public void setAutocomplete(int type) {
		ArrayList<String> names = new ArrayList<String>();
		
		if (type == 0) {
			Cursor cursor = mDB.query(true, DBHelper.TABLE_COMPANIES, new String[] {"name", "searchhints"}, null, null, null, null, null, null);
			
			while (cursor.moveToNext()) {
				names.add(cursor.getString(0)); // название
				names.add(cursor.getString(1)); // подсказка
			}
			
			cursor.close();
		}
		else {
			Cursor cursor = mDB.query(true, DBHelper.TABLE_SERVICES, new String[] {"name"}, null, null, null, null, null, null);
			
			while (cursor.moveToNext()) {
				names.add(cursor.getString(0)); // товар
			}
			
			cursor.close();
		}
		
		mActvSearch.setAdapter(new ArrayAdapter<String>(this, R.layout.dropdown, names));
	}

	public void startSearch(String search) {
		Cursor cursor;
		StringTokenizer st = new StringTokenizer(search, " ");
		
		boolean isFirst = true;
		String searchname = "";
		String searchhint = "";
		String servicesname = "";
		while (st.hasMoreTokens()) {
			String str = st.nextToken();
			
			searchname = searchname + (isFirst ? "" : " AND ") + "(name LIKE '%" + str + "%'" + 
									  " OR name LIKE '%" + str.toUpperCase() + "%'" + 
									  " OR name LIKE '%" + str.toLowerCase() + "%'" + 
									  " OR name LIKE '%" + str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase() + "%')";
			searchhint = searchhint + (isFirst ? "" : " AND ") + "(searchhints LIKE '%" + str + "%'" + 
									  " OR searchhints LIKE '%" + str.toUpperCase() + "%'" + 
									  " OR searchhints LIKE '%" + str.toLowerCase() + "%'" + 
									  " OR searchhints LIKE '%" + str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase() + "%')";
			servicesname = servicesname + (isFirst ? "" : " AND ") + "(services.name LIKE '%" + str + "%'" + 
									  " OR services.name LIKE '%" + str.toUpperCase() + "%'" + 
									  " OR services.name LIKE '%" + str.toLowerCase() + "%'" + 
									  " OR services.name LIKE '%" + str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase() + "%')";
			
			isFirst = false;
		}
		
		if (mRgSearch.getCheckedRadioButtonId() == R.id.rbEnterprise) {
			cursor = mDB.rawQuery("SELECT DISTINCT companies.id, companies.name, companies.flags, " +
								  "companies.unitname, companies.phone, companies.logo, companies.worktime " +
								  "FROM companies " +
								  "WHERE (" + searchname + ") OR (" + searchhint + ") " +
								  "ORDER BY companies.flags DESC", null);
		}
		else {
			cursor = mDB.rawQuery("SELECT DISTINCT companies.id, companies.name, companies.flags, " +
								  "companies.unitname, companies.phone, companies.logo, companies.worktime " +
								  "FROM services " +
							  	  "LEFT JOIN companies_services " +
								  "ON services.id = companies_services.service_id " +
								  "LEFT JOIN companies " +
								  "ON companies_services.company_id = companies.id " +
								  "WHERE " + servicesname + " AND companies.id IS NOT null " +
								  "ORDER BY companies.flags DESC", null);
		}
		
		mEnts.clear();
		ArrayList<Integer> index = new ArrayList<Integer>();
		ArrayList<Integer> index_temp = new ArrayList<Integer>();
		Map<Integer, Object[]> map = new HashMap<Integer, Object[]>();
		
		int flag = -1;
		while (cursor.moveToNext()) {
			if (flag != cursor.getInt(2)) {
				if (index_temp.size() != 0) {
					shuffleArray(index, index_temp);
					
					index_temp.clear();
				}
				
				flag = cursor.getInt(2);
			}
			
			int id = cursor.getInt(0);
			index_temp.add(id);
			map.put(id, new Object[]{cursor.getInt(0), cursor.getString(1), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6)}); // ид0, имя1, юнит3, телефон2, лого10
		}
		
		cursor.close();
		
		shuffleArray(index, index_temp);
		
		for (Integer id : index)
			mEnts.add(map.get(id));
		
		if (mEnts.size() == 0)
			mEnts.add(new Object[] {-1, "Ничего не найдено", "", "", "", ""});
		
		mMyListAdapter.setData(mEnts);
		mMyListAdapter.notifyDataSetChanged();
	}
	
	public void shuffleArray(ArrayList<Integer> index, ArrayList<Integer> temp) {
		Random rnd = new Random();
		
		while (temp.size() > 0) {
			int next = rnd.nextInt(temp.size());
			Integer find = temp.get(next); 
			index.add(find);
			temp.remove(find);
		}
	}
	
	public void onClickCustom(View v) {
		switch (v.getId()) {
		case R.id.btnCallS:
			if (mPhone != "") {
				Intent intent = new Intent(Intent.ACTION_CALL);
				Uri phone = Uri.parse("tel:" + Utils.getFirstPhone(mPhone));
				intent.setData(phone);
				startActivity(intent);
			}
			
			break;
		case R.id.btnAddS:
			String result = Utils.briefcase(mCurId, mCurName, mDBService, mBtnBriefcase); 
			if (result != "")
				Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			
			break;
		case R.id.rbEnterprise:
			setAutocomplete(0);
			
			break;
		case R.id.rbGoods:
			setAutocomplete(1);
			
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
				Utils.setVisibility(mLlEnts, mLayers);

				return true;
			}
			else if (mLlEnts.getVisibility() == View.VISIBLE) {
				showDialog(IDD_BACK);
				
				return true;
			}	
		}
		
	    return super.onKeyDown(keyCode, event);
	}
}
