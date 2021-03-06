/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_edus_apollo_opuslib_OpusTool */

#ifndef _Included_com_edus_apollo_opuslib_OpusTool
#define _Included_com_edus_apollo_opuslib_OpusTool
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    startRecord
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_edus_apollo_opuslib_OpusTool_startRecord
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    writeFrame
 * Signature: (Ljava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_edus_apollo_opuslib_OpusTool_writeFrame
  (JNIEnv *, jobject, jobject, jint);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    stopRecord
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_edus_apollo_opuslib_OpusTool_stopRecord
  (JNIEnv *, jobject);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    openOpusFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_edus_apollo_opuslib_OpusTool_openOpusFile
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    seekOpusFile
 * Signature: (F)I
 */
JNIEXPORT jint JNICALL Java_com_edus_apollo_opuslib_OpusTool_seekOpusFile
  (JNIEnv *, jobject, jfloat);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    isOpusFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_edus_apollo_opuslib_OpusTool_isOpusFile
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    closeOpusFile
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_edus_apollo_opuslib_OpusTool_closeOpusFile
  (JNIEnv *, jobject);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    readOpusFile
 * Signature: (Ljava/nio/ByteBuffer;I[I)V
 */
JNIEXPORT void JNICALL Java_com_edus_apollo_opuslib_OpusTool_readOpusFile
  (JNIEnv *, jobject, jobject, jint, jintArray);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    getTotalPcmDuration
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_edus_apollo_opuslib_OpusTool_getTotalPcmDuration
  (JNIEnv *, jobject);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    getWaveform
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_edus_apollo_opuslib_OpusTool_getWaveform
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_edus_apollo_opuslib_OpusTool
 * Method:    getWaveform2
 * Signature: ([SI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_edus_apollo_opuslib_OpusTool_getWaveform2
  (JNIEnv *, jobject, jshortArray, jint);

#ifdef __cplusplus
}
#endif
#endif
