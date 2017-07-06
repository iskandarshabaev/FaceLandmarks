package org.dlib;

import android.graphics.Bitmap;

public class FrontalFaceDetector {

    static {
        System.loadLibrary("native-lib");
    }

    public native void initFrontalFaceDetector(String path);

    public native byte[] detectLandmarksFromFace(Bitmap bitmap);

}
