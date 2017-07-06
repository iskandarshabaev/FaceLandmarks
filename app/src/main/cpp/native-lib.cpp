#include <jni.h>
#include <string>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/image_io.h>
#include <android/bitmap.h>

using namespace dlib;
using namespace std;

#define JNI_METHOD(NAME) \
    Java_org_dlib_FrontalFaceDetector_##NAME


void convertBitmapToArray2d(JNIEnv *env,
                            jobject bitmap,
                            array2d<rgb_pixel> &out) {
    AndroidBitmapInfo bitmapInfo;
    void *pixels;
    int state;
    if (0 > (state = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo))) {
        return;
    } else if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
    }
    if (0 > (state = AndroidBitmap_lockPixels(env, bitmap, &pixels))) {
        return;
    }
    out.set_size((long) bitmapInfo.height, (long) bitmapInfo.width);
    char *line = (char *) pixels;
    for (int h = 0; h < bitmapInfo.height; ++h) {
        for (int w = 0; w < bitmapInfo.width; ++w) {
            uint32_t *color = (uint32_t *) (line + 4 * w);
            out[h][w].red = (unsigned char) (0xFF & ((*color) >> 24));
            out[h][w].green = (unsigned char) (0xFF & ((*color) >> 16));
            out[h][w].blue = (unsigned char) (0xFF & ((*color) >> 8));
        }
        line = line + bitmapInfo.stride;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}

void resize_image_with_ratio(array2d<rgb_pixel> &in, array2d<rgb_pixel> &out,
                             int max_width, int max_height) {
    float ratio = 0;
    int width = in.nc();
    int height = in.nr();

    int s_witdh = max_width;
    int s_height = max_height;

    if (width > max_width) {
        ratio = (float) max_width / (float) width;
        s_witdh = (int)(width * ratio);
        s_height = (int) (height * ratio);
    }
    if (height > max_height) {
        ratio = (float) max_height / (float) height;
        s_witdh = (int)(width * ratio);
        s_height = (int) (height * ratio);
    }
    out.set_size(s_height, s_witdh);
    resize_image(in, out);
}

frontal_face_detector sFaceDetector;
shape_predictor sp;

extern "C"
JNIEXPORT jobject JNICALL
JNI_METHOD(findLandmarks)(JNIEnv *env, jobject obj) {
    array2d<rgb_pixel> img;
    pyramid_up(img);
    jclass cls = env->FindClass("org/dlib/FullObjectDetection");
    jmethodID cid = env->GetMethodID(cls, "<init>", "()V");
    return env->NewObject(cls, cid);
}

extern "C"
JNIEXPORT void JNICALL
JNI_METHOD(initFrontalFaceDetector)(JNIEnv *env, jobject obj, jstring path) {
    sFaceDetector = get_frontal_face_detector();
    const char *nativeString = env->GetStringUTFChars(path, 0);
    deserialize(nativeString) >> sp;
    env->ReleaseStringUTFChars(path, nativeString);
}

extern "C"
JNIEXPORT jobjectArray JNICALL
JNI_METHOD(detectLandmarksFromFace)(JNIEnv *env, jobject obj, jobject bitmap) {
    try {
        array2d<rgb_pixel> img;
        convertBitmapToArray2d(env, bitmap, img);
        array2d<rgb_pixel> resizedImg;
        resize_image_with_ratio(img, resizedImg, 160, 160);
        array2d<unsigned char> img_gray;
        assign_image(img_gray, resizedImg);
        //pyramid_up(img);
        std::vector<rectangle> dets = sFaceDetector(img_gray);
        std::vector<full_object_detection> shapes;
        for (unsigned long j = 0; j < dets.size(); ++j) {
            full_object_detection shape = sp(img, dets[j]);
            shapes.push_back(shape);
        }
        jclass cls = env->FindClass("[I");
        jintArray iniVal = env->NewIntArray(dets.size());
        jobjectArray outer = env->NewObjectArray(dets.size(), cls, iniVal);

        for (int i = 0; i < dets.size(); ++i){
            rectangle rect = dets[0];
            float k = (float)img.nc()/(float)resizedImg.nc();
            jintArray r;
            r = env->NewIntArray(4);
            jint fill[4];
            fill[0] = (long) (rect.left() * k);
            fill[1] = (long) (rect.top() * k);
            fill[2] = (long) (rect.right() * k);
            fill[3] = (long) (rect.bottom() * k);
            env->SetIntArrayRegion(r, 0, 4, fill);
            env->SetObjectArrayElement(outer, i, r);
            env->DeleteLocalRef(r);
        }
        dlib::array<array2d<rgb_pixel> > face_chips;
        //extract_image_chips(img, get_face_chip_details(shapes), face_chips);
        return outer;

    } catch (int a) {

    }
    return NULL;
}