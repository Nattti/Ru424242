package com.anddeveloper.ru424242;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Calendar;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.TextView;

class AsynkNews extends AsyncTask<Object, Void, Void> {
	
	@Override
	protected Void doInBackground(Object... params) {
		try {
			Utils.getNews((Integer) params[0], (Integer) params[1], (SQLiteDatabase) params[2], (Application) params[3], (String) params[4]);
		} catch (MalformedURLException e) {
			Utils.genLog("AsynkNews MalformedURLException " + e.getLocalizedMessage());
		} catch (IOException e) {
			Utils.genLog("AsynkNews IOException " + e.getLocalizedMessage());
		} catch (Exception e) {
			Utils.genLog("AsynkNews Exception " + e.getLocalizedMessage());
		}
		
		return null;
	}
	
	// получение данных о погоде и курсах
	public void showWeatherRates(Application app, SQLiteDatabase db, 
			TextView weather, TextView usd, TextView eur) throws MalformedURLException, IOException, Exception {
		NodeList nodes;
		Document doc = Utils.getDocument(Parameters.WEATHER_RATES);
		
		db.delete(DBHelper.TABLE_RW, null, null);
		ContentValues values = new ContentValues();
		
		nodes = doc.getElementsByTagName("weather");
		for (int i = 0; i < nodes.getLength(); i++) {
			NodeList child = nodes.item(i).getChildNodes();
			for (int j = 0; j < child.getLength(); j++) {
				String name = child.item(j).getNodeName(); 
				
				if ("description".equals(name)) {
					String value = child.item(j).getFirstChild().getNodeValue(); 
						
					weather.setText(value);
					
					values.put("weather", value);
				}
				else if ("image".equals(name)) {
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
					
					usd.setText(value);

					values.put("usd", value);
				}
				else if ("eur".equals(name)) {
					String value = child.item(j).getFirstChild().getNodeValue(); 

					eur.setText(value);

					values.put("eur", value);
				}
			}
		}
		
		Calendar calendar = Calendar.getInstance();
		String date = calendar.getTime().toString();
		
		values.put("date", date);
		
		db.insert(DBHelper.TABLE_RW, null, values);
	}
}
