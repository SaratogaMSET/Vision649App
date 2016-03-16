package com.example.suneelbelkhale.vision649;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;




public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CameraBridgeViewBase mOpenCvCameraView;

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
        setContentView(R.layout.activity_main);

        //Check if opencv is working
        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }

        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView_java);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
//
//        ImageView v = (ImageView) findViewById(R.id.imageView1);
//        v.setDrawingCacheEnabled(true);
//
//        // this is the important code :)
//        // Without it the view will have a dimension of 0,0 and the bitmap will be null
//        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
//
//        v.buildDrawingCache(true);
//
//        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
//        Mat m = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
//
//        v.setDrawingCacheEnabled(false); // clear drawing cache
//
//        Utils.bitmapToMat(b, m);
//
//        Mat im_canny = new Mat();  // you have to initialize output image before giving it to the Canny method
//        Imgproc.Canny(m, im_canny, 70, 100);
//
//        Utils.matToBitmap(im_canny, b);
//
//        v.setImageBitmap(b);


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
    }

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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //TODO this is where we will add image processing

        TargetFinder targetFinder = new TargetFinder();

        Center c = targetFinder.findOneRetroTarget(inputFrame.rgba());

        Mat m = new Mat();

        inputFrame.rgba().copyTo(m);


        if (!c.equals(TargetFinder.NO_CENTER)) {
            Imgproc.line(m, new Point(c.x + 30, c.y), new Point(c.x - 30, c.y), new Scalar(0, 255, 0), 3);
            Imgproc.line(m, new Point(c.x, c.y + 30), new Point(c.x, c.y - 30), new Scalar(0, 255, 0), 3);
        }

        else {
            Imgproc.putText(m, "No Target Detected", new Point(10, 10), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255,0,0));
        }

        return m;

    }

}
