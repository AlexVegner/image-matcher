package com.mytest.imagematcher.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.mytest.imagematcher.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Alex on 2/26/14.
 */
public class MatUtils {

    /**
     * Hide constructor for utility class
     */
    private MatUtils(){}

    public static void saveImageToDisk(Mat source, String filename, String directoryName, Context ctx, int colorConversion){

        Mat mat = source.clone();
        if(colorConversion != -1)
            Imgproc.cvtColor(mat, mat, colorConversion, 4);

        Bitmap bmpOut = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmpOut);
        if (bmpOut != null){

            mat.release();
            OutputStream fout = null;
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            String dir = root + "/" + ctx.getResources().getString(R.string.app_name) + "/" + directoryName;
            String fileName = filename + ".jpg";
            File file = new File(dir);
            file.mkdirs();
            file = new File(dir, fileName);

            try {
                fout = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fout);
                bmpOut.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                bmpOut.recycle();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
        bmpOut.recycle();
    }

    public static String getDescriptorType(int descriptor){
        if (descriptor == DescriptorExtractor.BRIEF)
            return "BRIEF";
        else if (descriptor == DescriptorExtractor.BRISK)
            return "BRISK";
        else if (descriptor == DescriptorExtractor.FREAK)
            return "FREAK";
        else if (descriptor == DescriptorExtractor.ORB)
            return "ORB";
        else if (descriptor == DescriptorExtractor.SIFT)
            return "SIFT";
        else if(descriptor == DescriptorExtractor.SURF)
            return "SURF";
        return null;
    }

}
