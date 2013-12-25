package com.anddeveloper.ru424242;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Utils {
	
	// удаление или добавление в портфель
	public static String briefcase(Integer id, String name, SQLiteDatabase db, Button btn) {
		String result = "";
		
		if (inBriefcase(id, db)) {
			if (db.delete(DBHelper.TABLE_BRIEFCASE, "id = " + id, null) != 0) {
				result = "Предприятие '" + name + "' удалено из портфеля";
				btn.setText(R.string.strToBriefcase);
			}
		}
		else {
			ContentValues values = new ContentValues();
			values.put("id", id);
			
			if (db.insert(DBHelper.TABLE_BRIEFCASE, null, values) != -1) {
				result = "Предприятие '" + name + "' добавлено в портфель";
				btn.setText(R.string.strFromBriefcase);
			}
		}
		
		return result;
	}
	
	public static String getFirstPhone(String fullPhone) {
		String result = "";
		ArrayList<String> digits = new ArrayList<String>();
		digits.add("0");digits.add("1");digits.add("2");digits.add("3");digits.add("4");digits.add("5");digits.add("6");digits.add("7");digits.add("8");digits.add("9");
		
		String phone;
		StringTokenizer st = new StringTokenizer(fullPhone, ",");
		if (st.hasMoreTokens()) {
			phone = st.nextToken();
		}
		else {
			phone = fullPhone;
		}
		
		char[] chphone = phone.toCharArray();
		for (int i = 0; i < chphone.length; i++) {
			char ch = chphone[i];
			
			if (digits.contains(String.valueOf(ch))) {
				result = result + String.valueOf(ch);
			}
		}
		
		if (result.length() == 6) {
			result = "83496" + result; 
		}
		
		return result;
	}
	
	// проверка наличия в портфеле
	public static boolean inBriefcase(Integer id, SQLiteDatabase db) {
		boolean result = false;
		
		Cursor cursor = db.query(DBHelper.TABLE_BRIEFCASE, null, "id = " + id, null, null, null, null);
		if (cursor.getCount() != 0) {
			result = true;
		}
		
		cursor.close();
		
		return result;
	}
	
	// публикация сообщения в лог
	public static void genLog(String text) {
		Log.v("Ru424242", text == null ? "text is null" : text);
	} 
	
	// загрузка картинки по адресу
	public static Bitmap loadBitmap(String src) throws IOException {
	    Bitmap bitmap = null;
	    InputStream input = null;
	    
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        input = connection.getInputStream();
        bitmap = BitmapFactory.decodeStream(input);

	    return bitmap;
	}	
	
	// получение xml из потока
	public static Document getDocument(String url) throws MalformedURLException, IOException, Exception {
		URL documentUrl = new URL(url);
		URLConnection conn = documentUrl.openConnection();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document;
		InputStream stream = null;
		try {
			stream = conn.getInputStream();
			
			String strStream = Utils.stringFromIS(stream);
			strStream = strStream.replaceAll("&", "&amp;");
			
			document = builder.parse(new ByteArrayInputStream(strStream.getBytes()));
		} finally {
			if (stream != null) stream.close();
		}
		return document;
	}
	
	public static String stringFromIS(InputStream stream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(stream);
		ByteArrayBuffer bab = new ByteArrayBuffer(50);
		
		int current;
		while ((current = bis.read())!=-1) {
			bab.append((byte)current);
		}
		
		return new String(bab.toByteArray());
	}
	
	// дата последнего обновления
	public static String getDate() throws IOException {
		URL localUrl = new URL(Parameters.LAST_UPDATE);
		HttpURLConnection connecton = (HttpURLConnection) localUrl.openConnection();
		
		InputStream is = connecton.getInputStream();
		
		return Utils.stringFromIS(is);
	} 
	
	// проверка интернет-соединения
	public static boolean checkConnect(Application app) {
		ConnectivityManager cm = (ConnectivityManager)app.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		try {
			if (ni.isConnected()) {
				return true;
			}	
			else
			{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	// проверка скоростного соединения
	public static boolean checkSpeedConnect(Application app) {
		ConnectivityManager cm = (ConnectivityManager)app.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		try {
			if (ni.isConnected() && ni.getTypeName().equals("WIFI")) {
				return true;
			}	
			else
			{
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	// получение новостей и запись в базу
	public static void getNews(int id, int isnews, SQLiteDatabase db, Application app, String url) throws MalformedURLException, IOException, Exception {
		NodeList nodes;
		NodeList child;
		if (Utils.checkConnect(app)) {
			Document doc = Utils.getDocument(url);
			nodes = doc.getElementsByTagName("item");
			
			db.delete(DBHelper.TABLE_NEWS, "id = " + id + " and isnews = " + isnews, null);
			for (int i = 0; i < nodes.getLength(); i++) {
				child = nodes.item(i).getChildNodes();
				
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
				}
				
				values.put("id", id); // общие новости
				values.put("isnews", isnews);
				db.insert("news", null, values);
			}
		}
	}
	
	// установка видимости слоев
	public static void setVisibility(View v, ArrayList<View> layers) {
		for (View layer : layers) {
			if (layer == v)
				layer.setVisibility(View.VISIBLE);
			else
				layer.setVisibility(View.GONE);
		}
	}
	
	// установка картинки на виджет
	public static void setImage(ImageView image, int id, String logo, int mode, Application app, int width, int height) {
		image.setImageDrawable(app.getResources().getDrawable(R.drawable.nofoto86));
		
		if (logo.equals("") && (logo != null))
			return;
		
		String path = "";
		switch (mode) {
		case 0: // предприятие
			path = DBHelper.TEMP_CAT + "ent" + id + ".ru42"; // чтобы картинку не видел стандартный просмотрщик
			break;
		case 1: // новость
			path = DBHelper.TEMP_CAT + "new" + id + getFilename(logo) + ".ru42";
			break;
		case 2: // акция
			path = DBHelper.TEMP_CAT + "act" + id + getFilename(logo) + ".ru42";
			break;
		case 3: // афиша
			path = DBHelper.TEMP_CAT + "afi" + getFilename(logo) + ".ru42";
			break;
		default:
			break;
		}
			
		File file = new File(path);
		if (file.exists()) { // грузим картинку из кеша
			try {
				Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(file));
				
				if (width != 0 && height !=0 && bmp != null && bmp.getWidth() !=0 && bmp.getHeight() != 0) {
					float scale = Math.min((float) height/bmp.getHeight(), (float) width/bmp.getWidth());
					
					Matrix matrix = new Matrix();
					matrix.preScale(scale, scale);
					
					bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
				}
				
				image.setImageDrawable(new BitmapDrawable(bmp));
			} catch (FileNotFoundException e) {
				Utils.genLog("FileNotFoundException " + e.getLocalizedMessage());
			}
		}
		else {
			if (Utils.checkConnect(app) && (!logo.equals(""))) { // пытаемся загрузить картинку
				new AsynkDownload().execute(new Object[] {logo, image, path, width, height});
			}
		}
	}
	
	public static String getFilename(String path) {
		String filename = "";
		
		File file = new File(path);
		filename = file.getName();
		
		StringTokenizer str = new StringTokenizer(filename, ".");
		if (str.hasMoreTokens()) {
			filename = str.nextToken();
		}
		
		return filename;
	}
	
	// получение делатьной информации о компании
	public static Object[] refreshEnterpriseInfo(Integer id, SQLiteDatabase db, SQLiteDatabase db_service, Resources res, Application app) {
		ArrayList<String> gro = new ArrayList<String>();
		ArrayList<ArrayList<Object[]>> chi = new ArrayList<ArrayList<Object[]>>();
		
		Cursor cursor = db.query(DBHelper.TABLE_COMPANIES, null, "id = " + id, null, null, null, null);
		cursor.moveToNext();
		
		// контактная информация
		gro.add(res.getString(R.string.strContacts));
		
		ArrayList<Object[]> listCon = new ArrayList<Object[]>();
		
		String tel = cursor.getString(2);
		if (!tel.equals("")) {
			StringTokenizer st = new StringTokenizer(tel, ",");
			while (st.hasMoreTokens()) {
				listCon.add(new Object[] {st.nextToken(), "phone"});
			}
		}	
		
		String adr = cursor.getString(7);
		if (!adr.equals(""))
			listCon.add(new Object[] {adr});
		
		String eml = cursor.getString(8);
		if (!eml.equals(""))
			listCon.add(new Object[] {eml, "email"});
		
		String web = cursor.getString(5);
		if (!web.equals("")) {
			StringTokenizer st = new StringTokenizer(web, " ");
			while (st.hasMoreTokens()) {
				listCon.add(new Object[] {st.nextToken(), "web"});
			}
		}	
		
		if (listCon.size() == 0)
			listCon.add(new Object[] {"нет данных"});
		
		chi.add(listCon);
		
		// О компании
		gro.add(res.getString(R.string.strAbout));
		
		ArrayList<Object[]> listAbo = new ArrayList<Object[]>();
		
		String abo = cursor.getString(6);
		if (!abo.equals(""))
			listAbo.add(new Object[] {abo});
		
		if (listAbo.size() == 0)
			listAbo.add(new Object[] {"нет данных"});
		
		chi.add(listAbo);
		
		// Товары и услуги
		gro.add(res.getString(R.string.strGoods));
		
		Cursor cursor2 = db.rawQuery("SELECT DISTINCT services.name " +
									  "FROM companies_services " +
									  "LEFT JOIN services " +
									  "ON companies_services.service_id = services.id " +
									  "WHERE companies_services.company_id = " + id, null);
		
		ArrayList<Object[]> listGds = new ArrayList<Object[]>();
		
		if (cursor2.getCount() == 0)
			listGds.add(new Object[] {"нет данных"});
		else {
			while (cursor2.moveToNext())
				listGds.add(new Object[] {cursor2.getString(0)});
		}	
		
		chi.add(listGds);
		
		// новости и акции
		// обновление данных в базах
		boolean connection = Utils.checkConnect(app);
		
		if (connection) {
			try {
				Utils.getNews(id, 0, db_service, app, Parameters.COMPANY_ACTIONS + id); // акции
				Utils.getNews(id, 1, db_service, app, Parameters.COMPANY_NEWS + id); // новости
			} catch (MalformedURLException e) {
				Utils.genLog(e.getLocalizedMessage());
			} catch (IOException e) {
				Utils.genLog(e.getLocalizedMessage());
			} catch (Exception e) {
				Utils.genLog(e.getLocalizedMessage());
			}
		}
		
		for (int i = 1; i >= 0; i--) {
			ArrayList<Object[]> listNA = new ArrayList<Object[]>();
			
			Cursor cursor3 = db_service.query(DBHelper.TABLE_NEWS, null, "id = " + id + " and isnews = " + i, null, null, null, null);
			
			if (cursor3.getCount() == 0) {
				/*
				if (connection) 
					listNA.add(new Object[] {"нет данных"});
				else
					listNA.add(new Object[] {"нет данных (нет соединения)"});
				*/	
			}
			else {
				while (cursor3.moveToNext())
					listNA.add(new Object[] {cursor3.getString(2), cursor3.getString(3), cursor3.getString(4), 
							cursor3.getInt(1), cursor3.getString(5)}); // заголовок, описание, дата, ид компании, логотип
			}
			
			if (listNA.size() != 0) {
				if (i == 1)
					gro.add(res.getString(R.string.strNews));
				else
					gro.add(res.getString(R.string.strActions));
				
				chi.add(listNA);
			}
			
			cursor3.close();
		}
		
		cursor.close();
		cursor2.close();
		
		return new Object[] {gro, chi};
	}
	
}
