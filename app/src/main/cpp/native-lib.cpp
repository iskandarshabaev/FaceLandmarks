#include <jni.h>
#include <string>
#include <dlib/image_processing/frontal_face_detector.h>
#include <dlib/image_processing.h>
#include <dlib/image_io.h>
#include <android/bitmap.h>

#define JNI_METHOD(NAME) \
    Java_org_dlib_FrontalFaceDetector_##NAME

void convertBitmapToArray2d(JNIEnv* env,
                            jobject bitmap,
                            dlib::array2d<dlib::rgb_pixel>& out) {
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

dlib::frontal_face_detector sFaceDetector;
dlib::shape_predictor sp;

extern "C"
JNIEXPORT jobject JNICALL
JNI_METHOD(findLandmarks)(JNIEnv *env, jobject obj) {
    dlib::array2d<dlib::rgb_pixel> img;
    pyramid_up(img);
    jclass cls = env->FindClass("org/dlib/FullObjectDetection");
    jmethodID cid = env->GetMethodID(cls, "<init>", "()V");
    return env->NewObject(cls, cid);
}

extern "C"
JNIEXPORT void JNICALL
JNI_METHOD(initFrontalFaceDetector)(JNIEnv *env, jobject obj, jstring path) {
    sFaceDetector = dlib::get_frontal_face_detector();
    const char *nativeString = env->GetStringUTFChars(path, 0);
    dlib::deserialize(nativeString) >> sp;
    env->ReleaseStringUTFChars(path, nativeString);
}

extern "C"
JNIEXPORT void JNICALL
JNI_METHOD(detectLandmarksFromFace)(JNIEnv *env, jobject obj, jobject bitmap) {
    try {
        dlib::array2d<dlib::rgb_pixel> img;
        convertBitmapToArray2d(env, bitmap, img);
        pyramid_up(img);
        const long width = img.nc();
        const long height = img.nr();
        std::vector<dlib::rectangle> dets = sFaceDetector(img);
        std::vector<dlib::full_object_detection> shapes;
        for (unsigned long j = 0; j < dets.size(); ++j) {
            dlib::full_object_detection shape = sp(img, dets[j]);
            shapes.push_back(shape);
        }
        dlib::array<dlib::array2d<dlib::rgb_pixel> > face_chips;
        //extract_image_chips(img, get_face_chip_details(shapes), face_chips);
    } catch (int a) {

    }
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