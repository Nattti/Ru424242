package com.anddeveloper.ru424242;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	public static final String TEMP_CAT = "/sdcard/ru424242/";
	public static final String TABLE_RW = "rates_weather";
	public static final String TABLE_NEWS = "news";
	public static final String TABLE_AFISHA = "afisha";
	public static final String TABLE_BRIEFCASE = "briefcase";
	public static final String TABLE_COMPANIES = "companies";
	public static final String TABLE_SECTIONS = "sections";
	public static final String TABLE_SERVICES = "services";
	public static final String TABLE_COMPANIES_SECTIONS = "companies_sections";
	public static final String TABLE_COMPANIES_SERVICES = "companies_services";
	public static final String DB_SERVICE = "service.db";
	
	public DBHelper(Context context) {
		super(context, DB_SERVICE, null, 5);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + TABLE_RW + "(_id integer primary key," +
												  "date text," +
												  "usd text," +
												  "eur text," +
												  "weather text, " +
												  "logo text);");
		db.execSQL("create table " + TABLE_NEWS + "(_id integer primary key," +
													"id integer," +
									  				"title text," +
									  				"description text," +
									  				"date text," +
									  				"image text, " +
									  				"isnews integer);");
		db.execSQL("create table " + TABLE_AFISHA + "(_id integer primary key, " +
													 "title text, " +
													 "description text, " +
													 "date text, " +
													 "image text, " +
													 "source text);"); 
		db.execSQL("create table " + TABLE_BRIEFCASE + "(_id integer primary key, " +
													     "id integer);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_RW);
		db.execSQL("drop table if exists " + TABLE_NEWS);
		db.execSQL("drop table if exists " + TABLE_AFISHA);
		db.execSQL("drop table if exists " + TABLE_BRIEFCASE);
		onCreate(db);
	}
}
