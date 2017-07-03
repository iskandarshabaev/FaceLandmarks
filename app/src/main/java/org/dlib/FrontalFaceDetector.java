package org.dlib;

import android.graphics.Bitmap;

public class FrontalFaceDetector {

    static {
        System.loadLibrary("native-lib");
    }

    public native FullObjectDetection findLandmarks();

    public native void initFrontalFaceDetector();

    public native byte[] detectFaces(Bitmap bitmap);

    public native byte[] detectLandmarksFromFace(Bitmap bitmap,
                                                  long left,
                                                  long top,
                                                  long right,
                                                  long bottom);


    public native byte[] detectLandmarksFromFaces(Bitmap bitmap,
                                                   byte[] faceBounds);
}
