package com.ishabaev.facelandmarksdetection;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.dlib.FrontalFaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback {

    FrontalFaceDetector frontalFaceDetector;
    Canvas canvas;
    Bitmap imageBitmap;
    private ImageView image;
    private Handler mHandler;
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private boolean ready;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    private volatile boolean handled = true;
    private Paint mPaint;

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);

        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mHandler = new Handler(Looper.getMainLooper());
        frontalFaceDetector = new FrontalFaceDetector();

        imageBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(imageBitmap);

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);

        if (!frontalFaceDetector.isLandmarksLoaded()) {
            frontalFaceDetector.loadLandmarks(this, new FrontalFaceDetector.Callbacks() {
                @Override
                public void onSuccess() {
                    start();
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        } else {
            start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;

        int previewSurfaceWidth = preview.getWidth();
        int previewSurfaceHeight = preview.getHeight();

        ViewGroup.LayoutParams lp = preview.getLayoutParams();

        // здесь корректируем размер отображаемого preview, чтобы не было искажений

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            // портретный вид
            camera.setDisplayOrientation(90);
            lp.height = previewSurfaceHeight;
            lp.width = (int) (previewSurfaceHeight / aspect);
        } else {
            // ландшафтный
            camera.setDisplayOrientation(0);
            lp.width = previewSurfaceWidth;
            lp.height = (int) (previewSurfaceWidth / aspect);
        }
        preview.setLayoutParams(lp);
        image.setLayoutParams(lp);
        imageBitmap = Bitmap.createBitmap(previewSurfaceWidth, previewSurfaceHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(imageBitmap);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera paramCamera) {
        if (handled) {
            handled = false;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Camera.Size size = paramCamera.getParameters().getPreviewSize();
            detect(YUV_toRGB(data, size.width, size.height));
        }
    }

    public Bitmap YUV_toRGB(byte[] yuv, int W, int H) {

        if (rs == null || yuvToRgbIntrinsic == null) {
            rs = RenderScript.create(this);
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        }
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuv.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(W).setY(H);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(yuv);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        out.copyTo(bmp);

        out.destroy();
        in.destroy();

        return RotateImage(bmp, -90);
    }

    private Bitmap RotateImage(Bitmap _bitmap, int angle) {

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        _bitmap = Bitmap.createBitmap(_bitmap, 0, 0, _bitmap.getWidth(), _bitmap.getHeight(), matrix, true);
        return _bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //camera = Camera.open();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(camIdx);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void detect(final Bitmap bitmap) {
        if (ready) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    long time1 = System.nanoTime();
                    final int[][][] faces = frontalFaceDetector.detectLandmarksFromFace(bitmap);
                    if (faces.length > 0) {
                        Log.d("ssdfsd", "fadsfsdffasdfds");
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                image.setImageBitmap(imageBitmap);
                            }
                        });
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            canvas.drawBitmap(bitmap, 0, 0, null);
                            for (int[][] face : faces) {
                                for (int[] point : face) {
                                    canvas.drawCircle((float) point[0], (float) point[1], 4.f, mPaint);
                                }
                            }
                            image.setImageBitmap(imageBitmap);
                        }
                    });
                    long time2 = System.nanoTime();
                    Log.d("bench", "" + (time2 - time1) / 1000 / 1000);
                    handled = true;
                }
            }).start();
        }
    }

    private void start() {
        frontalFaceDetector.initDetector();
        ready = true;
    }
}
