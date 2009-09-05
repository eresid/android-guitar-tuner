/*
 * fft.h
 *
 *  Created on: Sep 5, 2009
 *      Author: surkov
 */

#ifndef FFT_H_
#define FFT_H_

#include <jni.h>


// Taken from http://www.ddj.com/cpp/199500857
// which took it from Numerical Recipes in C++, p.513

// The 'data' should be an array of length 'size' * 2,
// where each even element corresponds
// to the real part and each odd element to the imaginary part of a
// complex number.
// For an incoming stream, all imaginary parts should be zero.
void Java_com_example_GuitarTuner_PitchDetector_DoFFT(
    JNIEnv* env, jobject thiz, jdoubleArray data, jint size);

jint JNI_OnLoad(JavaVM* vm, void* reserved);

#endif /* FFT_H_ */
