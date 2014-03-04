package com.mytest.imagematcher.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public abstract class CommonPreferences {

	private SharedPreferences sp;
	
	protected CommonPreferences(Context context){
		sp = context.getSharedPreferences(getName(), Context.MODE_PRIVATE);
	}

    public SharedPreferences getSharedPreferences(){
        return sp;
    }

    protected abstract String getName();
	
	protected String get( String key, String defValue ) {
		return sp.getString(key, defValue);
	}
	
	public boolean get( String key, boolean defValue ) {
		return sp.getBoolean(key, defValue);
	}
	
	protected long get(String key, long defValue) {
		return sp.getLong(key, defValue);
	}
	
	protected int get(String key, int defValue) {
		return sp.getInt(key, defValue);
	}

	protected float get(String key, float defValue) {
		return sp.getFloat(key, defValue);
	}
	
	protected void set( String key, String value ) {
		SharedPreferences.Editor ed = sp.edit();
		ed.putString(key, value);
		ed.commit();
	}
	
	public void set(String key, boolean value ) {
		SharedPreferences.Editor ed = sp.edit();
		ed.putBoolean(key, value);
		ed.commit();
	}
	
	protected void set(String key, long value) {
		SharedPreferences.Editor ed = sp.edit();
		ed.putLong(key, value);
		ed.commit();
	}
	
	protected void set(String key, int value) {
			SharedPreferences.Editor ed = getEditor();
			ed.putInt(key, value);
			commit(ed);		
	    }
    
	public SharedPreferences.Editor getEditor(){
	    return sp.edit();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void commit(SharedPreferences.Editor ed){
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			// async call
	        ed.apply();
	    } else {
	        // old api, writes disk on UI thread
	        ed.commit();
	    }
	}


	
}

