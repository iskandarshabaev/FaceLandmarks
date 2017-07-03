package com.ishabaev.facelandmarksdetection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.dlib.FrontalFaceDetector;
import org.dlib.FullObjectDetection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.sample_text);
        FrontalFaceDetector frontalFaceDetector = new FrontalFaceDetector();
        frontalFaceDetector.initFrontalFaceDetector();
        FullObjectDetection detection = frontalFaceDetector.findLandmarks();
    }
}
