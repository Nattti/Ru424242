package com.anddeveloper.ru424242;

import java.util.HashMap;
import java.util.Map;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class Main extends TabActivity  implements OnTabChangeListener {
	TabHost mTabHost;
	Map<String, String> mNameById;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mNameById = new HashMap<String, String>();
        setTabs();
    }
    
    private void setTabs() {
    	Resources res = getResources();
    	
    	mTabHost = getTabHost();
    	mTabHost.setOnTabChangedListener(this);
    	
    	addTab(res.getString(R.string.strPrimary), R.drawable.tab_main, Primary.class);
    	addTab(res.getString(R.string.strEnterprise), R.drawable.tab_enter, Enterprise.class);
    	addTab(res.getString(R.string.strSearch), R.drawable.tab_search, Search.class);
    	addTab(res.getString(R.string.strNews), R.drawable.tab_news, News.class);
    	addTab(res.getString(R.string.strBriefcase), R.drawable.tab_brief, Briefcase.class);
    }
    
 	private void addTab(String labelId, int drawableId, Class<?> c) {
 		TabHost tabHost = getTabHost();
    	Intent intent = new Intent(this, c);
    	TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);
    	mNameById.put("tab" + labelId, labelId);
    	
    	View view = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		TextView title = (TextView) view.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		if (labelId == getResources().getString(R.string.strPrimary))
			spec.setContent(intent);
		else	
			spec.setContent(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    	spec.setIndicator(view);
    	tabHost.addTab(spec);
    }

	@Override
	public void onTabChanged(String tabId) {
		setTitle(mNameById.get(tabId));
	}
	
	public void swithTab(int tab) {
		mTabHost.setCurrentTab(tab);
	}
}