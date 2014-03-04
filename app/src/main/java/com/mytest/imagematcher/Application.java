package com.mytest.imagematcher;

import com.mytest.imagematcher.utils.ProjectPreferences;

/**
 * Created by Alex on 3/4/14.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ProjectPreferences.initialize(getApplicationContext());
    }

}