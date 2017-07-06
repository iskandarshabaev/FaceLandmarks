package com.ishabaev.facelandmarksdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.dlib.FrontalFaceDetector;

public class MainActivity extends AppCompatActivity {

    FrontalFaceDetector frontalFaceDetector;
    private ImageView image;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);
        mHandler = new Handler(Looper.getMainLooper());
        frontalFaceDetector = new FrontalFaceDetector();
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

    private void start() {
        frontalFaceDetector.initDetector();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap photo = BitmapFactory.decodeResource(getResources(),
                        R.drawable.photo);
                final Bitmap d = photo.copy(Bitmap.Config.ARGB_8888, true);
                final Canvas canvas = new Canvas(d);
                final Paint p = new Paint();
                p.setStyle(Paint.Style.STROKE);
                p.setAntiAlias(true);
                p.setFilterBitmap(true);
                p.setDither(true);
                p.setColor(Color.RED);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageBitmap(d);
                    }
                });
                for (int i = 0; i < 100; i++) {
                    long time1 = System.nanoTime();
                    final int[] bounds = frontalFaceDetector.detectLandmarksFromFace(d);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            canvas.drawRect(bounds[0], bounds[1], bounds[2], bounds[3], p);
                            image.setImageBitmap(d);
                        }
                    });
                    long time2 = System.nanoTime();
                    Log.d("bench", "" + (time2 - time1) / 1000 / 1000);
                }
            }
        }).start();
    }
}
