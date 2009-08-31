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
import java.util.Map.Entry;

import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;


public class DrawableView extends View {

    public DrawableView(Context context) {
        super(context);
       
    }

    protected void onDraw(Canvas canvas) {
    	Paint paint = new Paint();
    	paint.setARGB(255, 140, 140, 140);
    	paint.setTextSize(35);
        canvas.drawText(text_, 20, 40, paint);
        
        final int bounding_top = 50;
        final int bounding_left = 5;
        final int bounding_bottom = canvas.getHeight() - 50;
        final int bounding_right = canvas.getWidth() - 5;
        final int MaxAmplitude = 500;
        if (frequencies_ != null) {
        	int column_no = 0;
        	final int column_width = (bounding_right - bounding_left) / frequencies_.size();
        	Iterator<Entry<Double, Double>> it = frequencies_.entrySet().iterator();
        	while(it.hasNext()) {
        		Entry<Double, Double> entry = it.next();
        		//double frequency = entry.getKey();
        		double amplitude = Math.min(entry.getValue(), MaxAmplitude);
        		long height = Math.round(amplitude / MaxAmplitude * (bounding_bottom - bounding_top));
        		canvas.drawRect(bounding_left + column_no * column_width,
        				        bounding_bottom - height, 
        				        bounding_left + (column_no + 1) * column_width, 
        				        bounding_bottom, 
        				        paint);
        		column_no++;
        		
        	}
        	
        }
    }
    
    public void setText(String text) {
    	text_ =  text;
    	this.invalidate();
    }
    
    public void setFrequencies(final HashMap<Double, Double> frequencies) {
    	frequencies_ = frequencies;
    }
    
  private String text_;
  HashMap<Double, Double> frequencies_;
}