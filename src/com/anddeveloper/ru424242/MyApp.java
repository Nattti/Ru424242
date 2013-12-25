package com.anddeveloper.ru424242;

import android.app.Application;

public class MyApp extends Application {
	boolean isLoad;
	boolean isAfisha;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		isLoad = false;
	}
	
	public void setLoad(boolean isload) {
		isLoad = isload;
	}
	
	public boolean getLoad() {
		return isLoad;
	}
	
	public void setAfisha(boolean isafisha) {
		isAfisha = isafisha;
	}
	
	public boolean getAfisha() {
		return isAfisha;
	}
}
