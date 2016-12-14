package org.opencv.samples.colorblobdetect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.SurfaceView;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static org.opencv.core.Core.FILLED;
import static org.opencv.core.Core.merge;
import static org.opencv.core.Core.split;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2HSV;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_CCOMP;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.morphologyEx;

public class ColorBlobDetectionActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private boolean mIsColorSelected = false;
    private Mat mRgba;
    private Scalar mBlobColorRgba;
    private Scalar mBlobColorHsv;
    private ColorBlobDetector mDetector;
    private Mat mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;

    //lijia
    DrawOnTop mDraw;
    float mLeft = 300;
    float mRight = 300;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };
    private float mLeft1;
    private float mRight2;

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.color_blob_detection_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //lijia
        mDraw = new DrawOnTop(this);




        addContentView(mDraw, new ViewGroup.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    class DrawOnTop extends View {

        private int mCnt = 0;
        private float mLefttmp = 0;
        private float mRighttmp = 0;

        public DrawOnTop(Context context) {
            super(context);
// TODO Auto-generated constructor stub
        }

        @Override
        protected void onDraw(Canvas canvas) {
// TODO Auto-generated method stub

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            canvas.drawText("Test Text", mLeft, mRight, paint);
            canvas.drawRect(300, 300, 500, 500, paint);

            canvas.drawLine(400, 400, 600, 600, paint);

            Resources r = this.getContext().getResources();
            Drawable drawale = r.getDrawable(R.drawable.icon);


                Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                Canvas cas = new Canvas(bitmap);
                drawale.setBounds(0, 0, 50, 50);
                drawale.draw(cas);

            float x, y;
            x = abs(mLeft1 - mLefttmp);
            y = abs(mRight2 - mRighttmp);
            if ((x > 10) && (y > 10)) {
                mLefttmp = mLeft1;
                mRighttmp = mRight2;
            }
            canvas.drawBitmap(bitmap, mLefttmp, mRighttmp, paint);
            canvas.drawText("Target!!!", mLefttmp, mRighttmp, paint);

                //canvas.drawBitmap(bitmap, mLeft1, mRight2, paint);



            invalidate();

            super.onDraw(canvas);
        }

        public void invalidateDrawable (Drawable who) {
                        Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            //who.drawText("Test Text", 10, 10, paint);


        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int) event.getX() - xOffset;
        int y = (int) event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x > 4) ? x - 4 : 0;
        touchedRect.y = (y > 4) ? y - 4 : 0;

        touchedRect.width = (x + 4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y + 4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();


          if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());

            ///Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

              //lijia
              List<MatOfPoint> contours_max;
              contours_max = GetMaxContour(contours, 50);
              Imgproc.drawContours(mRgba, contours_max, -1, CONTOUR_COLOR);

              Mat colorLabel = mRgba.submat(4, 68, 4, 68);
              colorLabel.setTo(mBlobColorRgba);

              Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
              mSpectrum.copyTo(spectrumLabel);
        }

        //mRgba = aaa(mRgba);

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


     Mat mycolor(Mat imgOriginal) {

         int iLowH = 100;
         int iHighH = 140;

         int iLowS = 90;
         int iHighS = 255;

         int iLowV = 90;
         int iHighV = 255;

         //Create trackbars in "Control" window
         //cvCreateTrackbar("LowH", "Control", & iLowH, 179); //Hue (0 - 179)
         //cvCreateTrackbar("HighH", "Control", & iHighH, 179);

         //cvCreateTrackbar("LowS", "Control", & iLowS, 255); //Saturation (0 - 255)
         //cvCreateTrackbar("HighS", "Control", & iHighS, 255);

         //cvCreateTrackbar("LowV", "Control", & iLowV, 255); //Value (0 - 255)
         //cvCreateTrackbar("HighV", "Control", & iHighV, 255);


         Mat imgHSV = new Mat();
         Vector<Mat> hsvSplit = new Vector<Mat>();
         Imgproc.cvtColor(imgOriginal, imgHSV, COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV

         //因为我们读取的是彩色图，直方图均衡化需要在HSV空间做
         split(imgHSV, hsvSplit);
         Imgproc.equalizeHist(hsvSplit.elementAt(2), hsvSplit.elementAt(2));

         merge(hsvSplit, imgHSV);
         Mat imgThresholded = new Mat();

         Scalar s1 = new Scalar(0.8);
         Scalar s2 = new Scalar(1.2);


         Core.inRange(imgHSV,mBlobColorHsv.mul(s1), mBlobColorHsv.mul(s2) , imgThresholded); //Threshold the image

         //开操作 (去除一些噪点)
         Size sz = new Size(5, 5);
         Mat element = Imgproc.getStructuringElement(MORPH_RECT, sz);


         morphologyEx(imgThresholded, imgThresholded, MORPH_OPEN, element);

         //闭操作 (连接一些连通域)
         morphologyEx(imgThresholded, imgThresholded, MORPH_CLOSE, element);

         ///imshow("Thresholded Image", imgThresholded); //show the thresholded image
         ///imshow("Original", imgOriginal); //show the original image


         return imgThresholded;
     }

    List<MatOfPoint> GetMaxContour(List<MatOfPoint> contours, double minArea) {
        // Find max contour area
        List<MatOfPoint> contours_max = new ArrayList<MatOfPoint>();
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        MatOfPoint max = new MatOfPoint();
        Point[] p;
        List<Point> p2 = new ArrayList<Point>();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) {
                maxArea = area;
                max = wrapper;
                p = max.toArray();
                p2 = max.toList();
            }
        }
        if (maxArea < minArea)
            return null;

        contours_max.add(max);

        Iterator<Point> each_point = p2.iterator();
        Point p3;
        double sum_x = 0;
        double sum_y = 0;
        while(each_point.hasNext()) {
            p3 = each_point.next();
            sum_x += p3.x;
            sum_y += p3.y;
        }
        mLeft1 = (float)sum_x / p2.size() + 250; //adjust location;
        mRight2 = (float)sum_y / p2.size();

        return contours_max;
    }

    Mat aaa(Mat image) {
        Mat imageShold = new Mat();
        Mat hsv = new Mat();


        ///Imgproc.pyrDown(image, imageShold);
        ///Imgproc.pyrDown(imageShold, imageShold);
        ///Imgproc.cvtColor(imageShold, hsv, Imgproc.COLOR_RGB2HSV_FULL);
        //Core.inRange(mHsvMat, mLowerBound, mUpperBound, mMask);

        Imgproc.cvtColor(image, imageShold, COLOR_BGR2GRAY);
        Imgproc.threshold(imageShold, imageShold, 60, 255, THRESH_BINARY);

        Mat element = new Mat(5,5,CV_8U, new Scalar(1));
        Mat eroded = new Mat();
        morphologyEx(imageShold,eroded,MORPH_OPEN,element);

        Mat dilated = new Mat();
        dilate(eroded,dilated, new Mat());
        erode(dilated,dilated,new Mat());

        //dilated.convertTo(dilated, CV_8UC1);

        //List<MatOfPoint> contours = new LinkedList<MatOfPoint>();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        //Imgproc.threshold(dilated, dilated, 60, 255, THRESH_BINARY);

        Imgproc.findContours(dilated, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_NONE);
        ///Mat mHierarchy = new Mat();
        ///Imgproc.findContours(dilated, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        ///Mat result = new Mat(image.size(),CV_8U, new Scalar(255));

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        MatOfPoint max = new MatOfPoint();
        Point[] p;
        List<Point> p2;
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea) {
                maxArea = area;
                max = wrapper;
                p = max.toArray();
                p2 = max.toList();
                int a = 8;
                mLeft1 = (float)p[0].x;
                mRight2 = (float)p[0].y;

            }

        }

        List<MatOfPoint> contours_max = new ArrayList<MatOfPoint>();
        contours_max.add(max);

        Imgproc.drawContours(image, contours_max, -1, new Scalar(0, 0, 255), FILLED);

        return image;
    }



}
