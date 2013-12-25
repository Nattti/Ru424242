package com.anddeveloper.ru424242;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.util.ByteArrayBuffer;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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

public class Primary extends BaseDrawerActivity {
	static final int IDD_DOWN = 101;
	static final int IDD_REFRESH = 102;
	static final int IDD_BACK = 103;
	
	TextView mTxtTextView1;
	ProgressDialog mDialog;
	
	DBHelper mHelper;
	SQLiteDatabase mDB;
	SQLiteDatabase mDBService;
	
	SharedPreferences mPref;
	SharedPreferences.Editor mEditor;
	Long mLastDate;
	String mNewDate;
	
	TextView mTxtDate;
	TextView mTxtUSD;
	TextView mTxtEUR;
	TextView mTxtWeather;
	ImageView mIvWeather;
	
	TextView mTxtDesc;
	TextView mTxtDateD;
	ImageView mIvAction;
	
	MyApp mMyApp;
	
	ArrayList<View> mLayers;
	
	ListView mLvAfisha;
	ListView mLvAfishaDet;
	ScrollView mSvAfishaDet;
	
	ArrayList<Object[]> mData;
	ArrayList<Object[]> mDataDet;
	MyCursorAdapter mAdapter;
	MyCursorAdapter mAdapterDet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.primary);
		
		mTxtDate = (TextView) findViewById(R.id.txtDate);
		mTxtUSD = (TextView) findViewById(R.id.txtUSD);
		mTxtEUR = (TextView) findViewById(R.id.txtEUR);
		mTxtWeather = (TextView) findViewById(R.id.txtWeatherWal);
		mIvWeather = (ImageView) findViewById(R.id.ivWeather);
		
		mTxtTextView1 = (TextView) findViewById(R.id.textView1);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		mEditor = mPref.edit();
		mMyApp = (MyApp) getApplicationContext(); 
		
		// загрузка базы
		// проверка наличия базы
		File base = new File(Parameters.DB_PATH);
		if (base.exists()) {
			try {
				if (!mMyApp.getLoad()) {
					// база существует, обновляем на быстром соединении
					if (Utils.checkSpeedConnect(getApplication())) {
						if (checkDate()) {
							showDialog(IDD_DOWN);
						}
					}
				}
			} catch (IOException e) {
				Utils.genLog("Base IOException " + e.getLocalizedMessage());
			} catch (ParseException e) {
				Utils.genLog("Base ParseException " + e.getLocalizedMessage());
			}
		}
		else {
			// базы нет, нужно развернуть из ассетов
			try {
				getDataBaseFromAssets(Parameters.DB_PATH_ASSET, Parameters.DB_PATH);
			} catch (IOException e) {
				Utils.genLog(e.getLocalizedMessage());
				finish();
				return;
			} 
		}
		
		// определение адаптеров для афиши
		mLvAfisha = (ListView) findViewById(R.id.lvAfisha);
		mLvAfishaDet = (ListView) findViewById(R.id.lvAfishaDet);
		mSvAfishaDet = (ScrollView) findViewById(R.id.svAfishaDet);
		
		mTxtDesc = (TextView) findViewById(R.id.txtDescA);
		mTxtDateD = (TextView) findViewById(R.id.txtDateA);
		mIvAction = (ImageView) findViewById(R.id.ivActionA);
		
		mLayers = new ArrayList<View>();
		mLayers.add(mLvAfisha);
		mLayers.add(mLvAfishaDet);
		mLayers.add(mSvAfishaDet);
		
		mData = new ArrayList<Object[]>();
		mDataDet = new ArrayList<Object[]>();
		mAdapter = new MyCursorAdapter(this, mData);
		mLvAfisha.setAdapter(mAdapter);
		mLvAfisha.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos,
					long arg3) {
				mDataDet.clear();
				
				if (mData.get(pos)[0] == null)
					return;
				
				Cursor cursor = mDBService.query(DBHelper.TABLE_AFISHA, null, "source = '" + mData.get(pos)[0].toString() + "'", null, null, null, null);
				startManagingCursor(cursor);
				
				while (cursor.moveToNext()) {
					mDataDet.add(new Object[] {cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)});
				}
				
				mAdapterDet.setData(mDataDet);
				mAdapterDet.notifyDataSetChanged();
				
				Utils.setVisibility(mLvAfishaDet, mLayers);
			}
		});
		
		mAdapterDet = new MyCursorAdapter(this, mDataDet);
		mAdapterDet.setData(mDataDet);
		mLvAfishaDet.setAdapter(mAdapterDet);
		mLvAfishaDet.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos,
					long arg3) {
				Object[] info = mDataDet.get(pos);
				
				mTxtDesc.setText((CharSequence) info[1]);
				mTxtDateD.setText((CharSequence) info[2]);
				Utils.setImage(mIvAction, 0, (String) info[3], 3, getApplication(), 0, 0);
				
				Utils.setVisibility(mSvAfishaDet, mLayers);
			}
		});
		
		mDB = SQLiteDatabase.openDatabase(Parameters.DB_PATH, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		mHelper = new DBHelper(this);
		mDBService = mHelper.getWritableDatabase();
		
		refreshNewsWeather();
	}
	
	public boolean checkDate() throws IOException, ParseException {
		boolean result = false;
		
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        
		Long curDate = mPref.getLong("date", 0);
		
		mNewDate = Utils.getDate();
        Date newDateD = format.parse(mNewDate);
        mLastDate = newDateD.getTime(); 
        
        if (mLastDate > curDate) {
        	result = true;
		}
		
		return result;
	}
	
	public void getDataBaseFromAssets(String path, String filepath) throws IOException {
		InputStream myInput = getAssets().open(path);
		
		// создание каталога, если его нет
    	File dir = new File(Parameters.DB_DIR);
    	if (!dir.exists()) {
			dir.mkdirs();
		}

    	File file = new File(filepath);
    	
    	// удаление существующего файла, на всякий случай
    	if (file.exists())
    		file.delete();
    	
	    OutputStream myOutput = new FileOutputStream(file);
        
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = myInput.read(buffer)) > 0) {
	    	myOutput.write(buffer, 0, length);
	    }
	    
	    myOutput.flush();
	    myOutput.close();

	    myInput.close();
	}
	
	// обновление новостей
	public void refreshNewsWeather() {
		try {
			if (Utils.checkConnect(getApplication())) {
				Handler handler = new Handler();
				handler.post(weather);
				handler.post(afisha);
				
				new AsynkNews().execute(new Object[] {99999, 1, mDBService, getApplication(), Parameters.CITY_NEWS});
			}
			else {
				getRatesWeatherFromDB();
				getAfishaFromDB();
			}	
				
		} catch (Exception e) {
			Utils.genLog("Weather Exception " + e.getLocalizedMessage());
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		Cursor cursor = mDBService.query(DBHelper.TABLE_NEWS, null, "id = 99999", null, null, null, null);
		startManagingCursor(cursor);
		if (cursor.getCount() == 0) {
			refreshNewsWeather();
		}
	}

	Runnable weather = new Runnable() {
		@Override
		public void run() {
			try {
				showWeatherRates();
			} catch (MalformedURLException e) {
				Utils.genLog("Weather MalformedURLException " + e.getLocalizedMessage());
			} catch (IOException e) {
				Utils.genLog("Weather IOException " + e.getLocalizedMessage());
			} catch (Exception e) {
				Utils.genLog("Weather Exception " + e.getLocalizedMessage());
			}
		}
	};
	
	Runnable afisha = new Runnable() {
		@Override
		public void run() {
			try {
				showAfisha();
			} catch (MalformedURLException e) {
				Utils.genLog("Afisha MalformedURLException " + e.getLocalizedMessage());
			} catch (IOException e) {
				Utils.genLog("Afisha IOException " + e.getLocalizedMessage());
			} catch (Exception e) {
				Utils.genLog("Afisha Exception " + e.getLocalizedMessage());
			}
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mDB != null)
			mDB.close();
		
		if (mDBService != null)
			mDBService.close();
	}
	
	// поток парсинга базы данных
	@SuppressWarnings("unused")
	private class DownloadTh extends Thread {
		Handler mHandler;
		Bundle mBundle;
		Message mMessage;
		String jsontext;
		
		public DownloadTh(Handler handler) {
			mHandler = handler;
		}

		@Override
		public void run() {
            try {
				sendText("получение данных...");
				
				// получение пустой базы из ассетов
            	getDataBaseFromAssets(Parameters.DB_PATH_ASSET_BLANC, Parameters.DB_PATH_BLANK);
        		SQLiteDatabase db_temp = SQLiteDatabase.openDatabase(Parameters.DB_PATH_BLANK, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        		
        		// получение файла обновления
                URL updateURL = new URL(Parameters.REFRESH_BASE);
                URLConnection conn = updateURL.openConnection();
                InputStream is = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);
                
                int lenght = is.available();
                ByteArrayBuffer baf = new ByteArrayBuffer(lenght);
 
                int current = 0;
                while((current = bis.read()) != -1)
                    baf.append((byte)current);
                
                JsonFactory factory = new JsonFactory();
                JsonParser parser = factory.createJsonParser(baf.toByteArray());
                
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                	String fieldname = parser.getCurrentName();
                	
                	if ("section".equals(fieldname)) { // секции
    					sendText("обновление разделов...");
                		
                		parser.nextToken();
                		
                		db_temp.execSQL("delete from sections");
                		db_temp.beginTransaction();
                		
                		boolean has_finish = false;
                		ContentValues values = new ContentValues();
                		
						while (parser.nextToken() != JsonToken.END_ARRAY) {
							String namefield = parser.getCurrentName();
							
							if ("id".equals(namefield)) {
								parser.nextToken();
								
								values.clear();
								values.put("id", parser.getIntValue());
							}
							else if ("parent_id".equals(namefield)) {
								parser.nextToken();

								values.put("parent_id", parser.getIntValue());
								has_finish = true;
							}
							else if ("name".equals(namefield)) {
								parser.nextToken();
								
								values.put("name", parser.getText());
							}
							
							if (has_finish) {
								has_finish = false;
		                		db_temp.insert("sections", null, values);
		                		
		                		continue;
							}
						}
						
						try {
							db_temp.setTransactionSuccessful();
						} 
						finally {
							db_temp.endTransaction();
						}
					}
	                else if ("services".equals(fieldname)) { // сервисы
    					sendText("обновление товаров...");
                		
	                	parser.nextToken();
	                	
                		db_temp.execSQL("delete from services");
                		db_temp.beginTransaction();
                		
                		boolean has_finish = false;
                		ContentValues values = new ContentValues();
                		
						while (parser.nextToken() != JsonToken.END_ARRAY) {
							String namefield = parser.getCurrentName();
							
							if ("id".equals(namefield)) {
								parser.nextToken();
								
								values.clear();
								values.put("id", parser.getIntValue());
							}
							else if ("name".equals(namefield)) {
								parser.nextToken();
								
								values.put("name", parser.getText());
								has_finish = true;
							}
							
							if (has_finish) {
								has_finish = false;
		                		db_temp.insert("services", null, values);
		                		
		                		continue;
							}
						}
						
						try {
							db_temp.setTransactionSuccessful();
						} 
						finally {
							db_temp.endTransaction();
						}
	                }
	                else if ("companies_services".equals(fieldname)) { // связка компаний и сервисов
    					sendText("обновление товаров компаний...");
                		
	                	parser.nextToken();
	                	
                		db_temp.execSQL("delete from companies_services");
                		db_temp.beginTransaction();
                		
                		boolean has_finish = false;
                		ContentValues values = new ContentValues();
                		
						while (parser.nextToken() != JsonToken.END_ARRAY) {
							String namefield = parser.getCurrentName();
							
							if ("company_id".equals(namefield)) {
								parser.nextToken();
								
								values.clear();
								values.put("company_id", parser.getIntValue());
							}
							else if ("service_id".equals(namefield)) {
								parser.nextToken();
								
								values.put("service_id", parser.getIntValue());
								has_finish = true;
							}
							
							if (has_finish) {
								has_finish = false;
		                		db_temp.insert("companies_services", null, values);
		                		
		                		continue;
							}
						}
						
						try {
							db_temp.setTransactionSuccessful();
						} 
						finally {
							db_temp.endTransaction();
						}
					}
	                else if ("companies_sections".equals(fieldname)) { // связка компаний и секций
    					sendText("обновление разделов компаний...");
                		
	                	parser.nextToken();
	                	
                		db_temp.execSQL("delete from companies_sections");
                		db_temp.beginTransaction();
                		
                		boolean has_finish = false;
                		ContentValues values = new ContentValues();
                		
						while (parser.nextToken() != JsonToken.END_ARRAY) {
							String namefield = parser.getCurrentName();
							
							if ("company_id".equals(namefield)) {
								parser.nextToken();
								
								values.clear();
								values.put("company_id", parser.getIntValue());
							}
							else if ("section_id".equals(namefield)) {
								parser.nextToken();
								
								values.put("section_id", parser.getIntValue());
								has_finish = true;
							}
							
							if (has_finish) {
								has_finish = false;
		                		db_temp.insert("companies_sections", null, values);
		                		
		                		continue;
							}
						}
						
						try {
							db_temp.setTransactionSuccessful();
						} 
						finally {
							db_temp.endTransaction();
						}
					}
	                else if ("companies".equals(fieldname)) { // информация о компаниях
    					sendText("обновление компаний...");
                		
	                	parser.nextToken();
    					
                		db_temp.execSQL("delete from companies");
                		db_temp.beginTransaction();
                		
                		boolean has_finish = false;
                		ContentValues values = new ContentValues();
                		
						while (parser.nextToken() != JsonToken.END_ARRAY) {
							String namefield = parser.getCurrentName();
							
							if ("id".equals(namefield)) {
								parser.nextToken();
								
								values.clear();
								values.put("id", parser.getIntValue());
							}
							else if ("name".equals(namefield)) { // название
								parser.nextToken();
								
								values.put("name", parser.getText());
							}
							else if ("phone".equals(namefield)) { // телефон
								parser.nextToken();
								
								values.put("phone", parser.getText());
							}
							else if ("unitname".equals(namefield)) { // 
								parser.nextToken();
								
								values.put("unitname", parser.getText());
							}
							else if ("searchhints".equals(namefield)) { // тэги
								parser.nextToken();
								
								values.put("searchhints", parser.getText());
							}
							else if ("web".equals(namefield)) { // сайт
								parser.nextToken();
								
								values.put("web", parser.getText());
							}
							else if ("description".equals(namefield)) { // описание
								parser.nextToken();
								
								values.put("description", parser.getText());
							}
							else if ("address".equals(namefield)) { // адрес
								parser.nextToken();
								
								values.put("address", parser.getText());
							}
							else if ("email".equals(namefield)) { // электронная почта
								parser.nextToken();
								
								values.put("email", parser.getText());
							}
							else if ("worktime".equals(namefield)) { // рабочее время
								parser.nextToken();
								
								values.put("worktime", parser.getText());
							}
							else if ("logo_url".equals(namefield)) { // путь до логотипа
								parser.nextToken();
								
								values.put("logo", parser.getText());
							}
							else if ("flags".equals(namefield)) {
								parser.nextToken();
								
								values.put("flags", parser.getIntValue());
								has_finish = true;
							}
							
							if (has_finish) {
								has_finish = false;
		                		db_temp.insert("companies", null, values);
		                		
		                		continue;
							}
						}
						
						try {
							db_temp.setTransactionSuccessful();
						} 
						finally {
							db_temp.endTransaction();
						}
					}
				}
                
			    File inputFile = new File(Parameters.DB_PATH_BLANK);
			    File outputFile = new File(Parameters.DB_PATH);

			    FileInputStream fis  = new FileInputStream(inputFile);
			    FileOutputStream fos = new FileOutputStream(outputFile);

			    byte[] buf = new byte[1024];
		        int i = 0;
		        while ((i = fis.read(buf)) != -1) {
		            fos.write(buf, 0, i);
		        }
		        
		        if (fis != null) 
		        	fis.close();
		        
		        if (fos != null) 
		        	fos.close();
		        
		        mEditor.putLong("date", mLastDate);
		        mEditor.commit();
			    
            } catch (JsonGenerationException e) {
    			Utils.genLog("Update JsonGenerationException " + e.getLocalizedMessage());
            } catch (JsonMappingException e) {
    			Utils.genLog("Update JsonMappingException " + e.getLocalizedMessage());
            } catch (Exception e) {
    			Utils.genLog("Update Exception " + e.getLocalizedMessage());
            } finally {
                dismissDialog(IDD_REFRESH);
            }
		}
		
		public void sendText(String text) {
            mBundle = new Bundle();
            mBundle.putString("field", text);
            mMessage = mHandler.obtainMessage();
            mMessage.setData(mBundle);
            mHandler.sendMessage(mMessage);
		} 
	}
	
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			/*
			int max = msg.getData().getInt("max", -1);
			if (max != -1)
				mDialog.setMax(max);
			
			int count = msg.getData().getInt("count", -1);
			if (count != -1)
				mDialog.setProgress(count);
			*/
			String field = msg.getData().getString("field");
			if (field != "") {
				//mTxtTextView1.setText(field);
				mDialog.setTitle(field);
				mDialog.setMessage(field);
			}
			
		}
	};

	// получение данных о погоде и курсах
	public void showWeatherRates() throws MalformedURLException, IOException, Exception {
		if (Utils.checkConnect(getApplication())) {
			NodeList nodes;
			Document doc = Utils.getDocument(Parameters.WEATHER_RATES);
			
			mDBService.delete(DBHelper.TABLE_RW, null, null);
			ContentValues values = new ContentValues();
			
			nodes = doc.getElementsByTagName("weather");
			for (int i = 0; i < nodes.getLength(); i++) {
				NodeList child = nodes.item(i).getChildNodes();
				for (int j = 0; j < child.getLength(); j++) {
					String name = child.item(j).getNodeName(); 
					
					if ("description".equals(name)) {
						String value = child.item(j).getFirstChild().getNodeValue(); 
							
						mTxtWeather.setText(value);
						
						values.put("weather", value);
					}
					else if ("image".equals(name)) {
						String value = child.item(j).getFirstChild().getNodeValue(); 
						
						Utils.setImage(mIvWeather, 88888, value, 0, getApplication(), 0, 0);

						values.put("logo", value);
					}
				}
			}

			nodes = doc.getElementsByTagName("rate");
			for (int i = 0; i < nodes.getLength(); i++) {
				NodeList child = nodes.item(i).getChildNodes();
				for (int j = 0; j < child.getLength(); j++) {
					Node node = child.item(j);
					String name = node.getNodeName(); 
					
					if ("usd".equals(name)) {
						String value = child.item(j).getFirstChild().getNodeValue();
						
						mTxtUSD.setText(value);

						values.put("usd", value);
					}
					else if ("eur".equals(name)) {
						String value = child.item(j).getFirstChild().getNodeValue(); 

						mTxtEUR.setText(value);

						values.put("eur", value);
					}
				}
			}
			
			Calendar calendar = Calendar.getInstance();
			String date = calendar.getTime().toString();
			
			mTxtDate.setText(date);
			values.put("date", date);
			
			mDBService.insert(DBHelper.TABLE_RW, null, values);
		}
	}
	
	public void showAfisha() throws MalformedURLException, IOException, Exception {
		NodeList nodes;
		Document doc = Utils.getDocument(Parameters.AFISHA);
		
		mDBService.delete(DBHelper.TABLE_AFISHA, null, null);
		
		nodes = doc.getElementsByTagName("item");
		for (int i = 0; i < nodes.getLength(); i++) {
			NodeList child = nodes.item(i).getChildNodes();
			
			ContentValues values = new ContentValues();
			for (int j = 0; j < child.getLength(); j++) {
				Node node = child.item(j);
				String name = node.getNodeName();
				
				if ("description".equals(name))
					values.put("description", node.getFirstChild().getNodeValue());
				else if ("title".equals(name))
					values.put("title", node.getFirstChild().getNodeValue());
				else if ("pubDate".equals(name))
					values.put("date", node.getFirstChild().getNodeValue());
				else if ("image".equals(name))
					values.put("image", node.getFirstChild().getNodeValue());
				else if ("source".equals(name)) {
					if (node.hasChildNodes()) {
						values.put("source", node.getFirstChild().getNodeValue());
					}
				}	
			}	
			mDBService.insert(DBHelper.TABLE_AFISHA, null, values);
		}
		getAfishaFromDB();
	}
	
	// получение курсов и погоды без соединения
	public void getRatesWeatherFromDB() {
		Cursor cursor = mDBService.query(DBHelper.TABLE_RW, null, null, null, null, null, null);
		startManagingCursor(cursor);
		
		if (cursor.moveToNext()) {
			mTxtDate.setText(cursor.getString(1));
			mTxtUSD.setText(cursor.getString(2));
			mTxtEUR.setText(cursor.getString(3));
			mTxtWeather.setText(cursor.getString(4));
			Utils.setImage(mIvWeather, 88888, cursor.getString(5), 0, getApplication(), 0, 0);
		}
	}
	
	public void getAfishaFromDB() {
		mData.clear();
		Cursor cursor = mDBService.query(true, DBHelper.TABLE_AFISHA, new String[] {"source"}, null, null, null, null, null, null);
		startManagingCursor(cursor);
		
		while (cursor.moveToNext()) {
			mData.add(new Object[] {cursor.getString(0), ""});
		}
		
		mAdapter.setData(mData);
		mAdapter.notifyDataSetChanged();
	}
	
	public class MyCursorAdapter extends BaseAdapter {
		Context mContext;
		ArrayList<Object[]> mData;
		LayoutInflater mInflater;
		
		public MyCursorAdapter(Context context, ArrayList<Object[]> mData) {
			mContext = context;
			this.mData = mData;
			mInflater = getLayoutInflater();
		}
		
		public void setData(ArrayList<Object[]> data) {
			mData = data;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object[] getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListHolder listholder;
			
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.primary_dtl, null);
				//convertView = mInflater.inflate(R.layout.enterprise_dtl, null);
				
				listholder = new ListHolder();
				
				listholder.name = (TextView) convertView.findViewById(R.id.txtNameAfisha);
				
				/*
				listholder.name = (TextView) convertView.findViewById(R.id.txtName);
				listholder.image = (ImageView) convertView.findViewById(R.id.ivImage);
				*/
				
				convertView.setTag(listholder);
			}
			else {
				listholder = (ListHolder) convertView.getTag();
			}
			
			Object[] info = getItem(position);
			
			listholder.name.setText((CharSequence) info[0]);
			
			/*
			if (!((String) info[1]).equals(""))
				Utils.setImage(listholder.image, 0, (String) info[3], 3, getApplication(), 120, 75);
			else
				listholder.image.setVisibility(View.GONE);
			
			convertView.requestLayout();
			*/
			
			return convertView;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case IDD_REFRESH:
			mDialog = new ProgressDialog(this);
			mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			return mDialog;
		case IDD_BACK:
			AlertDialog.Builder builder0 = new AlertDialog.Builder(this);
			builder0.setMessage(R.string.strBack);
			builder0.setPositiveButton(R.string.strYes, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			builder0.setNegativeButton(R.string.strNo, 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			
			builder0.setCancelable(false);
			return builder0.create();
		case IDD_DOWN:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Загрузить обновление от " + mNewDate + "?");
			builder.setPositiveButton("Да", new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					showDialog(IDD_REFRESH);
					
					DownloadTh downloadTh = new DownloadTh(handler);
					downloadTh.start();
					
					mMyApp.setLoad(true);
				}
			});
			builder.setNegativeButton("Нет", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

					mMyApp.setLoad(true);
				}
			});
			
			builder.setCancelable(true);
			return builder.create();
		default:
			return null;
		}
	}
	
	public void onClickCustom(View v) {
		Main parent;
		parent = (Main) this.getParent();
		
		switch (v.getId()) {
		case R.id.btnRefresh:
			showDialog(IDD_REFRESH);
			
			DownloadTh downloadTh = new DownloadTh(handler);
			downloadTh.start();
			
			break;
		case R.id.btnNews:
			//parent.swithTab(3);
			String url = "http://talkdriver.ru/support.php?sid=1452&crc=b7ed35e3&nf&ni";
			if (!url.startsWith("http://") && !url.startsWith("https://"))
				url = "http://" + url;
			
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(myIntent);
			
			break;
		case R.id.btnBriefCase:
			parent.swithTab(4);
			
			break;
		case R.id.btnGiveCall:
			Intent intent = new Intent(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:8(3496)424242"));
			startActivity(intent);
			
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mSvAfishaDet.getVisibility() == View.VISIBLE) {
				Utils.setVisibility(mLvAfishaDet, mLayers);
				
				return true;
			}
			else if (mLvAfishaDet.getVisibility() == View.VISIBLE) {
				Utils.setVisibility(mLvAfisha, mLayers);

				return true;
			}
			else if (mLvAfisha.getVisibility() == View.VISIBLE) {
				showDialog(IDD_BACK);
				
				return true;
			}	
		}
		
	    return super.onKeyDown(keyCode, event);
	}
}
