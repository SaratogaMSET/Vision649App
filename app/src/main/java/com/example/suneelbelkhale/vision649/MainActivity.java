package com.example.suneelbelkhale.vision649;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageWriter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    //from robot
    String SERVER_IP = "10.6.49.70";
    int SERVER_PORT = 5050;

    int MAX_X = 288, MAX_Y = 352; //cam resolution
    int green = Color.parseColor("#43fa00"), red = Color.parseColor("#fa0000");

    Handler handler = new Handler();

    static String TAG = "Vision649";
    static String fileName = "vision649.txt";

    TargetFinder targetFinder;

    RelativeLayout colorScreen;

    Thread clientThread;
    private PortraitCameraView mOpenCvCameraView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                    Log.d(this.getClass().getSimpleName(), "CAMERA SUCCESS");
                } break;
                default:
                {
                    super.onManagerConnected(status);

                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ////*******//////

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        ////*******//////


        setContentView(R.layout.activity_main);

        //Check if opencv is working
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        mCamera = Camera.open();
//        mCamera.setDisplayOrientation(90);

        colorScreen = (RelativeLayout) findViewById(R.id.colorBack);

        mOpenCvCameraView = (PortraitCameraView) findViewById(R.id.HelloOpenCvView_java);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(600, 600);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        targetFinder = new TargetFinder();


    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this,
                mLoaderCallback);

        targetFinder = new TargetFinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    Map<String, Object> results;

    @Override
    public Mat onCameraFrame(PortraitCameraView.CvCameraViewFrame inputFrame) {
        //TODO this is where we will add image processing


        double t1 = System.currentTimeMillis();

        Mat m = new Mat(), original = new Mat();


        inputFrame.rgba().copyTo(original);

        original.copyTo(m);

        //initial
        double _time = Calendar.getInstance().getTimeInMillis();

        results = targetFinder.shapeDetectTarget(m);

        Log.d("->timelog", "SHAPE DETECT TARGET FULL t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        Center c = (Center)results.get("center");
        m = (Mat)results.get("m");
        Mat thresh = (Mat)results.get("thresh");
        Mat subImage = (Mat)results.get("subImage");
        Mat blobMat = (Mat)results.get("blobMat");
        Mat combined = (Mat)results.get("combined");
        Mat hsv = (Mat)results.get("hsv");
//        Mat ycrcb = (Mat)results.get("ycrcb");
        Rect roi = (Rect)results.get("roi");

        Log.d("->timelog", "Pulling data t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();


        DecimalFormat df = new DecimalFormat("#.##");

        String formattedCenter = "C (" + df.format(c.x) + ", " + df.format(c.y) + ")";

        //Log.d(TAG, formattedCenter);

        if (!c.equals(TargetFinder.NO_CENTER)) {
            Imgproc.putText(original, "FOUND: " + formattedCenter, new Point(10, 20), Core.FONT_HERSHEY_PLAIN, 0.5, new Scalar(255, 0, 0));
            Imgproc.line(original, new Point(c.x + 10, c.y), new Point(c.x - 10, c.y), new Scalar(255, 0, 0), 2);
            Imgproc.line(original, new Point(c.x, c.y + 10), new Point(c.x, c.y - 10), new Scalar(255, 0, 0), 2);
            Imgproc.rectangle(original, roi.tl(), roi.br(), new Scalar(0, 255, 0));
            // update the background
            if (c.x > MAX_X / 2.0 - 5 && c.x < MAX_X / 2.0 + 5) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        colorScreen.setBackgroundColor(green);
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        colorScreen.setBackgroundColor(red);
                    }
                });
            }

        }
        else {
            Imgproc.putText(original, "No Target Detected", new Point(10, 20), Core.FONT_HERSHEY_PLAIN, 0.5, new Scalar(0, 0, 255));
            handler.post(new Runnable() {
                @Override
                public void run() {
                    colorScreen.setBackgroundColor(red);
                }
            });
        }

        Log.d("->timelog", "draw crap on image t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        //draw boundaries
        Imgproc.line(original, new Point(0, TargetFinder.MIN_Y_COORD), new Point(TargetFinder.RES_X, TargetFinder.MIN_Y_COORD), new Scalar(20, 20, 20), 2);
        Imgproc.line(original, new Point(0, TargetFinder.MAX_Y_COORD), new Point(TargetFinder.RES_X, TargetFinder.MAX_Y_COORD), new Scalar(20, 20, 20), 2);


        //writeToFile(fileName, "" + df.format(c.x) + " " + df.format(c.y));//+ " <> Time: " + df.format(System.currentTimeMillis()));

        Log.d("->timelog", "draw 2 lines and write to file t: " + (Calendar.getInstance().getTimeInMillis() - _time));
        _time = Calendar.getInstance().getTimeInMillis();

        //post it to the rio
        clientThread = new Thread(new Client(c));
        clientThread.start(); //hella

        //Log.d("T>>>>>", "" + (System.currentTimeMillis() - t1));

        return original;
    }


    private void writeToFile(String fName, String data) {


        try {
            //verifyStoragePermissions(this);

            File root = new File(Environment.getExternalStorageDirectory(), "Vision");
            File filepath = new File(root, fName);  // file path to save
            FileWriter fileOut = new FileWriter(filepath);

            fileOut.write("");
            fileOut.append(data + "\n");

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 1);

            fileOut.append("" + cal.getTime());
            fileOut.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public class Client implements Runnable{
        Center c;

        public Client(Center c){
            this.c = c;
        }

        @Override
        public void run(){
            try {
                Socket s = new Socket(SERVER_IP, SERVER_PORT); //set up on Robot
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                if (c != null) {
                    dos.writeUTF("" + c.x + ", " + c.y); //EG sent like:  120.2, 222.3
                    dos.flush();
                    dos.close();
                }
                s.close();
            }
            catch (UnknownHostException e){
                Log.e(TAG, "Unknown Host Error: " + e.getMessage());
            }
            catch (IOException e){
                Log.e(TAG, "IO Error: " + e.getMessage());
            }
        }
    }

}




