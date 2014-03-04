package com.mytest.imagematcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.opencv.features2d.DescriptorExtractor;

public class ProjectPreferences extends CommonPreferences {

    private static final String RREF_FILE_NAME = "ImageMacher";

    private static final String PREF_DESCRIPTOR = "pref_descriptor";
    private static final String PREF_MIN_DIST = "pref_min_dist";
    private static final String PREF_MIN_MATCHES = "pref_min_matches";

    private static final int DEFAULT_PREF_DESCRIPTOR = DescriptorExtractor.BRISK;
    private static final int DEFAULT_PREF_MIN_DIST = 10;
    private static final int DEFAULT_PREF_MIN_MATCHES = 750;

   	private static ProjectPreferences instance;
    private Context context;

    /**
     * Run initialization in Application.onCreate
     * @param context - ApplicationContext
     */
    public synchronized static void initialize(Context context){
        if (instance == null) {
            instance = new ProjectPreferences(context);
        }
    }

    protected ProjectPreferences(Context context){
        super(context);
        this.context = context;
    }

    public static ProjectPreferences getInstance(){
        return instance;
    }

    @Override
    public String getName() {
        return RREF_FILE_NAME;
    }


    public int getDescriptor(){
        return get(PREF_DESCRIPTOR, DEFAULT_PREF_DESCRIPTOR);
    }

    public void setDescriptor(int value){
        set(PREF_DESCRIPTOR, value);
    }

    public int getMinDist(){
        return get(PREF_MIN_DIST, DEFAULT_PREF_MIN_DIST);
    }

    public void setMinDist(int value){
        set(PREF_MIN_DIST, value);
    }

    public int getMinMatches(){
        return get(PREF_MIN_MATCHES, DEFAULT_PREF_MIN_MATCHES);
    }

    public void setMinMatches(int value){
        set(PREF_MIN_MATCHES, value);
    }

    public void applyMachSettings(int descriptor, int minDist, int minMatches){
        SharedPreferences.Editor ed = getEditor();
        set(PREF_DESCRIPTOR, descriptor);
        set(PREF_MIN_DIST, minDist);
        set(PREF_MIN_MATCHES, minMatches);
        commit(ed);
    }

}
