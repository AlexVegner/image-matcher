package com.mytest.imagematcher.matcher;

import android.app.ProgressDialog;
import android.util.Log;

import com.mytest.imagematcher.ui.Camera2Activity;
import com.mytest.imagematcher.utils.MatUtils;
import com.mytest.imagematcher.utils.ProjectPreferences;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 2/26/14.
 */
public class ImageMatchTask {

    private static Mat img1, img2, descriptors, dupDescriptors;
    private static FeatureDetector detector;
    private static DescriptorExtractor DescExtractor;
    private static DescriptorMatcher matcher;
    private static MatOfKeyPoint keypoints, dupKeypoints;
    private static MatOfDMatch matches, matches_final_mat;
    private static ProgressDialog pd;
    private static boolean isDuplicate = false;
    private Camera2Activity asyncTaskContext=null;
    private static Scalar RED = new Scalar(255,0,0);
    private static Scalar GREEN = new Scalar(0,255,0);
    private long endTime;
    private long startTime;
    private static String TAG = "image_macher";


    private static int descriptor;
    private static String descriptorType;
    private static int minDist;
    private static int minMatches;

    public ImageMatchTask(Camera2Activity context, Mat last, Mat current){
        this.img1 = last;
        this.img2 = current;
        asyncTaskContext=context;
        ProjectPreferences pref = ProjectPreferences.getInstance();
        this.minDist = pref.getMinDist();
        this.minMatches = pref.getMinMatches();
        this.descriptor = pref.getDescriptor();
        this.descriptorType = MatUtils.getDescriptorType(descriptor);
    }


    public void execute(){
        try {
            Mat img1 = this.img1.clone();
            Mat img2 = this.img2.clone();
            detector = FeatureDetector.create(FeatureDetector.PYRAMID_FAST);
            DescExtractor = DescriptorExtractor.create(descriptor);
            matcher = DescriptorMatcher
                    .create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            keypoints = new MatOfKeyPoint();
            dupKeypoints = new MatOfKeyPoint();
            descriptors = new Mat();
            dupDescriptors = new Mat();
            matches = new MatOfDMatch();
            detector.detect(img1, keypoints);
            Log.d("LOG!", "number of query Keypoints= " + keypoints.size());
            detector.detect(img2, dupKeypoints);
            Log.d("LOG!", "number of dup Keypoints= " + dupKeypoints.size());
            // Descript keypoints
            DescExtractor.compute(img1, keypoints, descriptors);
            DescExtractor.compute(img2, dupKeypoints, dupDescriptors);
            Log.d("LOG!", "number of descriptors= " + descriptors.size());
            Log.d("LOG!",
                    "number of dupDescriptors= " + dupDescriptors.size());
            // matching descriptors
            matcher.match(descriptors, dupDescriptors, matches);
            Log.d("LOG!", "Matches Size " + matches.size());
            // New method of finding best matches
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final = new ArrayList<DMatch>();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= minDist) {
                    matches_final.add(matches.toList().get(i));
                }
            }
            matches_final_mat = new MatOfDMatch();
            matches_final_mat.fromList(matches_final);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int goodMatches = matches_final_mat.toList().size();
        Log.i(TAG, String.format("in:  %S, out: %s, result %s",keypoints.toList().size(), dupKeypoints.toList().size(),  goodMatches ));

        isDuplicate = (goodMatches > minMatches);
        if (isDuplicate){
            asyncTaskContext.showToastnUiThread( String.format("Images are similar. \n Count of good matches: %d", goodMatches));
        } else {
            asyncTaskContext.takeAPictureOnUiThread(goodMatches);
        }
     }

}
