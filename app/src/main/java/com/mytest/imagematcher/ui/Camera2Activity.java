package com.mytest.imagematcher.ui;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.mytest.imagematcher.R;
import com.mytest.imagematcher.matcher.ImageMatchTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class Camera2Activity extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private static int MIN_DIST = 30;

    private int checkInterval = 5;


    private CameraView mOpenCvCameraView;

    private static long startTime, endTime;

    // Menu
    private List<Camera.Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private MenuItem mPlayMenu;
    private MenuItem mStopMenu;

    private SubMenu mCheckIntervalMenu;
    private MenuItem[] mCheckIntervalMenuItems;


    private Mat lastMat = null;
    private boolean isActive;
    private long lastCheck;

    private MenuItem mMachCongig;



    private static int descriptor = DescriptorExtractor.BRISK;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // This has to be called before setContentView
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.tutorial3_surface_view);

        mOpenCvCameraView = (CameraView) findViewById(R.id.tutorial3_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        stopRecord();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        if (isActive && lastCheck + TimeUnit.SECONDS.toMillis(checkInterval) <= System.currentTimeMillis() ){
            lastCheck = System.currentTimeMillis();
            if (lastMat == null){
                lastMat = inputFrame.rgba();
                //TODO save first picture
            } else {
                showProgressBar(true);
                Mat currentMat = inputFrame.rgba();
                Mat img1 = lastMat.clone();
                Mat img2 = currentMat.clone();
                Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
                img1.convertTo(img1, CvType.CV_32F);
                img2.convertTo(img2, CvType.CV_32F);
                //Log.d("ImageComparator", "img1:"+img1.rows()+"x"+img1.cols()+" img2:"+img2.rows()+"x"+img2.cols());
                Mat hist1 = new Mat();
                Mat hist2 = new Mat();
                MatOfInt histSize = new MatOfInt(180);
                MatOfInt channels = new MatOfInt(0);
                ArrayList<Mat> bgr_planes1= new ArrayList<Mat>();
                ArrayList<Mat> bgr_planes2= new ArrayList<Mat>();
                Core.split(img1, bgr_planes1);
                Core.split(img2, bgr_planes2);
                MatOfFloat histRanges = new MatOfFloat (0f, 180f);
                boolean accumulate = false;
                Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, accumulate);
                Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
                Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, accumulate);
                Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
                img1.convertTo(img1, CvType.CV_32F);
                img2.convertTo(img2, CvType.CV_32F);
                hist1.convertTo(hist1, CvType.CV_32F);
                hist2.convertTo(hist2, CvType.CV_32F);

                double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
                Log.d("ImageComparator", "compare: "+compare);
                if(compare>0 && compare<1500) {
                    showToastnUiThread( "Images may be possible duplicates, verifying");
                    new ImageMatchTask(Camera2Activity.this, lastMat.clone(), currentMat.clone()).execute();
                }
                else if(compare==0){
                    showToastnUiThread( "Images are exact duplicates");
                } else {
                    showToastnUiThread( "Images are not duplicates");
                    takeAPictureOnUiThread(null);
                }

                showProgressBar(false);
            }
        }
        return inputFrame.rgba();
    }


    private void startRecord(){
        isActive = true;
        lastCheck = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(checkInterval);
        lastMat = null;
        configureMenu();
    }

    private void stopRecord(){
        isActive = false;
        configureMenu();
    }

    private void configureMenu(){
        mPlayMenu.setVisible(!isActive);
        mStopMenu.setVisible(isActive);
        mCheckIntervalMenu.getItem().setVisible(!isActive);
        mColorEffectsMenu.getItem().setVisible(!isActive);
        mResolutionMenu.getItem().setVisible(!isActive);
        mMachCongig.setVisible(!isActive);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mOpenCvCameraView == null){
            return false;
        }

        mCheckIntervalMenu = menu.addSubMenu("Check interval");
        mCheckIntervalMenuItems = new MenuItem[3];
        mCheckIntervalMenuItems[0] = mCheckIntervalMenu.add(5,0, Menu.NONE, "2 seconds");
        mCheckIntervalMenuItems[0] = mCheckIntervalMenu.add(5,1, Menu.NONE, "5 seconds");
        mCheckIntervalMenuItems[0] = mCheckIntervalMenu.add(5,2, Menu.NONE, "10 seconds");

        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
            String element = effectItr.next();
            mEffectMenuItems[idx] = mColorEffectsMenu.add(5, idx, Menu.NONE, element);
            idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Camera.Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
        }

        mPlayMenu = menu.add(3, 3, 0,"Play");
        mPlayMenu.setIcon(android.R.drawable.ic_media_play);
        mPlayMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mStopMenu = menu.add(4, 4, 0,"Stop");
        mStopMenu.setIcon(android.R.drawable.ic_media_pause);
        mStopMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mStopMenu.setVisible(false);

        mMachCongig = menu.add(6, 6, 0,"Match config");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Camera.Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == 3){
            startRecord();
        } else if (item.getItemId() == 4){
            stopRecord();
        } else if (item.getGroupId() == 5){
            if (item.getItemId() == 0){
                Toast.makeText(this, "2 seconds set", Toast.LENGTH_SHORT).show();
                checkInterval = 2;
            } else if (item.getItemId() == 1){
                Toast.makeText(this, "5 seconds set", Toast.LENGTH_SHORT).show();
                checkInterval = 5;
            } else if (item.getItemId() == 2){
                Toast.makeText(this, "10 seconds set", Toast.LENGTH_SHORT).show();
                checkInterval = 10;
            }
        } else if (item.getGroupId() == 6){
            Intent call = new Intent(Camera2Activity.this, SettingsActivity.class);
            startActivity(call);
        }
        return true;
    }


    public void showToastnUiThread(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Camera2Activity.this, message, Toast.LENGTH_LONG).show();
            }
        });

    }

    public void takeAPictureOnUiThread(final Integer goodMatches){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                takeAPicture(goodMatches);
            }
        });
    }

    private void showProgressBar(final boolean value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(value);
            }
        });
    }

    private void  takeAPicture(Integer goodMatches){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = "Not a duplicate. \n" + goodMatches== null ? "" : String.format("Count of good matches: %d \n", goodMatches)
                + Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime;
        if (!isActive)
            return;
        mOpenCvCameraView.takePicture(fileName);

        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
    }






}
