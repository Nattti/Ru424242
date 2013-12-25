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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class Enterprise extends Activity {
	static final int IDD_BACK = 101;
	
	SQLiteDatabase mDB;
	DBHelper mHelper;
	SQLiteDatabase mDBService;

	TextView mTxtName;
	TextView mTxtUnit;
	TextView mTxtDate;
	TextView mTxtDesc;
	TextView mTxtCatalog;
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
	LinearLayout mLlSearchBox;
	
	MyAdapter mMyAdapter;
	MyListAdapter mMyListAdapter;
	MyExpListAdapter mExpAdapter;
	Map<ExpandableListView, Integer> mSizes;
	
	String mPhone;
	String mCurName;
	Integer mCurId;
	
	Map<String, Integer> mNameToId;
	LinearLayout mLlEntDtl;
	ExpandableListView mExpEnt;
	
	String mSectionName;
	boolean mIsSearchView;
	AutoCompleteTextView mActvSearch;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enterprise);
		
		mDB = SQLiteDatabase.openDatabase(Parameters.DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		mHelper = new DBHelper(this);
		mDBService = mHelper.getWritableDatabase();
		
		mTxtName = (TextView) findViewById(R.id.txtNameE);
		mTxtUnit = (TextView) findViewById(R.id.txtUnitE);
		mTxtDesc = (TextView) findViewById(R.id.txtDescE);
		mTxtDate = (TextView) findViewById(R.id.txtDateE);
		mTxtCatalog = (TextView) findViewById(R.id.txtCatalog);
		mTxtWorkTime = (TextView) findViewById(R.id.txtWorkTimeE);
		mIvAction = (ImageView) findViewById(R.id.ivActionE);
		mIvDetails = (ImageView) findViewById(R.id.ivDetailsE);
		mBtnBriefcase = (Button) findViewById(R.id.btnAddE);
		
		mExpDtlGro = new ArrayList<String>();
		mExpDtlChi = new ArrayList<ArrayList<Object[]>>();
		
		mLlEnts = (LinearLayout) findViewById(R.id.llEntsE);
		mLvEnts = (ListView) findViewById(R.id.lvEntsE);
		mExpEnt = (ExpandableListView) findViewById(R.id.expEnt);
		mSizes = new HashMap<ExpandableListView, Integer>();
		mLlDetails = (LinearLayout) findViewById(R.id.llDetailsE);
		mExpDetails = (ExpandableListView) findViewById(R.id.expDetailsE);
		mSvDetails = (ScrollView) findViewById(R.id.svDetailsE);
		mLlSearchBox = (LinearLayout) findViewById(R.id.llSearchBox);
		
		mLayers = new ArrayList<View>();
		mLayers.add(mLlSearchBox);
		mLayers.add(mLlEnts);
		mLayers.add(mLlDetails);
		mLayers.add(mSvDetails);
		
		Cursor cursor = mDB.query("sections", null, null, null, null, null, null);
		startManagingCursor(cursor);
		
		ArrayList<String> names = new ArrayList<String>();
		mNameToId = new HashMap<String, Integer>();
		while (cursor.moveToNext()) {
			mNameToId.put(cursor.getString(2), cursor.getInt(0));
			names.add(cursor.getString(2));
		}
		
		Object[] data = getFirstLevel();
		
		mIsSearchView = false;
		mMyAdapter = new MyAdapter((ArrayList<String>) data[0], (Map<String, ArrayList<Object[]>>) data[1], 1, mExpEnt);
		mExpEnt.setAdapter(mMyAdapter);
		mExpEnt.expandGroup(0);
		mExpEnt.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				processChildClick(((TextView)v).getText().toString());
				return false;
			}
		});
		mExpEnt.setOnGroupClickListener(new OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				if (mIsSearchView) {
					processChildClick(((TextView)v).getText().toString());
				}
				
				return false;
			}
		});
		
		// основной лист
		mEnts = new ArrayList<Object[]>();
		mMyListAdapter = new MyListAdapter(this, getApplication());
		mLvEnts.setAdapter(mMyListAdapter);
		mLvEnts.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				Object[] info = mEnts.get(position);
				mCurId = (Integer) info[0];
				mCurName = (String) info[1]; 
				mPhone = (String) info[3];
				mTxtWorkTime.setText((CharSequence) info[5]);
				mTxtName.setText(mCurName);
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
		
		// поиск
		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mActvSearch = (AutoCompleteTextView) findViewById(R.id.actvSearchE);
		mActvSearch.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View dialog, int keyCode, KeyEvent event) {
				String search = mActvSearch.getText().toString();
				
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					Object[] data; 
					if (search.length() > 2) {
						mIsSearchView = true;
						data = getSearchLevel(search);
					}
					else {
						mActvSearch.setText("");
						mIsSearchView = false;
						data = getFirstLevel();
					}
					
					imm.hideSoftInputFromWindow(mActvSearch.getWindowToken(), 0);
					
					mMyAdapter.setData(data);
					mMyAdapter.notifyDataSetChanged();
					
					return true;
				}
				
				return false;
			}
		});
		mActvSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				imm.showSoftInputFromInputMethod(mActvSearch.getWindowToken(), InputMethodManager.SHOW_FORCED);
			}
		});
		
		mActvSearch.setAdapter(new ArrayAdapter<String>(this, R.layout.dropdown, names));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		mDB.close();
		mDBService.close();
	}
	
	public Object[] getSearchLevel(String str) {
		ArrayList<String> parents = new ArrayList<String>();
		Map<String, ArrayList<Object[]>> childrens = new HashMap<String, ArrayList<Object[]>>();
		
		String search = "name LIKE '%" + str + "%'" + 
		  " OR name LIKE '%" + str.toUpperCase() + "%'" + 
		  " OR name LIKE '%" + str.toLowerCase() + "%'" + 
		  " OR name LIKE '%" + str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase() + "%'";
		
		Cursor cursor = mDB.query(DBHelper.TABLE_SECTIONS, new String[] {"name"}, search, null, null, null, null);
		while (cursor.moveToNext()) {
			String parent_name = cursor.getString(0);
			parents.add(parent_name);
			
			ArrayList<Object[]> children = new ArrayList<Object[]>();
			childrens.put(parent_name, children);
		}
		
		return new Object[] {parents, childrens};
	}
	
	public Object[] getFirstLevel() {
		Cursor curParent;
		Cursor curChildren;
		ArrayList<String> parents = new ArrayList<String>();
		Map<String, ArrayList<Object[]>> childrens = new HashMap<String, ArrayList<Object[]>>();
		
		curParent = mDB.query(DBHelper.TABLE_SECTIONS, null, "parent_id = 0", null, null, null, "id");
		
		while (curParent.moveToNext()) {
			int parent_id = curParent.getInt(0);
			String  parent_name = curParent.getString(2);
			
			curChildren = mDB.query(DBHelper.TABLE_SECTIONS, null, "parent_id = " + parent_id, null, null, null, "id");
			
			ArrayList<Object[]> children = new ArrayList<Object[]>();
			while (curChildren.moveToNext()) {
				children.add(new Object[] {curChildren.getString(2), curChildren.getInt(0)});
			}
			
			parents.add(parent_name);
			childrens.put(parent_name, children);
			
			curChildren.close();
		}
		
		curParent.close();
		
		return new Object[] {parents, childrens};
	}
	
	// адаптер для основного дерева организаций
	public class MyAdapter extends BaseExpandableListAdapter {
		ArrayList<String> mGroups;
		Map<String, ArrayList<Object[]>> mChildren;
		int mLevel;
		ExpandableListView mTopList;
	    
	    public MyAdapter(ArrayList<String> groups, Map<String, ArrayList<Object[]>> children, int level, ExpandableListView topList) {
			mGroups = groups;
			mChildren = children;
			mLevel = level;
			mTopList = topList;
		}

		public TextView getGenericView() {
			TextView textView = new TextView(Enterprise.this);
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		    textView.setPadding(mLevel == 1 ? 50 : 36*mLevel, 0, 0, 0);
		    textView.setMinHeight(50);
		    textView.setMaxHeight(50);
		    textView.setGravity(Gravity.CENTER_VERTICAL);
		    textView.setBackgroundColor(getResources().getColor(R.color.white));
		    textView.setTextColor(getResources().getColor(R.color.black));
		    return textView;
		}
		
		@SuppressWarnings("unchecked")
		public void setData(Object[] data) {
			mGroups = (ArrayList<String>) data[0];
			mChildren = (Map<String, ArrayList<Object[]>>) data[1];
		}
		  
		@Override
		public Object[] getChild(int groupPosition, int childPosition) {
			return mChildren.get(mGroups.get(groupPosition)).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			// проверим наличие детей данного элемента
			String name = (String) getChild(groupPosition, childPosition)[0];
			int id = (Integer) getChild(groupPosition, childPosition)[1];
			
			Cursor cursor = mDB.query("sections", null, "parent_id = " + id, null, null, null, null);
			startManagingCursor(cursor);
			
			boolean exp = true;
			if ((cursor.getCount() == 0) || (name == mGroups.get(groupPosition))) {
				exp = false;
			}
			
			if (!exp) {
				TextView textView = getGenericView();
				textView.setText(name);
				return textView;
			}
			else {
				MyExpListView expList = new MyExpListView(Enterprise.this);
				
				ArrayList<Object[]> children = new ArrayList<Object[]>();
				children.add(new Object[] {name, id});
				while (cursor.moveToNext()) {
					children.add(new Object[] {cursor.getString(2), cursor.getInt(0)});
				}
				ArrayList<String> parents = new ArrayList<String>();
				parents.add(name);
				
				Map<String, ArrayList<Object[]>> childrens = new HashMap<String, ArrayList<Object[]>>();
				childrens.put(name, children);
				
				expList.setRows(1);
				expList.setAdapter(new MyAdapter(parents, childrens, mLevel + 1, mTopList));
				mSizes.put(expList, children.size());
				expList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
					@Override
					public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
							long id) {
						if (mIsSearchView) {
							processChildClick(((TextView)v).getText().toString());
						}
						else {
							if (parent.isGroupExpanded(groupPosition))
								parent.collapseGroup(groupPosition);
							else
								parent.expandGroup(groupPosition);
							
							if (parent instanceof MyExpListView) {
								MyExpListView mev = (MyExpListView) parent;
								if (parent.isGroupExpanded(groupPosition))
									mev.setRows(mSizes.get(parent) + 1);
								else
									mev.setRows(1);
							}
							
							mTopList.requestLayout();
						}
						
						return true;
					}
				});
				expList.setOnChildClickListener(new OnChildClickListener() {
					
					@Override
					public boolean onChildClick(ExpandableListView parent, View v,
							int groupPosition, int childPosition, long id) {
						processChildClick(((TextView)v).getText().toString());
						return false;
					}
				});
				
				return expList;
			}
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int i = 0;
			try {
				i = mChildren.get(mGroups.get(groupPosition)).size();
			} catch (Exception e) {
			}
			return i;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return mGroups.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpand, View convertView,
				ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}
	}
	
	// обработка клика на организацию в дереве
	public void processChildClick(String name) {
		mSectionName = name;
		refreshEnterprises(name);
		mTxtCatalog.setText(name);
		
		Utils.setVisibility(mLlEnts, mLayers);
	}
	
	public void refreshEnterprises(String section) {
		Integer section_id = mNameToId.get(section);
		ArrayList<Integer> ids = new ArrayList<Integer>();
		ids.add(section_id);
		ids = getSubSection(section_id, ids);
		
		String where = "(";
		boolean isFirst = true;
		for (Integer id : ids) {
			if (!isFirst)
				where = where + ",";
			
			where = where + id;
			
			isFirst = false;
		}
		where = where + ")";
		
		mEnts.clear();
		
		Cursor cursor = mDB.rawQuery("SELECT DISTINCT companies_sections.section_id, companies.name, companies.unitname, " +
									 "companies.id, companies.phone, companies.logo, companies.flags, companies.worktime " +
									 "FROM companies_sections " +
									 "LEFT JOIN companies " +
									 "ON companies_sections.company_id = companies.id " +
									 "WHERE companies_sections.section_id in " + where + " AND companies.id IS NOT null " +
									 "ORDER BY companies.flags DESC", null);
		startManagingCursor(cursor);
		
		/*
		if (cursor.getCount() == 0)
			mEnts.add(new Object[] {"", "нет компаний в этой категории"});
		else {
			while (cursor.moveToNext())
				mEnts.add(new Object[] {cursor.getInt(3), cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), cursor.getString(7)}); // ид, имя, юнит, телефон, лого
		}
		*/

		ArrayList<Integer> index = new ArrayList<Integer>();
		ArrayList<Integer> index_temp = new ArrayList<Integer>();
		Map<Integer, Object[]> map = new HashMap<Integer, Object[]>();
		
		int flag = -1;
		while (cursor.moveToNext()) {
			if (flag != cursor.getInt(6)) {
				if (index_temp.size() != 0) {
					shuffleArray(index, index_temp);
					
					index_temp.clear();
				}
				
				flag = cursor.getInt(6);
			}
			
			int id = cursor.getInt(3);
			if (!index.contains(id) && !index_temp.contains(id)) {
				index_temp.add(id);
				map.put(id, new Object[] {cursor.getInt(3), cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(5), cursor.getString(7)}); // ид0, имя1, юнит3, телефон2, лого10
			}
		}
		
		cursor.close();
		
		shuffleArray(index, index_temp);
		
		for (Integer id : index)
			mEnts.add(map.get(id));
		
		if (mEnts.size() == 0)
			mEnts.add(new Object[] {-1, "нет компаний в этой категории", "", "", "", "", ""});
		
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
	
	public ArrayList<Integer> getSubSection(int section_id, ArrayList<Integer> ids) {
		Cursor cursor = mDB.query(DBHelper.TABLE_SECTIONS, null, "parent_id = " + section_id, null, null, null, null);
		startManagingCursor(cursor);
		
		while (cursor.moveToNext()) {
			int id = cursor.getInt(0);
			ids.add(id);
			ids = getSubSection(id, ids); 
		}
		
		return ids;
	}
	
	public void onClickCustom(View v) {
		switch (v.getId()) {
		case R.id.btnCallE:
			Intent intent = new Intent(Intent.ACTION_CALL);
			Uri phone = Uri.parse("tel:" + Utils.getFirstPhone(mPhone));
			intent.setData(phone);
			startActivity(intent);
			
			break;
		case R.id.btnAddE:
			String result = Utils.briefcase(mCurId, mCurName, mDBService, mBtnBriefcase); 
			if (result != "")
				Toast.makeText(this, result, Toast.LENGTH_LONG).show();
			
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
				Utils.setVisibility(mLlSearchBox, mLayers);

				return true;
			}
			else if (mExpEnt.getVisibility() == View.VISIBLE) {
				showDialog(IDD_BACK);
				
				return true;
			}
		}
		
	    return super.onKeyDown(keyCode, event);
	}
}
