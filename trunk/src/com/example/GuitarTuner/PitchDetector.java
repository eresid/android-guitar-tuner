/** Copyright (C) 2009 by Aleksey Surkov.
**
** Permission to use, copy, modify, and distribute this software and its
** documentation for any purpose and without fee is hereby granted, provided
** that the above copyright notice appear in all copies and that both that
** copyright notice and this permission notice appear in supporting
** documentation.  This software is provided "as is" without express or
** implied warranty.
*/

package com.example.GuitarTuner;

import java.lang.Runnable;
import java.lang.Thread;
import java.util.HashMap;

import com.example.GuitarTuner.FFT;
import com.example.GuitarTuner.GuitarTunerActivity;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;

public class PitchDetector implements Runnable {
  // Currently, only this combination of rate, encoding and channel mode actually works.
  private final static int RATE = 8000;
  private final static int CHANNEL_MODE = AudioFormat.CHANNEL_CONFIGURATION_MONO;
  private final static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  
  private final static int BUFFER_SIZE_IN_MS = 3000;
  private final static int CHUNK_SIZE_IN_SAMPLES_POW2 = 11;
  private final static int CHUNK_SIZE_IN_SAMPLES = 2048;  // = 2 ^ CHUNK_SIZE_IN_SAMPLES_POW2  
  private final static int CHUNK_SIZE_IN_MS = 1000 * CHUNK_SIZE_IN_SAMPLES / RATE;
  private final static int BUFFER_SIZE_IN_BYTES = RATE * BUFFER_SIZE_IN_MS / 1000 * 2;
  private final static int CHUNK_SIZE_IN_BYTES = RATE * CHUNK_SIZE_IN_MS / 1000 * 2;
  private final static int MIN_FREQUENCY = 50;  // HZ
  private final static int MAX_FREQUENCY = 1000;  // HZ - it's for guitar, should be enough
  private final static int DRAW_FREQUENCY_STEP = 10;
  private final static int MIN_AMPLITUDE = 100;
  public PitchDetector(GuitarTunerActivity parent) {
	  parent_ = parent;
	  handler_ = new Handler();
  }
  
  public void run() {
      android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
      recorder_ = new AudioRecord(AudioSource.MIC,
              RATE,
              CHANNEL_MODE,
              ENCODING,
              6144);
      if (recorder_.getState() != AudioRecord.STATE_INITIALIZED) {
    	  parent_.ShowPitchDetectionResult("Can't initialize AudioRecord", null);
    	  return;
      }
	  short[] audio_data = new short[BUFFER_SIZE_IN_BYTES / 2];
	  double[] x_r = new double[CHUNK_SIZE_IN_SAMPLES];
	  double[] x_i = new double[CHUNK_SIZE_IN_SAMPLES];
	  FFT fft = new FFT(CHUNK_SIZE_IN_SAMPLES_POW2);
	  final int min_frequency_fft = Math.round(MIN_FREQUENCY * CHUNK_SIZE_IN_SAMPLES / RATE);
	  final int max_frequency_fft = Math.round(MAX_FREQUENCY * CHUNK_SIZE_IN_SAMPLES / RATE);
      while(!Thread.interrupted()) {
    	  recorder_.startRecording();
    	  recorder_.read(audio_data, 0, CHUNK_SIZE_IN_BYTES / 2);
    	  recorder_.stop();
    	  for (int i = 0; i < CHUNK_SIZE_IN_SAMPLES; i++) {
    		  x_r[i] = audio_data[i]; x_i[i] = 0;
    	  }
    	  fft.doFFT(x_r, x_i, false);
    	  double best_frequency = min_frequency_fft;
    	  double best_amplitude = 0;
    	  HashMap<Double, Double> frequencies = new HashMap<Double, Double>();
    	  final double draw_frequency_step =  1.0 * RATE / CHUNK_SIZE_IN_SAMPLES;
    	  for (int i = min_frequency_fft; i <= max_frequency_fft; i++) {
    		  final double current_frequency = i * 1.0 * RATE / CHUNK_SIZE_IN_SAMPLES;
    		  final double draw_frequency = Math.round((current_frequency - MIN_FREQUENCY) / DRAW_FREQUENCY_STEP) *
    		      DRAW_FREQUENCY_STEP + MIN_FREQUENCY;
    		  final double current_amplitude = Math.pow(x_r[i], 2) + Math.pow(x_i[i], 2);
    		  Double current_sum_for_this_slot = frequencies.get(draw_frequency);
    		  if (current_sum_for_this_slot == null) current_sum_for_this_slot = 0.0;
    		  frequencies.put(draw_frequency, 
    				  Math.pow(current_amplitude, 0.5)/draw_frequency_step + 
    				  current_sum_for_this_slot);
    		  if (current_amplitude > best_amplitude) {
    			  best_frequency = current_frequency;
    			  best_amplitude = current_amplitude;
    		  }
    	  }
    	  String retval = Math.round(best_frequency) + " Hz";
    	  if (best_amplitude < MIN_AMPLITUDE) retval = "<make a sound>";
    	  PostToUI(retval, frequencies);
      }
  }

  private void PostToUI(final String msg, final HashMap<Double, Double> frequencies) {
	  handler_.post(new Runnable() {
		  public void run() {
			  parent_.ShowPitchDetectionResult(msg, frequencies);
		  } 
	  });
  }
  
  private GuitarTunerActivity parent_;
  private AudioRecord recorder_;
  private Handler handler_;
}
