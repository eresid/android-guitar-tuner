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

import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import com.example.GuitarTuner.PitchDetectionRepresentation;

public class DrawableView extends View {

	public DrawableView(Context context) {
		super(context);
		
		NotePitches[0][0] = 82.41;
		NotePitches[0][1] = 87.31;
		NotePitches[0][2] = 92.5;
		NotePitches[0][3] = 98;
		NotePitches[0][4] = 103.8;
		NotePitches[0][5] = 110;
		NotePitches[1][0] = 110;
		NotePitches[1][1] = 116.54;
		NotePitches[1][2] = 123.48;
		NotePitches[1][3] = 130.82;
		NotePitches[1][4] = 138.59;
		NotePitches[2][0] = 147.83;
		NotePitches[2][1] = 155.56;
		NotePitches[2][2] = 164.81;
		NotePitches[2][3] = 174.62;
		NotePitches[2][4] = 185;
		NotePitches[3][0] = 196;
		NotePitches[3][1] = 207;
		NotePitches[3][2] = 220;
		NotePitches[3][3] = 233.08;
		NotePitches[3][4] = 246.96;
		NotePitches[4][0] = 246.96;
		NotePitches[4][1] = 261.63;
		NotePitches[4][2] = 277.18;
		NotePitches[4][3] = 293.66;
		NotePitches[5][0] = 329.63;
		NotePitches[5][1] = 349.23;
		NotePitches[5][2] = 369.99;
		NotePitches[5][3] = 392;
		NotePitches[5][4] = 415.3;
		NotePitches[5][5] = 440;
		
		for (int string_no = 0; string_no < 6; string_no++) {
			for (int fret = 0; fret < 6; fret++) {
				if (NotePitches[string_no][fret] > 0) {
				  NotePitchesMap.put(NotePitches[string_no][fret], string_no * 100 + fret);  // encode coordinates
				}
			}
		}
		
	}
	
	// NotePitches[i][j] is the pitch of i-th string on j-th fret. 0th fret means an open fret.
	private double[][] NotePitches = new double[6][6]; 
	private TreeMap<Double, Integer> NotePitchesMap = new TreeMap<Double, Integer>(); 
    
	private final static int MIN_AMPLITUDE = 40000;
	private final static int MaxAmplitude = 200000;
	private final static double MAX_PITCH_DIFF = 20;  // in Hz

	private int GetFingerboardCoord(double pitch) {
		final SortedMap<Double, Integer> tail_map = NotePitchesMap.tailMap(pitch);
		final SortedMap<Double, Integer> head_map = NotePitchesMap.headMap(pitch);
		final double closest_right = tail_map == null || tail_map.isEmpty() ? NotePitchesMap.firstKey() : tail_map.firstKey(); 
		final double closest_left = head_map == null || head_map.isEmpty() ? NotePitchesMap.firstKey() : head_map.lastKey();
		if (closest_right - pitch < pitch - closest_left) {
			return NotePitchesMap.get(closest_right);
		} else {
			return NotePitchesMap.get(closest_left);
		}
	}
	
	final int FINGERBOARD_PADDING = 10;
	private void drawFingerboard(Canvas canvas, Rect rect) {
		Paint paint = new Paint();
		paint.setARGB(255, 100, 200, 100);
        // Draw strings		
		for (int i = 0; i < 6; i++) {
			final int offset = Math.round((rect.height() - FINGERBOARD_PADDING * 2) / 5 * i) + FINGERBOARD_PADDING;
			canvas.drawLine(rect.left, rect.top + offset, rect.right, rect.top + offset, paint);
		}
		// Draw fingerboard's end.
		canvas.drawRect(rect.right - FINGERBOARD_PADDING, rect.top, rect.right, rect.bottom, paint);
		
        // Draw frets
		for (int i = 1; i < 6; i++) {
			final int offset = Math.round((rect.width() - FINGERBOARD_PADDING * 2) / 5 * i) + FINGERBOARD_PADDING;
			canvas.drawLine(rect.right - offset, rect.top, rect.right - offset, rect.bottom, paint);
		}
	}
	
	private long getAmplitudeScreenHeight(Canvas canvas, double amplitude, Rect histogram_rect) {
		return Math.round(amplitude / MaxAmplitude * histogram_rect.height());
	}
	
	private void drawPitchOnFingerboard(Canvas canvas, Rect rect) {
		final int MARK_RADIUS = 5;
		if (representation_ == null) return;
		final int alpha = representation_.GetAlpha();
		if (alpha == 0) return;
		int string_no = representation_.string_no;
		int fret = representation_.fret;
		
		Paint paint = new Paint();
		paint.setARGB(alpha, 200, 210, 210);
		if (fret == 0) {
			// Highlight the string.
			final int offset = Math.round((rect.height() - FINGERBOARD_PADDING * 2) / 5 * string_no) + FINGERBOARD_PADDING;
			canvas.drawLine(rect.left, rect.top + offset, rect.right, rect.top + offset, paint);
			// Actually use the corresponding coordinate on the previous string.
			if (string_no > 0) {
				if (string_no == 4) {
					fret = 3;
				} else {
					fret = 4;
				} 
				string_no--;
			}
		}
		
		// Draw the needed position on the fingerboard.
		final long offset_y = Math.round((rect.height() - FINGERBOARD_PADDING * 2) / 5 * string_no) + FINGERBOARD_PADDING;
		final long offset_x = Math.round((rect.width() - FINGERBOARD_PADDING * 2) 
				/ 5 * (fret - 0.5)) + FINGERBOARD_PADDING;
		canvas.drawCircle(rect.right - offset_x, rect.top + offset_y, MARK_RADIUS, paint);
	}

	private boolean drawHistogram(Canvas canvas, Rect rect) {
		if (frequencies_ == null) return false;
		Paint paint = new Paint();
		// Draw border.
		paint.setARGB(80, 200, 200, 200);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rect, paint);
		
		// Draw threshold.
		paint.setARGB(180, 200, 0, 0);
		final long threshold_screen_height = getAmplitudeScreenHeight(canvas, MIN_AMPLITUDE, rect);
		canvas.drawLine(rect.left, rect.bottom - threshold_screen_height,
				        rect.right, rect.bottom - threshold_screen_height, paint);

		// Draw histogram.
		paint.setARGB(255, 140, 140, 140);

		boolean above_threshold = false;
		int column_no = 0;
		final int column_width = rect.width() / frequencies_.size();
		Iterator<Entry<Double, Double>> it = frequencies_.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Double, Double> entry = it.next();
			// double frequency = entry.getKey();
			final double amplitude = Math.min(entry.getValue(), MaxAmplitude);
			final long height = getAmplitudeScreenHeight(canvas, amplitude, rect);
			if (height > threshold_screen_height) above_threshold = true;
			canvas.drawRect(
					rect.left + column_no * column_width,
					rect.bottom - height, 
					rect.left + (column_no + 1) * column_width,
					rect.bottom, 
					paint);
			column_no++;
		}
		return above_threshold;
	}

	private void drawCurrentFrequency(Canvas canvas, int x, int y) {
		if (representation_ == null) return;
		final int alpha = representation_.GetAlpha();
		if (alpha == 0) return;
		Paint paint = new Paint();
		paint.setARGB(alpha, 200, 0, 0);
		paint.setTextSize(35);
		canvas.drawText(Math.round(representation_.pitch) + " Hz", 20, 40, paint);
	}
	
	protected void onDraw(Canvas canvas) {
		final int MARGIN = 20;
		final int effective_height = canvas.getHeight() - 4 * MARGIN;
		final int effective_width = canvas.getWidth() - 2 * MARGIN;
		final Rect fingerboard = new Rect(MARGIN, effective_height * 20 / 100 + MARGIN,
				                          effective_width + MARGIN, effective_height * 60 / 100 + MARGIN);
		final Rect histogram = new Rect(MARGIN, effective_height * 60 / 100 + 2 * MARGIN,
                effective_width + MARGIN, effective_height + MARGIN);
		if (drawHistogram(canvas, histogram)) {
			final int coord = GetFingerboardCoord(pitch_);
			final int string_no = coord / 100;
			final int fret = coord % 100;
			final double found_pitch = NotePitches[string_no][fret];
			final double diff = Math.abs(found_pitch - pitch_);
			if (diff < MAX_PITCH_DIFF) {
				representation_ = new PitchDetectionRepresentation(pitch_, string_no, fret);
			}
		}
		drawCurrentFrequency(canvas, 20, 50);
		drawFingerboard(canvas, fingerboard);
		drawPitchOnFingerboard(canvas, fingerboard);
	}

	public void setDetectionResults(final HashMap<Double, Double> frequencies, double pitch) {
		frequencies_ = frequencies;
		pitch_ = pitch;
	}

	private HashMap<Double, Double> frequencies_;
	private double pitch_;
	private PitchDetectionRepresentation representation_;
}