package com.ishabaev.facelandmarksdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.dlib.FrontalFaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.sample_text);
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
            photo = Bitmap.createScaledBitmap(photo, (int)(photo.getWidth()/1.9), (int)(photo.getHeight()/1.9), false);
            for (int i = 0; i < 100; i++) {
                long time1 = System.nanoTime();
                frontalFaceDetector.detectLandmarksFromFace(photo);
                long time2 = System.nanoTime();
                Log.d("bench", "" + (time2 - time1)/1000/1000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
