package com.anddeveloper.ru424242;



import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BaseDrawerActivity extends Activity {
	DrawerLayout mdDrawerLayout;
	ListView mdraweListView;
	String[] menuArray;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		menuArray=getResources().getStringArray(R.array.menu_array);
		mdraweListView=(ListView)findViewById(R.id.left_drawer);
		mdDrawerLayout=(DrawerLayout)findViewById(R.id.drawer);
		mdraweListView.setAdapter(new ArrayAdapter<String>(this,
                R.layout.base_drawer,menuArray ));
		
			
	}

}
