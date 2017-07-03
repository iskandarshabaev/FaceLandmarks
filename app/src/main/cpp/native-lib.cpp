#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/image_io.h>
using namespace dlib;

frontal_face_detector sFaceDetector;
static shape_predictor sp;

#define JNI_METHOD(NAME) \
    Java_org_dlib_FrontalFaceDetector_##NAME

/*
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

    // Lock the bitmap for copying the pixels safely.
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

    // Unlock the bitmap.
    AndroidBitmap_unlockPixels(env, bitmap);
}
*/
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
JNI_METHOD(initFrontalFaceDetector)(JNIEnv *env, jobject obj) {
    sFaceDetector = get_frontal_face_detector();
}

/*
extern "C" JNIEXPORT jbyteArray JNICALL
JNI_METHOD(detectFaces)(JNIEnv *env,
                        jobject thiz,
                        jobject bitmap) {

    // Convert bitmap to dlib::array2d.
    array2d<rgb_pixel> img;
    convertBitmapToArray2d(env, bitmap, img);


    const long width = img.nc();
    const long height = img.nr();

    vector<rectangle> dets = sFaceDetector(img);
    vector<full_object_detection> shapes;

    for (unsigned long j = 0; j < dets.size(); ++j)
    {
        full_object_detection shape = sp(img, dets[j]);
        shapes.push_back(shape);
    }

    // To protobuf message.
    FaceList faces;
    for (unsigned long i = 0; i < dets.size(); ++i) {

        rectangle& det = dets.at(i);

        Face* face = faces.add_faces();
        RectF* bound = face->mutable_bound();

        bound->set_left((float) det.left() / width);
        bound->set_top((float) det.top() / height);
        bound->set_right((float) det.right() / width);
        bound->set_bottom((float) det.bottom() / height);
    }

    // Prepare the return message.
    int outSize = faces.ByteSize();
    jbyteArray out = env->NewByteArray(outSize);
    jbyte* buffer = new jbyte[outSize];

    faces.SerializeToArray(buffer, outSize);
    env->SetByteArrayRegion(out, 0, outSize, buffer);
    delete[] buffer;

    return out;
}
*/