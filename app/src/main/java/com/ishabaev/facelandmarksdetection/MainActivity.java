package com.ishabaev.facelandmarksdetection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
        FrontalFaceDetector frontalFaceDetector = new FrontalFaceDetector();
        try {
            File f = new File("sdcard/shape_predictor_68_face_landmarks.dat");
            if(!f.exists()) {
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
            photo = Bitmap.createScaledBitmap(photo, 250, 250, false);
            byte[] d = frontalFaceDetector.detectLandmarksFromFace(photo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
