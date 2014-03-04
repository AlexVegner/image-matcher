package com.mytest.imagematcher.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.mytest.imagematcher.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
    private static final String TAG = "OCVSample::Activity";

    private static int MIN_DIST = 30;

    private CameraView mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private MenuItem mPlayMenu;
    private MenuItem mStopMenu;

    private SubMenu mCheckIntervalMenu;
    private MenuItem[] mCheckIntervalMenuItems;


    private boolean isActive;
    private long lastCheck;
    private int checkInterval = 5;
    private Mat lastMat = null;
    private int COUNT_OF_MACHES = 50;
    private int MAX_MACHES_DELTA_PERSENTAGE = 80;

    private static int descriptor = DescriptorExtractor.BRISK;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(CameraActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };



    public CameraActivity() {
       // Log.i(TAG, "Instantiated new " + Tutorial3Activity.getClass().getName());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat currentMat  = inputFrame.rgba();
        if (isActive && lastCheck + TimeUnit.SECONDS.toMillis(checkInterval) <= System.currentTimeMillis() ){
            lastCheck = System.currentTimeMillis();
            if (lastMat == null){
                lastMat = inputFrame.rgba();
                //TODO save first picture
            } else {
               /* MatchRequest mr = new MatchRequest();
                mr.setContect(this);
                mr.setDescriptor(DescriptorExtractor.BRISK);
                mr.setFirstMat(lastMat);
                mr.setSecondMat(currentMat);
                Mat m1 = lastMat.clone();
                Mat m2 = currentMat.clone();
                Imgproc.cvtColor(m1, m1, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.cvtColor(m2, m2, Imgproc.COLOR_RGBA2GRAY);
                mr.setFirstGrayMat(m1);
                mr.setSecondGrayMat(m2);*/

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

                Log.d("ImageComparator", "compare: " + compare);
                if(compare>0 && compare<1500) {
                    takeAPictureOnUiThread();
                }
                else if(compare==0) {
                    //TODO
                } else {
                    takeAPictureOnUiThread();

                }

                //new ImageComparator().execute(mr);


                /*Mat currentMat = inputFrame.rgba();
                Mat img1 = lastMat.clone();
                Mat img2 = currentMat.clone();

                Mat result = new Mat();

                Core.su
                *//*try {

                    Core.compare(img1, img2, result, Core.CMP_NE);

                    int val = Core.countNonZero(result);

                    Log.i("compare_log", "val = "+ String.valueOf(val));
                    if(val == 0) {
                        //Duplicate Image
                    } else {
                        //Different Image
                        lastMat = currentMat;
                        takeAPictureOnUiThread();
                    }
                } catch (CvException e){
                    lastMat = currentMat;
                    takeAPictureOnUiThread();
                 }*//*

                try {
                    if (isADuplicate(img1, img2)){
                        //showToastnUiThread("Images are exact duplicates");
                    } else {
                        //showToastnUiThread("Images are not duplicates");
                        lastMat = currentMat;
                        takeAPictureOnUiThread();
                    }





                } catch (CvException e){

                }*/



                /*
                img1.convertTo(img1, CvType.CV_32F);
                img2.convertTo(img2, CvType.CV_32F);
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
                    showToastnUiThread("Images may be possible duplicates, verifying");
                    //new asyncTask(CameraActivity.this).execute();
                }
                else if(compare==0)
                    showToastnUiThread("Images are exact duplicates");
                else {
                    showToastnUiThread("Images are not duplicates");
                    lastMat = currentMat;
                    lastCheck = System.currentTimeMillis();
                    takeAPictureOnUiThread();
                }*/
            }
        }
        if (lastMat != null)
            lastMat.release();
        lastMat = currentMat;
        return inputFrame.rgba();

    }


    private boolean isADuplicate(Mat img1, Mat img2){
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        //Definition of ORB keypoint detector and descriptor extractors
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        //Detect keypoints
        detector.detect(img1, keypoints1);
        detector.detect(img2, keypoints2);
        //Extract descriptors
        extractor.compute(img1, keypoints1, descriptors1);
        extractor.compute(img2, keypoints2, descriptors2);

        //Definition of descriptor matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        //Match points of two images
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1,descriptors2 ,matches);

        List<DMatch> matchesList = matches.toList();

        List<DMatch> matches_final = new ArrayList<DMatch>();
        int minDist = 1000000;
        int maxDist = 0;
        for (int i = 0; i < matchesList.size(); i++) {
            if (minDist > (int)matchesList.get(i).distance)
                minDist = (int)matchesList.get(i).distance;
            else if (maxDist < (int)matchesList.get(i).distance)
                maxDist = (int)matchesList.get(i).distance;
        }

        Log.i("currentMatchesCount", String.format("minDist = %s, maxDist = %s, deltaDist = %s", minDist, maxDist, minDist*2+maxDist*0.2 ));

        for (int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= minDist*2+maxDist*0.2) {
                matches_final.add(matches.toList().get(i));
            }
        }

        int keypoints1Size = keypoints1.toList().size();
        int keypoints2Size = keypoints2.toList().size();
        if (keypoints2Size == 0)
            return true;
        int countOfKeyPoint = keypoints1Size > keypoints2Size ? keypoints1Size : keypoints2Size;
        int currentMatchesCount =  matches_final.size();
        Log.i("currentMatchesCount", String.format("currentMatchesCount = %s, countOfKeyPoint = %s", currentMatchesCount, countOfKeyPoint));
        return countOfKeyPoint / 100 * MAX_MACHES_DELTA_PERSENTAGE <= currentMatchesCount;
    }

    private boolean isADuplicate2(Mat img1, Mat img2) {
        try {

            Imgproc.cvtColor(img1, img1, Imgproc.COLOR_BGR2RGB);
            Imgproc.cvtColor(img2, img2, Imgproc.COLOR_BGR2RGB);
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
            DescriptorExtractor descExtractor = DescriptorExtractor.create(descriptor);
            DescriptorMatcher matcher = DescriptorMatcher
                    .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            MatOfKeyPoint dupKeypoints = new MatOfKeyPoint();
            Mat descriptors = new Mat();
            Mat dupDescriptors = new Mat();
            MatOfDMatch matches = new MatOfDMatch();
            detector.detect(img1, keypoints);
            Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
            detector.detect(img2, dupKeypoints);
            Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
            // Descript keypoints
            //DescriptorExtractor descExtractor = DescriptorExtractor.create(descriptor);
            descExtractor.compute(img1, keypoints, descriptors);
            descExtractor.compute(img2, dupKeypoints, dupDescriptors);
            Log.d("LOG!", "number of descriptors= " + descriptors.size());
            Log.d("LOG!",
                    "number of dupDescriptors= " + dupDescriptors.size());
            // matching descriptors
            matcher.match(descriptors, dupDescriptors, matches);
            Log.d("LOG!", "Matches Size " + matches.size());
            // New method of finding best matches
            List<DMatch> matchesList = matches.toList();

            /*List<DMatch> matches_final = new ArrayList<DMatch>();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= MIN_DIST) {
                    matches_final.add(matches.toList().get(i));
                }
            }

            MatOfDMatch matches_final_mat = new MatOfDMatch();
            matches_final_mat.fromList(matches_final);*/

            int keypoints1Size = keypoints.toList().size();
            int keypoints2Size = dupKeypoints.toList().size();
            int countOfKeyPoint = keypoints1Size > keypoints2Size ? keypoints1Size : keypoints2Size;
            int currentMatchesCount =  matchesList.size();
            Log.i("currentMatchesCount", String.format("currentMatchesCount = %s, countOfKeyPoint = %s", currentMatchesCount, countOfKeyPoint));
            return countOfKeyPoint / 100 * MAX_MACHES_DELTA_PERSENTAGE <= currentMatchesCount;


        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void showToastnUiThread(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void takeAPictureOnUiThread(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                takeAPicture();
            }
        });
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

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
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

        //mPlayMenu.setGroupVisible(1, true);
        mStopMenu.setVisible(false);
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
            Size resolution = mResolutionList.get(id);
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
        }

        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //takeAPicture();
        return false;
    }

    private void  takeAPicture(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String fileName = Environment.getExternalStorageDirectory().getPath() +
                "/sample_picture_" + currentDateandTime;
        if (!isActive)
            return;
        mOpenCvCameraView.takePicture(fileName);

        Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
    }

    private void startRecord(){
        isActive = true;
        lastCheck = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(checkInterval);
        lastMat = null;
        mPlayMenu.setVisible(!isActive);
        mStopMenu.setVisible(isActive);
        mCheckIntervalMenu.setGroupVisible(5, !isActive);
        mColorEffectsMenu.setGroupVisible(1, !isActive);
        mResolutionMenu.setGroupVisible(2, !isActive);

    }

    private void stopRecord(){
        isActive = false;
        mPlayMenu.setVisible(!isActive);
        mStopMenu.setVisible(isActive);
        mCheckIntervalMenu.setGroupVisible(5, !isActive);
        mColorEffectsMenu.setGroupVisible(1, !isActive);
        mResolutionMenu.setGroupVisible(2, !isActive);
    }
}
