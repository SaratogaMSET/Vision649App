package com.example.suneelbelkhale.vision649;

import android.os.Environment;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by suneelbelkhale1 on 3/15/16.
 *
 */

public class TargetFinder {
    public static double CAM_UP_ANGLE = 5; //TODO
    public static double CAM_DOWN_ANGLE = 110;

    public static double RES_X = 288;
    public static double RES_Y = 352;

    public static double WIDTH_TARGET = 18.5; //in
    public static double STANDARD_VIEW_ANGLE = 0.454885;//0.9424778; //radians, for an Axis Camera 206 /////...54 degrees
    public static double MAX_Y_COORD = 352; ///RES_Y / 1.15; //293 //TODO find the actual angle of camera and the corresponding max y coord
    public static double MIN_Y_COORD = 170; //RES_Y / 4.3; //70
    public static double X_TARGET = 160;
    public static double K_PIX = 1.0/400;

    public static Center NO_CENTER = new Center(-1,-1);

    public static double POS_1_CAM_X = 160; //pixels
    public static double POS_2_CAM_X = 160; //pixels
    public static double POS_3_CAM_X = 160; //pixels
    public static double POS_4_CAM_X = 160; //pixels
    public static double POS_5_CAM_X = 160; //pixels

    public Mat hierarchy;
    public List<MatOfPoint> contours;
    public Mat finalImage;
    public Mat dilate, erode;

    Rect roi;

    //holds the hsv constants (enables faster editing through adb shell)
    static String HSVFileName = "hsv.txt";
    static String TAG = "TargetFinder";


    public TargetFinder(){

        dilate = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3));
        erode = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(3, 3));
    }

    //old method

    /**
     * Algorithm:
     *
     *  calls performThresh (see below)
     *  find contours
     *  find the largest object that is in the frame & is greater than the max size and in the upper half of the frame
     *
     * @return
     *
     *  Center found (NO_CENTER if none found)
     *
     */

    public Center findOneRetroTarget(Mat image){

        Center center =  new Center(-1,-1); //default
        Rect r1 = new Rect();


        ///
        finalImage = performThresh(image);
        /************/
        //CONTOURS AND OBJECT DETECTION
        contours = new ArrayList<>();
        hierarchy = new Mat();

        Moments mu;

        // find contours
        Imgproc.findContours(finalImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        boolean noValid = true;
        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
        {
            int largest = 0;

            // for each remaining contour, find the biggest
            for (int i = 0; i < contours.size(); i++)
            {
                double area = Imgproc.contourArea(contours.get(i));
                mu = Imgproc.moments(contours.get(i));
                double y_coord = mu.get_m01()/mu.get_m00();
                //greater than min size AND in the upper part of photo AND greater than the last biggest
                if (area > 20.0  &&  y_coord < MAX_Y_COORD && area >= Imgproc.contourArea(contours.get(largest))){
                    noValid = false;
                    largest = i;
                    //NetworkTable tab = NetworkTable.getTable("Obj " + i);

                    //Center:    mu.m10()/mu.m00() , mu.m01()/mu.m00()

                }
            }

            roi = Imgproc.boundingRect(contours.get(largest));
            mu = Imgproc.moments(contours.get(largest));

            //ASSUME LARGEST is the target, now calc dist

            //old
            double dist = calcDistAxis206(roi.width, WIDTH_TARGET, 320, STANDARD_VIEW_ANGLE);

            center = new Center(mu.get_m10()/mu.get_m00(), mu.get_m01()/mu.get_m00());

        }

        if (noValid){
            return NO_CENTER;
        }
        else {
            return center;
        }
    }

    //also old

    /**
     * Algorithm:
     *
     *  Gaussian Blur
     *  convert to HSV
     *  HSV threshold
     *  dilate
     *  erode
     *  dilate
     *  Blur again
     *  binary threshold to get rid of small elements
     *  dilate
     *
     * @return
     *
     *  threshed Mat
     *
     */

    public Mat performThresh(Mat image){
        Mat imageHSV, erode, dilate;
        imageHSV = new Mat();


        //BLUR
        Imgproc.GaussianBlur(image, image, new Size(11,11), 0);

        //read HSV from text file
        imageHSV = viewHSVFromFile(image);



        //DILATE > ERODE > DILATE
        dilate = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3, 3));
        Imgproc.dilate(imageHSV, imageHSV, dilate);//dilate
        erode = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(3, 3));
        Imgproc.erode(imageHSV, imageHSV, erode);
        Imgproc.dilate(imageHSV, imageHSV, dilate);

        //BLUR PART 2
        Imgproc.GaussianBlur(imageHSV, imageHSV, new Size(15, 15), 0);

        //THRESHING
        Imgproc.threshold(imageHSV, imageHSV, 73, 255, Imgproc.THRESH_BINARY);

        //DILATE ONCE MORE
        Imgproc.dilate(imageHSV, imageHSV, dilate);


        erode.release();
        dilate.release();
        System.gc();

        return imageHSV;
    }


    /**
     *
     * @return
     *
     *  converted binary Mat based on text file's constants after the HSV thresh
     *
     */

    ///CURRENT BEST: "(33,7,250)->(180,120,255)"
    public Mat viewHSVFromFile(Mat m){
        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2HSV);

        //Core.inRange(imageHSV, new Scalar(78, 124, 213), new Scalar(104, 255, 255), imageHSV);

        Scalar[] vals = readElements(HSVFileName);

        if (vals == null){
            vals = new Scalar[2];
                            //            vals[0] = new Scalar(60, 41, 218);    OLLLDDDDD
                            //            vals[1] = new Scalar(94, 255, 255);   OLDDDER
            vals[0] = new Scalar(33, 4, 230);
            vals[1] = new Scalar(85, 45, 255);
            Log.d(TAG, "File read failed ---- defaulting");
        }

        Core.inRange(m, vals[0], vals[1], m);

        return m;
    }

    //reading from text file
    public Scalar[] readElements(String filename){
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Vision");
            File filepath = new File(root, filename);  // file path to save
            BufferedReader in = new BufferedReader(new FileReader(filepath));

            String str1 = in.readLine();
            String str2 = in.readLine();
            String[] line1 = str1.split(",");
            String[] line2 = str2.split(",");


            Scalar[] s = new Scalar[]{new Scalar(Integer.parseInt(line1[0]), Integer.parseInt(line1[1]), Integer.parseInt(line1[2]))
                    , new Scalar(Integer.parseInt(line2[0]), Integer.parseInt(line2[1]), Integer.parseInt(line2[2]))};
            Log.d(TAG, "MIN: (" + s[0].val[0] + ", " + s[0].val[1] + ", " + s[0].val[2]
                    + ") MAX: (" + s[1].val[0] + ", " + s[1].val[1] + ", " + s[1].val[2] + ")");
            return s;
        }
        catch (Exception e){
            Log.e("Exception", "File READ failed: " + e.toString());
            return null;
        }
    }

    //for testing purposes
    public Mat testingAlgorithm(Mat m){

        Imgproc.GaussianBlur(m, m, new org.opencv.core.Size(11, 11), 0);

        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);

        Imgproc.threshold(m, m, 245, 255, Imgproc.THRESH_TOZERO);


        return m;

    }

    /**
     * Algorithm:
     *
     *  Gaussian Blur
     *  cvt to gray
     *  threshold (Tozero)
     *      //TODO add erode and dilates and other thresh fcts here
     *  find contours
     *  find, draw, and fill convex hulls
     *  subtract the threshed image from the one with filled convex hulls to get the new filled portions (blobs)
     *      ->this allows us to roughly find a U shape
     *  find contours of the blobs
     *  approximate the polygon (should be a rectangle)
     *  find the largest object & is greater than the max size & has 4 corners (and is not circular)
     *  Calculate its center and other features
     *
     * @return
     *
     *  Dictionary of objects
     *  -> "center" Center of found object
     *  -> "roi" Bounding rect of selected object
     *  -> "m" after convex hull fill
     *  -> "hsv"
     *  -> "thresh" the mat after initial thresholding
     *  -> "subImage" subtraced image
     *  -> "blobMat" after find contours of blobs
     *
     */

    public Map<String, Object> shapeDetectTarget(Mat m){
        long _time = Calendar.getInstance().getTimeInMillis();

        Center center = NO_CENTER;
        Mat hsv = new Mat();
        Mat ycrcb = new Mat();

        Imgproc.GaussianBlur(m, m, new org.opencv.core.Size(5, 5), 0);
        //Imgproc.medianBlur(m, m, 11);

        Log.d("->timelog", "GAUSSIAN t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfInt> hull = new ArrayList<MatOfInt>();


        //Imgproc.cvtColor(m, ycrcb, Imgproc.COLOR_RGB2YCrCb);
//        Imgproc.cvtColor(m, ycrcb, Imgproc.COLOR_RGB2Luv);
//        Scalar[] scalars = readElements("Lab.txt");
//        Log.d(TAG, scalars.toString());
//        Core.inRange(ycrcb, scalars[0], scalars[1],ycrcb);

        //Core.inRange(hsv, new Scalar(25, 2, 180), new Scalar(120, 255, 255), hsv); //relatively loose
//        //TROLL NOT ACTUALLY HSV HAHA
//        Imgproc.cvtColor(m, hsv, Imgproc.COLOR_RGB2YCrCb);
//        Scalar[] scalars = readElements("ycrcb.txt");
//        Core.inRange(hsv, scalars[0], scalars[1],hsv);

        HSVThread hsvSide = new HSVThread(m);
        hsvSide.start();
        GrayThread graySide = new GrayThread(m);
        graySide.start();

        try {
            graySide.join();
            hsvSide.join();
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        Log.d("->timelog", "Both threads t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();


        //find overlap
        Mat combined = new Mat();
        try {
            Core.bitwise_and(hsvSide.getFinalMat(), graySide.getFinalMat(), m); //think &&
        }
        catch(Exception e){
            return new HashMap<>(); //dun got broked, don' do this
        }

        //Imgproc.dilate(combined, m, dilate); //dilate to be safe

        Mat thresh = new Mat(), subImage = new Mat();
        m.copyTo(thresh); //copy filtered image

        Log.d("->timelog", "Bit AND & dilate t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        Imgproc.findContours(m, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Log.d("->timelog", "find contours t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        MatOfInt hullInt = new MatOfInt();
        MatOfPoint hullPointMat;
        List<Point> hullPointList = new ArrayList<>();
        List<MatOfPoint> hullPoints = new ArrayList<>();

        //draw convex Hulls
        for (int k=0; k < contours.size(); k++){
            MatOfPoint cont = contours.get(k);

            Imgproc.convexHull(cont, hullInt);

            hullPointList.clear();
            for (int j = 0; j < hullInt.toList().size(); j++) {
                hullPointList.add(cont.toList().get(hullInt.toList().get(j)));
            }

            hullPointMat = new MatOfPoint();
            hullPointMat.fromList(hullPointList);
            hullPoints.add(hullPointMat);
        }
        m = new Mat();
        thresh.copyTo(m); //bring the old one back and draw new stuff on it

        Log.d("->timelog", "draw Convex hulls t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();
//
//        //at this point thresh and m are the same (no contours just the threshold)
//        //draw the full contours on both
//
//        Imgproc.drawContours(thresh, contours, -1, new Scalar(255, 255, 255), 1);
//        Imgproc.fillPoly(thresh, contours, new Scalar(255, 255, 255));
////
        //Imgproc.drawContours(m, contours, -1, new Scalar(255, 255, 255), 1);
//        Imgproc.fillPoly(m, contours, new Scalar(255, 255, 255));
////
//        Imgproc.drawContours(m, hullPoints, -1, new Scalar(255, 255, 255), 1);
//        Imgproc.fillPoly(m,hullPoints, new Scalar(255,255,255));


        MatOfPoint2f approx;
        Log.d(TAG, "H SIZE: " + hullPoints.size());
        for (int i = 0; i < hullPoints.size(); i++) {
            Imgproc.fillConvexPoly(m, hullPoints.get(i), new Scalar(255,255,255));
        }

        Log.d("->timelog", "fillConvexPoly t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();


        //now we are left with whatever was filled in
        Core.subtract(m, thresh, subImage);

        Log.d("->timelog", "subtract images t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        Imgproc.erode(subImage, subImage, erode);
        //Imgproc.erode(subImage, subImage, erode);
        Imgproc.threshold(subImage, subImage, 250, 255, Imgproc.THRESH_BINARY);

        Log.d("->timelog", "2nd binary thresh t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        Mat blobMat = new Mat();
        subImage.copyTo(blobMat);
        List<MatOfPoint> blobContours = new ArrayList<>();
        Imgproc.findContours(blobMat, blobContours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Log.d("->timelog", "2nd find contours t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();


        boolean noValid = true;
        Moments mu;
        approx = new MatOfPoint2f();
        // if any contour exist...
        Log.d(TAG, "Blob count: " + blobContours.size());
        if (blobContours.size() > 0) {
            int largest = 0;

            // for each remaining contour, find the biggest
            for (int h = 0; h < blobContours.size(); h++) {
                MatOfPoint cont = blobContours.get(h);
                double area = Imgproc.contourArea(cont);
                mu = Imgproc.moments(cont);
                //number of corners
                approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(new MatOfPoint2f(cont.toArray()), approx,8, true);

                Point circ_center = new Point();
                float[] radius = new float[1];

                double y = mu.get_m01() / mu.get_m00();

                Imgproc.minEnclosingCircle(new MatOfPoint2f(cont.toArray()), circ_center, radius);
                boolean circle = Math.abs(area - Math.PI * radius[0] * radius[0]) < 5;
                //greater than min size AND in the upper part of photo AND greater than the last biggest
                if (area > 50.0 && area >= Imgproc.contourArea(blobContours.get(largest))&& approx.rows() == 4 && y < MAX_Y_COORD && y > MIN_Y_COORD){ //&& !circle)) {
                    noValid = false;
                    largest = h;

                }



            }

            Log.d("->timelog", "filtering blobs t: " + (Calendar.getInstance().getTimeInMillis() - _time));
            _time = Calendar.getInstance().getTimeInMillis();

            roi = Imgproc.boundingRect(blobContours.get(largest));
            mu = Imgproc.moments(blobContours.get(largest));
            center = new Center(mu.get_m10() / mu.get_m00(), mu.get_m01() / mu.get_m00());
//            Log.d(TAG, "m00: " + mu.get_m00());

        }

        //the array we return
        //          1:
        Map<String, Object> foundElements = new HashMap<>();
//        List<Object> foundElements = new ArrayList<>();


        if (noValid) {
            foundElements.put("center", NO_CENTER);

        }
        else {
            foundElements.put("center", center);
        }

        foundElements.put("roi", roi);
        foundElements.put("m", m);
        foundElements.put("hsv", hsv);
        foundElements.put("thresh", thresh);
        foundElements.put("combined", combined);
        foundElements.put("subImage", subImage);
        foundElements.put("blobMat", blobMat);
//        foundElements.put("ycrcb", ycrcb);


        return foundElements;
    }


    //old
    public double calcDistAxis206(double obj_pix, double obj_in, double view_pix, double max_cam_angle){
        return view_pix * obj_in / (2*Math.tan(max_cam_angle) * obj_pix);
    }

    public class HSVThread extends Thread{

        Mat m;
        boolean ready;

        public HSVThread(Mat mat){
            m = new Mat();
            mat.copyTo(m);
            ready = false; //kinda redundant but meh
        }

        @Override
        public void run() {
            if (m != null){
                //hsv side
                Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2HSV);
                Scalar[] scalars = readElements(HSVFileName);
                Core.inRange(m, scalars[0], scalars[1], m); //"20,3,215 - > 75,250,250"
                //used to be 15,2,210 -> 100,255,255


                Imgproc.dilate(m, m, dilate);//dilate
                Imgproc.erode(m, m, erode);
                //Imgproc.dilate(m, m, dilate);
                Imgproc.dilate(m, m, dilate);
                ready = true;
            }
        }

        public Mat getFinalMat(){
            if (ready){
                return m;
            }
            return new Mat();
        }
    }

    public class GrayThread extends Thread{

        Mat m;
        boolean ready;

        public GrayThread(Mat mat){
            m = new Mat();
            mat.copyTo(m);
            ready = false; //kinda redundant but meh
        }

        @Override
        public void run() {
            if (m != null){
                //brightness side
                Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
                Imgproc.threshold(m, m, 180, 250, Imgproc.THRESH_TOZERO);
//                Imgproc.threshold(m,m,200, 255, Imgproc.THRESH_BINARY);

                Imgproc.dilate(m, m, dilate);//dilate
                Imgproc.erode(m, m, erode);
                //Imgproc.dilate(m, m, dilate);
                Imgproc.dilate(m, m, dilate);
                ready = true;
            }
        }

        public Mat getFinalMat(){
            if (ready){
                return m;
            }
            return new Mat();
        }
    }


//        Imgproc.cornerHarris(m, cornerMat,2,3,0.04);
//        Core.normalize(cornerMat, cornerMat, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1, new Mat());
//        Core.convertScaleAbs(cornerMat, cornerMat);

//        for (int x = 0; x < cornerMat.rows(); x++){
//            for (int y = 0; y < cornerMat.cols(); y++) {
//                if (cornerMat.get(x,y)[0] > 200 ){
//                    Imgproc.circle(m, new Point(x,y), 5, new Scalar(255,0,0));
//                }
//            }
//        }



}
