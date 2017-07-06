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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView image;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image = (ImageView) findViewById(R.id.image);
        mHandler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                find();
            }
        }).start();
    }

    private void find() {
        FrontalFaceDetector frontalFaceDetector = new FrontalFaceDetector();
        try {
            File f = new File("sdcard/shape_predictor_68_face_landmarks.dat");
            if (!f.exists()) {
                InputStream is = getAssets().open("shape_predictor_68_face_landmarks.dat");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(buffer);
                fos.close();
            }
            frontalFaceDetector.initFrontalFaceDetector("sdcard/shape_predictor_68_face_landmarks.dat");
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

            Bitmap re = Bitmap.createScaledBitmap(photo, (int) (photo.getWidth() / 1.9), (int) (photo.getHeight() / 1.9), false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    image.setImageBitmap(d);
                }
            });
            for (int i = 0; i < 100; i++) {
                long time1 = System.nanoTime();
                final int[] bounds = frontalFaceDetector.detectLandmarksFromFace(re);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        canvas.drawRect(bounds[0],bounds[1],bounds[2],bounds[3], p);
                        image.setImageBitmap(d);
                    }
                });
                long time2 = System.nanoTime();
                Log.d("bench", "" + (time2 - time1) / 1000 / 1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
