package com.example.suneelbelkhale.vision649;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    private PortraitCameraView mOpenCvCameraView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static String TAG = "Vision649";
    static String fileName = "vision649.txt";

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

        mOpenCvCameraView = (PortraitCameraView) findViewById(R.id.HelloOpenCvView_java);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(600,600);





        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

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

    Map<String, Object> results;

    @Override
    public Mat onCameraFrame(PortraitCameraView.CvCameraViewFrame inputFrame) {
        //TODO this is where we will add image processing


        double t1 = System.currentTimeMillis();

        TargetFinder targetFinder = new TargetFinder();

        Mat m = new Mat(), original = new Mat();


        inputFrame.rgba().copyTo(original);

        original.copyTo(m);

        results = targetFinder.shapeDetectTarget(m);

        Center c = (Center)results.get("center");
        m = (Mat)results.get("m");
        Mat thresh = (Mat)results.get("thresh");
        Mat subImage = (Mat)results.get("subImage");
        Mat blobMat = (Mat)results.get("blobMat");
        Mat combined = (Mat)results.get("combined");
        Mat hsv = (Mat)results.get("hsv");
        Rect roi = (Rect)results.get("roi");


        DecimalFormat df = new DecimalFormat("#.##");

        String formattedCenter = "C (" + df.format(c.x) + ", " + df.format(c.y) + ")";

        Log.d(TAG, formattedCenter);

        if (!c.equals(TargetFinder.NO_CENTER)) {
            Imgproc.putText(original, "FOUND: " + formattedCenter, new Point(10, 10), Core.FONT_HERSHEY_PLAIN, 0.5, new Scalar(255,0,0));
            Imgproc.line(original, new Point(c.x + 10, c.y), new Point(c.x - 10, c.y), new Scalar(255, 0, 0), 2);
            Imgproc.line(original, new Point(c.x, c.y + 10), new Point(c.x, c.y - 10), new Scalar(255, 0, 0), 2);
            Imgproc.rectangle(original, roi.tl(), roi.br(), new Scalar(0,255,0));
        }

        else {
            Imgproc.putText(original, "No Target Detected", new Point(10, 10), Core.FONT_HERSHEY_PLAIN, 0.5, new Scalar(0,0,255));
        }

        writeToFile(fileName, "" + df.format(c.x) + " " + df.format(c.y));//+ " <> Time: " + df.format(System.currentTimeMillis()));


        Log.d("T>>>>>", "" + (System.currentTimeMillis() - t1));

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



}

















//////////IMPORTANT OLD STUFF///////////////




//public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {
//
//    private static final String TAG = "Camera2Test";
//
//    static {
//        if (!OpenCVLoader.initDebug()) {
//            // Handle initialization error
//            Log.d(TAG, "NO OPENCV ERRORS");
//        }
//    }
//
//    private CameraDevice mCamera;
//    private CameraCaptureSession mSession;
//    private TextureView mPreviewView;
//    private Surface mYUVCaptureSurface, mPreviewSurface;
//    private ImageReader mMainImageReader;
//    private CaptureResult mPendingResult;
//    private Size mPreviewSize;
//    Canvas canvas;
//    //private File mPhotoDir;
//    private CameraCharacteristics mCharacteristics;
//    private int mCaptureImageFormat;
//
//    TargetFinder targetFinder;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
//                View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN);
//
//        mPreviewView = new TextureView(this);
//        setContentView(mPreviewView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//
//        // wait for surface to be created
//        mPreviewView.setSurfaceTextureListener(this);
//
//        targetFinder = new TargetFinder();
//    }
//
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        try {
//            initCamera(surface);
//        } catch (CameraAccessException e) {
//            Log.e(TAG, "Failed to open camera", e);
//        }
//    }
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        if (mCamera != null) {
//            mCamera.close();
//            mCamera = null;
//        }
//        mSession = null;
//        return true;
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//
//    }
//
//
//    private void initCamera(SurfaceTexture surface) throws CameraAccessException {
//        CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
//
//        // get ID of rear-facing camera
//        String[] cameraIds = cm.getCameraIdList();
//        String cameraId = null;
//        CameraCharacteristics cc = null;
//        for (String id : cameraIds) {
//            cc = cm.getCameraCharacteristics(id);
//            if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
//                cameraId = id;
//                break;
//            }
//        }
//        if (cameraId == null) {
//            throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find suitable camera");
//        }
//
//        mCharacteristics = cc;
//        StreamConfigurationMap streamConfigs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//
//        // determine supported output formats..
//        boolean supportsRaw = false, supportsJpeg = false, supportsYUV = false;
//        for (int format : streamConfigs.getOutputFormats()) {
//            if (format == ImageFormat.RAW_SENSOR) {
//                supportsRaw = true;
//            } else if (format == ImageFormat.JPEG) {
//                supportsJpeg = true;
//            } else if (format == ImageFormat.YUV_420_888) {
//                supportsYUV = true;
//            }
//        }
//        if (supportsRaw) {
//            mCaptureImageFormat = ImageFormat.RAW_SENSOR;
//        } else if (supportsJpeg) {
//            mCaptureImageFormat = ImageFormat.JPEG;
//        } else {
//            throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "Couldn't find supported image format");
//        }
//
//        Log.d(TAG, "Supports YUV? " + supportsYUV + ", Supports Jpeg? " + supportsJpeg);
//
//        // alternatively, make a way for the user to select a capture size..
//        //Size rawSize = streamConfigs.getOutputSizes(ImageFormat.RAW_SENSOR)[0];
//        Size jpegSize = streamConfigs.getOutputSizes(ImageFormat.JPEG)[0];
//        Size yuvSize = streamConfigs.getOutputSizes(ImageFormat.YUV_420_888)[0];
//
//        // find the preview size that best matches the aspect ratio of the camera sensor..
//        Size[] previewSizes = streamConfigs.getOutputSizes(SurfaceTexture.class);
//        mPreviewSize = findOptimalPreviewSize(previewSizes, yuvSize.getWidth(), yuvSize.getHeight());
//        if (mPreviewSize == null) {
//            return;
//        }
//
//        // set up capture surfaces and image readers..
//        mPreviewSurface = new Surface(surface);
////        ImageReader rawReader = ImageReader.newInstance(rawSize.getWidth(), rawSize.getHeight(),
////                ImageFormat.RAW_SENSOR, 1);
////
////
////        //add the listener
////        rawReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
////
////            @Override
////            public void onImageAvailable(ImageReader reader) {
////               Log.d(TAG, "<<<<<<<<<<<RAW IMAGE AVAILABLE>>>>>>>>>>>>");
////            }
////
////        }, null);
//
//
//        //mRawCaptureSurface = rawReader.getSurface();
//
//
//        mMainImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
//                ImageFormat.YUV_420_888, 1);
//        mMainImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//            @Override
//            public void onImageAvailable(ImageReader reader) {
//                //reader.close();
//
//                Image image = reader.acquireLatestImage();
//
//                Log.d(TAG, "--------YUV IMAGE AVAILABLE--(" + image.getWidth() + ", " + image.getHeight() + ")------ ");
//
//                //canvas = reader.getSurface().lockCanvas(null);
//
//
//                Mat originalMat = getMatFromImage(image).t();
//
//                Thread i_thread = new Thread(new ImageProc(reader.getSurface(), originalMat));
//                i_thread.start();
//
//                image.close();
//            }
//        }, null);
//        mYUVCaptureSurface = mMainImageReader.getSurface();
//
//        try {
//            cm.openCamera(cameraId, new CameraDevice.StateCallback() {
//                @Override
//                public void onOpened(@NonNull CameraDevice camera) {
//                    mCamera = camera;
//                    Log.d(TAG, "<<><<<<<<<CAMERA OPENED>>>>>>>><<>>");
//                    initPreview();
//                }
//
//                @Override
//                public void onDisconnected(@NonNull CameraDevice camera) {
//
//                }
//
//                @Override
//                public void onError(@NonNull CameraDevice camera, int error) {
//
//                }
//            }, null);
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void initPreview() {
//        // scale preview size to fill screen width
//        int screenWidth = getResources().getDisplayMetrics().widthPixels;
//        float previewRatio = mPreviewSize.getWidth() / ((float) mPreviewSize.getHeight());
//        int previewHeight = Math.round(screenWidth * previewRatio);
//        LayoutParams params = mPreviewView.getLayoutParams();
//        params.width = screenWidth;
//        params.height = previewHeight;
//
//        List<Surface> outputSurfaces = Arrays.asList(mPreviewSurface, mYUVCaptureSurface);
////        List<Surface> outputSurfaces = new LinkedList<>();
////        outputSurfaces.add(mMainImageReader.getSurface());
//
//        try {
//            mCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    mSession = session;
//                    updatePreview();
//                    Log.e(TAG, "Configuration SUCCESS >>>>><<<<<<>><<>><<");
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//                    Log.e(TAG, "Configuration FAILED >>>>><<<<<<>><<>><<");
//
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            Log.d(TAG, "Failed to create camera capture session", e);
//        }
//    }
//
//    /**
//     * Call this whenever some camera control changes (e.g., focus distance, white balance, etc) that should affect the preview
//     */
//    private void updatePreview() {
//        try {
//            if (mCamera == null || mSession == null) {
//                return;
//            }
//            CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            builder.addTarget(mPreviewSurface);
//            //MOST IMPORTANT PART
//            builder.addTarget(mMainImageReader.getSurface());
//
//            builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
//
////            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, ...)
////            builder.set(CaptureRequest.SENSOR_SENSITIVITY, ...)
////            builder.set(CaptureRequest.CONTROL_AWB_MODE, ...)
////            builder.set(CaptureRequest.CONTROL_EFFECT_MODE, ...)
////            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ...)
////            etc...
//
//            mSession.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    // if desired, we can get updated auto focus & auto exposure values here from 'result'
//                    Log.d(TAG, "<><><><///////><CAPTURE COMPLETED");
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            Log.e(TAG, "Failed to start preview");
//        }
//    }
//
//
//    public Mat getMatFromImage(Image image) {
//        ByteBuffer buffer;
//        int rowStride;
//        int pixelStride;
//        int width = image.getWidth();
//        int height = image.getHeight();
//        int offset = 0;
//
//        Image.Plane[] planes = image.getPlanes();
//        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
//        byte[] rowData = new byte[planes[0].getRowStride()];
//
//        for (int i = 0; i < planes.length; i++) {
//            buffer = planes[i].getBuffer();
//            rowStride = planes[i].getRowStride();
//            pixelStride = planes[i].getPixelStride();
//            int w = (i == 0) ? width : width / 2;
//            int h = (i == 0) ? height : height / 2;
//            for (int row = 0; row < h; row++) {
//                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
//                if (pixelStride == bytesPerPixel) {
//                    int length = w * bytesPerPixel;
//                    buffer.get(data, offset, length);
//
//                    // Advance buffer the remainder of the row stride, unless on the last row.
//                    // Otherwise, this will throw an IllegalArgumentException because the buffer
//                    // doesn't include the last padding.
//                    if (h - row != 1) {
//                        buffer.position(buffer.position() + rowStride - length);
//                    }
//                    offset += length;
//                } else {
//
//                    // On the last row only read the width of the image minus the pixel stride
//                    // plus one. Otherwise, this will throw a BufferUnderflowException because the
//                    // buffer doesn't include the last padding.
//                    if (h - row == 1) {
//                        buffer.get(rowData, 0, width - pixelStride + 1);
//                    } else {
//                        buffer.get(rowData, 0, rowStride);
//                    }
//
//                    for (int col = 0; col < w; col++) {
//                        data[offset++] = rowData[col * pixelStride];
//                    }
//                }
//            }
//        }
//
//        // Finally, create the Mat.
//        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC3);
//        mat.put(0, 0, data);
//
//        return mat;
//    }
//
//
//    //all thread stuff happens here
//    public class ImageProc implements Runnable {
//        Mat m0;
//        Surface s0;
//
//        public ImageProc(Surface _s, Mat _m) {
//            m0 = _m;
//            s0 = _s;
//        }
//
//        @Override
//        public void run() {
//            Mat m = new Mat(m0.rows(), m0.cols(), CvType.CV_8UC3);
//
//            Imgproc.cvtColor(m0, m, Imgproc.COLOR_YUV2BGR);
//
//            if (!m.empty()) {
//                Center c = targetFinder.findOneRetroTarget(m);
//
////
////                }
////                else {
////                    Log.e(TAG, "Invalid Surface");
////                }
//
//
//                Log.d(TAG, "CENTER : (X , Y) : (" + Math.round(c.x) + ", " + Math.round(c.y) + ")");
//
//
////                if (!c.equals(TargetFinder.NO_CENTER)) {
////                    Imgproc.line(originalMat, new Point(c.x + 30, c.y), new Point(c.x - 30, c.y), new Scalar(0, 255, 0), 3);
////                    Imgproc.line(originalMat, new Point(c.x, c.y + 30), new Point(c.x, c.y - 30), new Scalar(0, 255, 0), 3);
////                }
////
////                else {
////                    Imgproc.putText(originalMat, "No Target Detected", new Point(10, 10), Core.FONT_HERSHEY_PLAIN, 2, new Scalar(255,0,0));
////                }
////
////                Bitmap bmp = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.ARGB_8888);
//            } else {
//                Log.e(TAG, "***INVALID MAT***");
//            }
//
//            m0.release();
//            m.release();
//
//        }
//    }
//
////old runnable stuff for canvas
//
////if (s0 != null && s0.isValid()){
////
////                    try {
////                        //do this to avoid any conflcits
////                        //synchronized (mPreviewSurface) {
//////                            canvas = s0.lockCanvas(null);
//////
//////                            if (canvas != null) {
//////                                Log.d(TAG + ": CANVAS", "NO ERRORS and processing canvas");
//////
//////                                Imgproc.rectangle(m0, new Point(5, 5), new Point(100, 100), new Scalar(255, 0, 0), 2);
//////
//////
//////                                Bitmap bmp = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
//////                                Utils.matToBitmap(m, bmp);
//////                                canvas.drawBitmap(bmp, 0, 0, null);
//////
//////                            }
//////                            s0.unlockCanvasAndPost(canvas);
////
//////                            if (canvas != null && s0 != null) {
//////                                s0.unlockCanvasAndPost(canvas);
//////                            }
////                        }
////                    }catch(IllegalStateException e){
////                        Log.e(TAG, ">>>tried to lock canvas when it was already locked");
////                    }
//
//
//    class CompareSizesByArea implements Comparator<Size> {
//
//        @Override
//        public int compare(Size e1, Size e2) {
//            int a1 = e1.getWidth() * e1.getHeight(), a2 = e2.getHeight() * e2.getWidth();
//            if (a1 < a2) {
//                return -1;
//            } else if (a1 > a2) {
//                return 1;
//            } else {
//                return 0;
//            }
//
//        }
//    }
//
//    /**
//     * Given a target size for raw output, search available preview sizes for one with a similar
//     * aspect ratio that does not exceed screen size.
//     */
//    private Size findOptimalPreviewSize(Size[] sizes, int width, int height) {
//
//        double GOOD_RATIO = (double) width / height;
//
//        int maxArea = width * height;
//        int smallestIndex = 0;
//
//        for (int i = 0; i < sizes.length; i++) {
//            int w = sizes[i].getWidth(), h = sizes[i].getHeight();
//            Log.d(TAG + ": All Sizes", "@Index " + i + ": " + w + "x" + h);
//
//            double ratio_W2H = (double) w / h;
//            if (w * h < maxArea
//                    && ratio_W2H == GOOD_RATIO) {
//                maxArea = w * h;
//                smallestIndex = i;
//            }
//        }
//
//        Log.d(TAG, "Chosen Index: " + smallestIndex);
//        return sizes[smallestIndex];
//
//        // Collect the supported resolutions that are at least as big as the preview Surface
////        List<Size> bigEnough = new ArrayList<>();
////        int w = targetSize.getWidth();
////        int h = targetSize.getHeight();
////        for (Size option : sizes) {
////            if (option.getHeight() == option.getWidth() * h / w &&
////                    option.getWidth() >= width && option.getHeight() >= height) {
////                bigEnough.add(option);
////            }
////        }
////
////        // Pick the smallest of those, assuming we found any
////        if (bigEnough.size() > 0) {
////            Size size = Collections.min(bigEnough, new CompareSizesByArea());
////            return Collections.min(bigEnough, new CompareSizesByArea());
////        } else {
////            Log.e(TAG, "Couldn't find any suitable preview size");
////
////            return sizes[0];
////        }
//
//
////        float targetRatio = targetSize.getWidth() * 1.0f / targetSize.getHeight();
////        float tolerance = 0.1f;
////        int screenWidth = getResources().getDisplayMetrics().widthPixels;
////        int maxPixels = screenWidth * Math.round(screenWidth * targetRatio);
////        int width, height;
////        float ratio;
////        for (Size size : sizes) {
////            width = size.getWidth();
////            height = size.getHeight();
////            if (width * height <= maxPixels) {
////                ratio = ((float) width) / height;
////                if (Math.abs(ratio - targetRatio) < tolerance) {
////                    return size;
////                }
////            }
////        }
////        return null;
//    }
//}

//}
//
////    private final static String TAG = "SimpleCamera";
////
////    public TargetFinder targetFinder;
////    private TextureView mTextureView = null;
////    private Size mPreviewSize = null;
////    private CameraDevice mCameraDevice = null;
////    private CaptureRequest.Builder mPreviewBuilder = null;
////    private CameraCaptureSession mPreviewSession = null;
////
////    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
////
////        @Override
////        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
////            // TODO Auto-generated method stub
////            //Log.i(TAG, "onSurfaceTextureUpdated()");
////
////        }
////
////        @Override
////        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
////                                                int height) {
////            // TODO Auto-generated method stub
////            Log.i(TAG, "onSurfaceTextureSizeChanged()");
////
////        }
////
////        @Override
////        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
////            // TODO Auto-generated method stub
////            Log.i(TAG, "onSurfaceTextureDestroyed()");
////            return false;
////        }
////
////        @Override
////        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
////                                              int height) {
////            // TODO Auto-generated method stub
////            Log.i(TAG, "onSurfaceTextureAvailable()");
////
////            CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
////
////
////
////
////            try{
////                String cameraId = manager.getCameraIdList()[0];
////                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
////                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
////                mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
////
////                manager.openCamera(cameraId, mStateCallback, null);
////
////
////
////                //mImageWriter = ImageWriter.newInstance(new Surface(surface), 50);
////
////            }
////            catch(CameraAccessException e)
////            {
////                e.printStackTrace();
////            }
////
////        }
////    };
////
////    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
////
////        @Override
////        public void onOpened(CameraDevice camera) {
////            // TODO Auto-generated method stub
////            Log.i(TAG, "onOpened");
////            mCameraDevice = camera;
////
////
////            SurfaceTexture texture = mTextureView.getSurfaceTexture();
////            if (texture == null) {
////                Log.e(TAG, "texture is null");
////                return;
////            }
////
////            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
////            Surface surface = new Surface(texture);
////
////            try {
////                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
////
////
////                mPreviewBuilder.addTarget(surface);
////
////                CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
////                String cameraId = manager.getCameraIdList()[0];
////                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
////                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
////
////                Size largest = Collections.max(
////                        Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
////                        new CompareSizesByArea());
////
////                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
////                        ImageFormat.YUV_420_888, /*maxImages*/2);
////                mImageReader.setOnImageAvailableListener(
////                        mOnImageAvailableListener, mBackgroundHandler);
////
////            } catch (CameraAccessException e) {
////                e.printStackTrace();
////            }
////
//////            try {
//////                mCameraDevice.createCaptureSession(Arrays.asList(surface), mPreviewStateCallback, null);
//////            } catch (CameraAccessException e) {
//////                e.printStackTrace();
//////            }
//////
//////
////            try{
////
////                // Here, we create a CameraCaptureSession for camera preview.
////                mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
////                        new CameraCaptureSession.StateCallback() {
////
////                            @Override
////                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
////                                // The camera is already closed
////                                if (null == mCameraDevice) {
////                                    return;
////                                }
////
////                                // When the session is ready, we start displaying the preview.
////                                mPreviewSession = cameraCaptureSession;
////                                try {
////                                    // Auto focus should be continuous for camera preview.
////                                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
////                                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
////                                    // Flash is automatically enabled when necessary.
////
////                                    // Finally, we start displaying the camera preview.
////                                    mPreviewRequest = mPreviewBuilder.build();
////                                    mPreviewSession.setRepeatingRequest(mPreviewRequest,
////                                            mCaptureCallback, mBackgroundHandler);
////                                } catch (CameraAccessException e) {
////                                    e.printStackTrace();
////                                }
////                            }
////
////
////                            @Override
////                            public void onConfigureFailed(
////                                    @NonNull CameraCaptureSession cameraCaptureSession) {
////
////                            }
////                        }, null);
////                    }
////
////
////
////
////                catch (CameraAccessException e){
////                    e.printStackTrace();
////                }
////
////
////
////        }
////
////
////
////        @Override
////        public void onError(CameraDevice camera, int error) {
////            // TODO Auto-generated method stub
////            Log.e(TAG, "onError");
////
////        }
////
////        @Override
////        public void onDisconnected(CameraDevice camera) {
////            // TODO Auto-generated method stub
////            Log.e(TAG, "onDisconnected");
////
////        }
////    };
////
////    private CameraCaptureSession.StateCallback mPreviewStateCallback = new CameraCaptureSession.StateCallback() {
////
////        @Override
////        public void onConfigured(CameraCaptureSession session) {
////            // TODO Auto-generated method stub
////            Log.i(TAG, "onConfigured");
////            mPreviewSession = session;
////
////            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
////
////            HandlerThread backgroundThread = new HandlerThread("CameraPreview");
////            backgroundThread.start();
////            Handler mBackgroundHandler = new Handler(backgroundThread.getLooper());
////
////            try {
////                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
////            } catch (CameraAccessException e) {
////                e.printStackTrace();
////            }
////
////        }
////
////        @Override
////        public void onConfigureFailed(CameraCaptureSession session) {
////            // TODO Auto-generated method stub
////            Log.e(TAG, "CameraCaptureSession Configure failed");
////        }
////    };
////
////    private ImageReader mImageReader;
////    private ImageWriter mImageWriter;
////    private Handler mBackgroundHandler;
////
////    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
////            = new ImageReader.OnImageAvailableListener() {
////
////        @Override
////        public void onImageAvailable(ImageReader reader) {
////            Image im = reader.acquireNextImage();
////            Mat m = getMatFromImage(im);
////            im.close();
////
////            Center c = targetFinder.findOneRetroTarget(m);
////
////            Log.d(TAG, "FOUND AN IMAGGEEEEEEE, Center: " + Math.round(c.x) + ", " + Math.round(c.y));
////
////            //Image im2 = getImageFromMat(n);
////
////            //mImageWriter.queueInputImage(im2);
////
////        }
////
////    };
////
////    public Mat getMatFromImage(Image image){
////        Mat buf = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
////
////        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
////        byte[] bytes = new byte[buffer.remaining()];
////        buffer.get(bytes);
////        buf.put(0, 0, bytes);
////
////// Do note that Highgui has been replaced by Imgcodecs for OpenCV 3.0 and above
////        Mat mat = Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR);
////
////        return mat;
////    }
////
//////    public Image getImageFromMat(Mat mat){
//////
//////        Bitmap bmp = null;
//////        bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
//////        Utils.matToBitmap(mat, bmp);
//////
//////    }
////
////
////
////    private CameraCaptureSession.CaptureCallback mCaptureCallback
////            = new CameraCaptureSession.CaptureCallback() {
////
////        @Override
////        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
////                                       TotalCaptureResult result) {
////            Bitmap barcodeBmp = mTextureView.getBitmap();
////
////            Log.d(TAG, "onCaptureCompleted YOOOOOOOOOO HOOOOOOO");
////        }
////
////    };
////
////
////
////
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        requestWindowFeature(Window.FEATURE_NO_TITLE);
////        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
////        //same as set-up android:screenOrientation="portrait" in <activity>, AndroidManifest.xml
////        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
////        setContentView(R.layout.activity_main);
////
////        //Check if opencv is working
////        if (!OpenCVLoader.initDebug()) {
////            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
////        } else {
////            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
////        }
////
////        targetFinder = new TargetFinder();
////
////        mTextureView = (TextureView) findViewById(R.id.textureView1);
////        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
////    }
////
////    @Override
////    protected void onPause() {
////        // TODO Auto-generated method stub
////        super.onPause();
////
////        if (mCameraDevice != null)
////        {
////            mCameraDevice.close();
////            mCameraDevice = null;
////        }
////    }
////
////
////
////    static class CompareSizesByArea implements Comparator<Size> {
////
////        @Override
////        public int compare(Size lhs, Size rhs) {
////            // We cast here to ensure the multiplications won't overflow
////            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
////                    (long) rhs.getWidth() * rhs.getHeight());
////        }
////
////    }
////
////    private CaptureRequest mPreviewRequest;
////
////
////
////}




