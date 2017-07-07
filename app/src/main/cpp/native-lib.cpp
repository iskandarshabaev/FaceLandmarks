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



void convertBitmapToArray2d(JNIEnv* env,
                            jobject bitmap,
                            array2d<rgb_pixel>& out) {
    AndroidBitmapInfo bitmapInfo;
    void* pixels;
    int state;
    if (0 > (state = AndroidBitmap_getInfo(env, bitmap, &bitmapInfo))) {
        return;
    } else if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
    }
    if (0 > (state = AndroidBitmap_lockPixels(env, bitmap, &pixels))) {
        return;
    }
    out.set_size((long) bitmapInfo.height, (long) bitmapInfo.width);
    char* line = (char*) pixels;
    for (int h = 0; h < bitmapInfo.height; ++h) {
        for (int w = 0; w < bitmapInfo.width; ++w) {
            uint32_t* color = (uint32_t*) (line + 4 * w);
            out[h][w].red = (unsigned char) (0xFF & ((*color) >> 24));
            out[h][w].green = (unsigned char) (0xFF & ((*color) >> 16));
            out[h][w].blue = (unsigned char) (0xFF & ((*color) >> 8));
        }
        line = line + bitmapInfo.stride;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
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
JNIEXPORT void JNICALL
JNI_METHOD(detectLandmarksFromFace)(JNIEnv *env, jobject obj, jobject bitmap) {
    try {
        array2d<rgb_pixel> img;
        convertBitmapToArray2d(env, bitmap, img);
        array2d<unsigned char> img_gray;
        assign_image(img_gray, img);
        //pyramid_up(img);
        std::vector<rectangle> dets = sFaceDetector(img_gray);
        std::vector<full_object_detection> shapes;
        for (unsigned long j = 0; j < dets.size(); ++j) {
            full_object_detection shape = sp(img, dets[j]);
            shapes.push_back(shape);
        }
        dlib::array<array2d<rgb_pixel> > face_chips;
        //extract_image_chips(img, get_face_chip_details(shapes), face_chips);

    } catch (int a) {

    }
}